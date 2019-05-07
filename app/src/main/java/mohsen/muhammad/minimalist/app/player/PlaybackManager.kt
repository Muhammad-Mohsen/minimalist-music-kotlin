package mohsen.muhammad.minimalist.app.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.initialize
import mohsen.muhammad.minimalist.core.ext.isPlayingSafe
import mohsen.muhammad.minimalist.core.ext.playPause
import mohsen.muhammad.minimalist.data.*
import mohsen.muhammad.minimalist.data.files.FileHelper
import java.io.File
import java.util.*


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * The object that's actually responsible for playing the music
 * This was originally an Android Service which had the advantage of running on a background thread, so any expensive operation can be done directly.
 * As this is no longer the case, expensive operations (for example MediaPlayer#prepare, and updateMetadata) are called inside a handler
 */

object PlaybackManager :
	EventBus.Subscriber,
	MediaPlayer.OnCompletionListener,
	AudioManager.OnAudioFocusChangeListener, // audio focus loss
	BroadcastReceiver() // headphone removal
{

	private const val eventSource = EventSource.SERVICE

	private lateinit var player: MediaPlayer
	private lateinit var playlist: Playlist

	private var timer: Timer? = null // a timer to update the seek
	private lateinit var handler: Handler

	private lateinit var audioFocusHandler: AudioFocusHandler

	val isPlaying: Boolean
		get() = player.isPlayingSafe

	// called when the application starts
	fun start(context: Context) {
		player = MediaPlayer() // initialize the media player
		player.setOnCompletionListener(this) //Set up MediaPlayer event listeners

		playlist = Playlist() // initialize the playlist

		EventBus.subscribe(this)

		audioFocusHandler = AudioFocusHandler(this, context) // audio focus loss
		context.registerReceiver(PlaybackManager, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) // headphone removal

		handler = Handler()
	}

	// restores state (from the State object) from a previous session
	private fun reinitialize() {
		if (State.Track.path.isBlank()) return

		handler.post {
			setTrack(State.Track.path)
			updateSeek(State.Track.seek)
		}
	}

	// playback
	private fun setTrack(path: String, updatePlaylist: Boolean = true) {
		// update playlist
		if (updatePlaylist) playlist.updateItems(path)

		playlist.setTrack(path, true)

		// initialize the track
		player.initialize(path)
	}
	private fun playTrack(path: String?) {

		if (path == null) return

		// the playback manager runs on the main thread
		// so expensive operations are run on a background thread
		handler.post {
			setTrack(path)

			val focusResult = audioFocusHandler.request()
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return@post

			player.start()

			sendMetadataUpdate(path)
			sendSeekUpdates()
		}
	}

	private fun playPause(play: Boolean) {
		if (play) {
			val focusResult = audioFocusHandler.request()
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

		} else {
			audioFocusHandler.abandon()
		}

		player.playPause(play)
		sendSeekUpdates(play)
	}

	// seek
	private fun updateSeek(mils: Int) {
		player.seekTo(mils)
		sendSingleSeekUpdate()
	}
	private fun sendSeekUpdates(toggleDispatch: Boolean = true) {
		timer?.cancel()

		if (!toggleDispatch) return

		timer = Timer()
		timer?.scheduleAtFixedRate(object : TimerTask() {
			override fun run() {
				sendSingleSeekUpdate()
			}

		}, 0L, 1000L)
	}

	// used to update the seek (used for when the playback is stopped but the user changes seek)
	private fun sendSingleSeekUpdate() {
		State.Track.seek = player.currentPosition
		EventBus.send(SystemEvent(eventSource, EventType.SEEK_UPDATE))
	}

	// metadata
	private fun sendMetadataUpdate(path: String) {
		handler.post {
			updateMetadataState(path)
			EventBus.send(SystemEvent(eventSource, EventType.METADATA_UPDATE))
		}
	}
	private fun updateMetadataState(path: String) {
		val metadataHelper = FileHelper(File(path))

		State.Track.path = path // this is mostly redundant, but it's ok
		State.Track.title = metadataHelper.title
		State.Track.album = metadataHelper.album
		State.Track.artist = metadataHelper.artist
		State.Track.duration = metadataHelper.duration
	}

	// pause playback on audio focus loss
	override fun onAudioFocusChange(focusChange: Int) {
		if (focusChange != AudioManager.AUDIOFOCUS_GAIN) {
			player.pause()
			EventBus.send(SystemEvent(eventSource, EventType.PAUSE))
		}
	}

	// becoming noisy receiver handler
	override fun onReceive(context: Context, intent: Intent) {
		if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
			player.pause()
			EventBus.send(SystemEvent(eventSource, EventType.PAUSE))
		}
	}

	// on playback completion
	override fun onCompletion(mp: MediaPlayer) {
		val nextTrack = playlist.getNextTrack(true)
		
		if (nextTrack != null) {
			playTrack(nextTrack)
			EventBus.send(SystemEvent(eventSource, EventType.PLAY_ITEM, nextTrack))
		
		} else {
			EventBus.send(SystemEvent(eventSource, EventType.PAUSE))
		}
	}

	// event bus handler
	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent && data.source != eventSource) { // if we're not the source
			when (data.type) {
				EventType.PLAY_ITEM -> playTrack(data.extras)
				EventType.PLAY -> playPause(true)
				EventType.PAUSE -> playPause(false)
				EventType.SEEK_UPDATE -> updateSeek(data.extras.toInt())

				// playlist stuff
				EventType.CYCLE_REPEAT -> playlist.cycleRepeatMode()
				EventType.CYCLE_SHUFFLE -> playlist.toggleShuffle()
				EventType.PLAY_PREVIOUS -> playTrack(playlist.getPreviousTrack())
				EventType.PLAY_NEXT -> playTrack(playlist.getNextTrack(false))

				EventType.METADATA_UPDATE -> reinitialize()
			}
		}
	}
}

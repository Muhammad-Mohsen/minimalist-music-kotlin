package mohsen.muhammad.minimalist.app.player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.initialize
import mohsen.muhammad.minimalist.core.ext.isPlayingSafe
import mohsen.muhammad.minimalist.core.ext.playPause
import mohsen.muhammad.minimalist.core.ext.readablePosition
import mohsen.muhammad.minimalist.data.PlaybackEvent
import mohsen.muhammad.minimalist.data.PlaybackEventSource
import mohsen.muhammad.minimalist.data.PlaybackEventType
import mohsen.muhammad.minimalist.data.files.FileHelper
import java.io.File
import java.util.*


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * The object that's actually responsible for playing the music
 * This was originally an Android Service which had the advantage of running on a background thread, so any expensive operation can be done directly.
 * As this is no longer the case, expensive operation (for example MediaPlayer#prepare, getMetadata) are called inside a handler
 */

object PlaybackManager :
	EventBus.Subscriber,
	MediaPlayer.OnCompletionListener,
	AudioManager.OnAudioFocusChangeListener
{

	private const val eventSource = PlaybackEventSource.SERVICE

	private lateinit var player: MediaPlayer
	private lateinit var playlist: Playlist

	private var timer: Timer? = null // a timer to update the seek
	private lateinit var handler: Handler

	private lateinit var audioFocusHandler: AudioFocusHandler

	val isPlaying: Boolean
		get() = player.isPlayingSafe

	// playback
	private fun setTrack(path: String, updatePlaylist: Boolean = true) {
		// update playlist
		if (updatePlaylist) playlist.updateItems(path)

		playlist.setTrack(path, true)

		// initialize the track
		player.initialize(path)
	}
	private fun playTrack(path: String) {
		handler.post {
			setTrack(path)

			val focusResult = audioFocusHandler.request()
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return@post

			player.start()

			sendMetadata(path)
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
	// used to update the seek (used for when the playback is stopped but the user changed seek)
	private fun sendSingleSeekUpdate() {
		val position = player.currentPosition.toString()
		val readablePosition = player.readablePosition

		EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.UPDATE_SEEK, "$position;$readablePosition"))
	}

	// metadata
	private fun sendMetadata(path: String) {
		handler.post {
			val metadata = getMetadata(path)
			EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.UPDATE_METADATA, metadata))
		}
	}
	private fun getMetadata(path: String): String {
		val metadataHelper = FileHelper(File(path))
		return "${metadataHelper.title};${metadataHelper.album};${metadataHelper.artist};${metadataHelper.duration};${player.duration}"
	}

	// pause playback on audio focus loss
	override fun onAudioFocusChange(focusChange: Int) {
		if (focusChange != AudioManager.AUDIOFOCUS_GAIN) {
			player.pause()
			EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.PAUSE))
		}
	}

	// on playback completion
	override fun onCompletion(mp: MediaPlayer) {
		val nextTrack = playlist.getNextTrack(true)
		
		if (nextTrack != null) {
			playTrack(nextTrack)
			EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.PLAY_ITEM, nextTrack))
		
		} else {
			EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.PAUSE))
		}
	}

	// from the event bus
	override fun receive(data: EventBus.EventData) {
		if (data is PlaybackEvent && data.source != eventSource) { // if we're not the source
			when (data.type) {
				PlaybackEventType.PLAY_ITEM -> playTrack(data.extras)
				PlaybackEventType.PLAY -> playPause(true)
				PlaybackEventType.PAUSE -> playPause(false)
				PlaybackEventType.UPDATE_SEEK -> updateSeek(data.extras.toInt())
				PlaybackEventType.CYCLE_REPEAT -> playlist.cycleRepeatMode()
				PlaybackEventType.CYCLE_SHUFFLE -> playlist.toggleShuffle()
			}
		}
	}

	// lifecycle
	fun start(context: Context) {
		player = MediaPlayer() // initialize the media player
		player.setOnCompletionListener(this) //Set up MediaPlayer event listeners

		playlist = Playlist() // initialize the playlist

		EventBus.subscribe(this) // register the service instance

		handler = Handler()

		audioFocusHandler = AudioFocusHandler(this, context)
	}
	fun destroy() {
		player.release() // destroy the Player instance
		EventBus.unsubscribe(this)
	}
}

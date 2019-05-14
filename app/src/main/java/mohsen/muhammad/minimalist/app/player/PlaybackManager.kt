package mohsen.muhammad.minimalist.app.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import mohsen.muhammad.minimalist.app.notification.MediaNotificationManager
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
 * A foreground service that's actually responsible for playing the music
 */

class PlaybackManager :
	Service(),
	EventBus.Subscriber,
	MediaPlayer.OnCompletionListener,
	AudioManager.OnAudioFocusChangeListener // audio focus loss
{

	private val player = MediaPlayer() // initialize the media player
	private val playlist = Playlist() // initialize the playlist

	// TODO remove this thing
	private var timer: Timer? = null // a timer to update the seek

	private lateinit var audioFocusHandler: AudioFocusHandler

	private lateinit var notificationManager: MediaNotificationManager // needed throughout the life of the app because it subs to the EventBus and updates the notification

	// headphone removal receiver
	private val becomingNoisyReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
				player.pause()
				EventBus.send(SystemEvent(EVENT_SOURCE, EventType.PAUSE))
			}
		}
	}

	// life cycle...YAY!!
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

		registerSelf(this)
		EventBus.subscribe(this)
		registerReceiver(becomingNoisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) // headphone removal

		audioFocusHandler = AudioFocusHandler(this, this) // audio focus loss

		player.setOnCompletionListener(this) //Set up MediaPlayer event listeners
		player.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK) // acquire a wake lock so that the system won't shut us down

		notificationManager = MediaNotificationManager(applicationContext)
		startForeground(MediaNotificationManager.NOTIFICATION_ID, notificationManager.createNotification())

		return super.onStartCommand(intent, flags, startId)
	}
	override fun onDestroy() {
		player.release() // destroy the Player instance
		unregisterReceiver(becomingNoisyReceiver)
		EventBus.unsubscribe(this)
	}

	// restores state (from the State object) from a previous session
	private fun reinitialize() {
		if (State.Track.path.isBlank()) return

		setTrack(State.Track.path)
		updateSeek(State.Track.seek)
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

		setTrack(path)

		val focusResult = audioFocusHandler.request()
		if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

		player.start()

		sendMetadataUpdate(path)
		sendSeekUpdates()
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
		EventBus.send(SystemEvent(EVENT_SOURCE, EventType.SEEK_UPDATE))
	}

	// metadata
	private fun sendMetadataUpdate(path: String) {
		updateMetadataState(path)
		EventBus.send(SystemEvent(EVENT_SOURCE, EventType.METADATA_UPDATE))
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
			EventBus.send(SystemEvent(EVENT_SOURCE, EventType.PAUSE))
		}
	}

	// on playback completion
	override fun onCompletion(mp: MediaPlayer) {
		val nextTrack = playlist.getNextTrack(true)
		
		if (nextTrack != null) {
			playTrack(nextTrack)
			EventBus.send(SystemEvent(EVENT_SOURCE, EventType.PLAY_ITEM, nextTrack))
		
		} else {
			EventBus.send(SystemEvent(EVENT_SOURCE, EventType.PAUSE))
		}
	}

	// event bus handler
	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent && data.source != EVENT_SOURCE) { // if we're not the source
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

	// override is mandated by the framework
	override fun onBind(intent: Intent?): IBinder? { return null }

	companion object {

		private const val EVENT_SOURCE = EventSource.SERVICE

		private var instance: PlaybackManager? = null

		val isPlaying: Boolean
			get() = instance?.player.isPlayingSafe


		private fun registerSelf(i: PlaybackManager) {
			instance = i
		}
	}
}

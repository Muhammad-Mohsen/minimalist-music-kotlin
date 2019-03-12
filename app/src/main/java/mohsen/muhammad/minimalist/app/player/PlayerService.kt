package mohsen.muhammad.minimalist.app.player

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.initialize
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
 * Background service that's actually responsible for playing the music
 */

class PlayerService : Service(),
	EventBus.Subscriber,
	MediaPlayer.OnCompletionListener,
	AudioManager.OnAudioFocusChangeListener
{

	private val eventSource = PlaybackEventSource.SERVICE

	private var player: MediaPlayer? = null
	private var playlist: Playlist? = null

	private var timer: Timer? = null // a timer to update the seek

	// playback
	private fun initializeTrack(path: String, updatePlaylist: Boolean = true) {
		// update playlist
		if (updatePlaylist) playlist?.updateItems(path)

		playlist?.setTrack(path, true)

		// initialize the track
		player?.initialize(path)
	}
	private fun playTrack(path: String) {
		initializeTrack(path)
		player?.start()

		sendSeekUpdates()
	}

	private fun playPause(play: Boolean) {
		player?.playPause(play)
		sendSeekUpdates(play)
	}

	// seek
	private fun updateSeek(mils: Int) {
		player?.seekTo(mils)

		// val isPlaying = player?.isPlaying ?: false
		// sendSeekUpdates(isPlaying)
	}
	private fun sendSeekUpdates(toggleDispatch: Boolean = true) {
		timer?.cancel()

		if (!toggleDispatch) return

		timer = Timer()
		timer?.scheduleAtFixedRate(object : TimerTask() {
			override fun run() {
				val position = player?.currentPosition.toString()
				val readablePosition = player?.readablePosition.toString()

				EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.UPDATE_SEEK, "$position;$readablePosition"))
			}

		}, 0L, 1000L)
	}

	// metadata
	private fun getMetadata(path: String): String {
		val metadataHelper = FileHelper(File(path))
		return "${metadataHelper.title};${metadataHelper.album};${metadataHelper.artist};${metadataHelper.duration};${player?.duration}"
	}
	private fun sendMetadata(path: String) {
		val metadata = getMetadata(path)
		EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.UPDATE_METADATA, metadata))
	}

	// pause playback on audio focus loss
	override fun onAudioFocusChange(focusChange: Int) {
		if (focusChange != AudioManager.AUDIOFOCUS_GAIN) {
			player?.pause()
			EventBus.send(PlaybackEvent(eventSource, PlaybackEventType.PAUSE))
		}
	}

	// on play complete
	override fun onCompletion(mp: MediaPlayer) {
		val nextTrack = playlist?.getNextTrack(true)
		
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
				PlaybackEventType.PLAY_ITEM -> {
					playTrack(data.extras)
					sendMetadata(data.extras)
				}
				PlaybackEventType.PLAY -> playPause(true)
				PlaybackEventType.PAUSE -> playPause(false)
				PlaybackEventType.UPDATE_SEEK -> updateSeek(data.extras.toInt())
				PlaybackEventType.CYCLE_REPEAT -> playlist?.cycleRepeatMode()
				PlaybackEventType.CYCLE_SHUFFLE -> playlist?.toggleShuffle()
			}
		}
	}

	// life cycle
	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

		// initialize the media player
		player = MediaPlayer()

		//Set up MediaPlayer event listeners
		player?.setOnCompletionListener(this)


		// initialize the playlist
		playlist = Playlist()

		// register the service instance
		EventBus.subscribe(this)

		return super.onStartCommand(intent, flags, startId)
	}
	override fun onDestroy() {
		player?.release() // destroy the Player instance
		EventBus.unsubscribe(this)
	}

	// override is mandated by the framework
	override fun onBind(intent: Intent): IBinder? { return null }
}

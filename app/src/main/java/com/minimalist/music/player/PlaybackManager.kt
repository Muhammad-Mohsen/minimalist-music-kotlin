package com.minimalist.music.player

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.ext.cancelSafe
import com.minimalist.music.foundation.ext.currentPositionSafe
import com.minimalist.music.foundation.ext.isPlayingSafe
import com.minimalist.music.foundation.ext.playPause
import com.minimalist.music.foundation.ext.prepareSource
import com.minimalist.music.foundation.ext.unregisterReceiverSafe
import com.minimalist.music.data.state.State
import com.minimalist.music.data.files.getNextChapter
import com.minimalist.music.data.files.getPrevChapter
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Type
import com.minimalist.music.foundation.EventBus.Target
import java.util.Timer
import java.util.TimerTask


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
	private val player = MediaPlayer()
	private lateinit var audioFocusHandler: AudioFocusHandler
	private lateinit var notificationManager: MediaNotificationManager
	private lateinit var sessionManager: MediaSessionManager

	// headphone removal receiver
	private val noisyReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
				player.pause()
				EventBus.dispatch(Event(Type.PAUSE, TARGET))
			}
		}
	}
	private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

	private var timer: Timer? = null // a timer to update the seek
	private var foregrounded = false

	override fun onCreate() {
		super.onCreate()

		registerSelf(this)
		EventBus.subscribe(this)
		State.initialize(applicationContext) // the initialization call in MainActivity.onCreate is not enough...Store was still showing exceptions

		player.apply {// initialize the media player
			setAudioAttributes(
				AudioAttributes.Builder()
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			)

			setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK) // acquire a wake lock so that the system won't shut us down
			setOnCompletionListener(this@PlaybackManager) // set up MediaPlayer event listeners
		}

		audioFocusHandler = AudioFocusHandler(this, this) // audio focus loss
		sessionManager = MediaSessionManager(applicationContext)
		notificationManager = MediaNotificationManager(applicationContext, sessionManager.token)

		// onStartCommand wouldn't have been called at the point when the METADATA_UPDATE event is dispatched to which the response would be to call restoreState
		// so a manual call is necessary
		restoreState(true)
	}

	override fun onDestroy() {
		player.release() // destroy the Player instance
		sessionManager.release() // and the media session
		audioFocusHandler.abandon() // ...and the audio focus

		timer.cancelSafe()
		timer = null

		unregisterReceiverSafe(noisyReceiver)
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		startForegroundSafe()
		return super.onStartCommand(intent, flags, startId)
	}

	// will be called from the fragment onStart to ensure that it's always started
	private fun startForegroundSafe() {
		if (foregrounded) return

		// try/catch because the store reports a ForegroundServiceStartNotAllowedException!
		try {
			foregrounded = true
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(MediaNotificationManager.NOTIFICATION_ID, notificationManager.createNotification(), FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
			else startForeground(MediaNotificationManager.NOTIFICATION_ID, notificationManager.createNotification())

		} catch (_: Exception) {
			foregrounded = false
		}
	}

	// restores state (from the State object) from a previous session
	private fun restoreState(isBootstrapping: Boolean = false) {
		if (!State.track.exists) return

		setTrack(State.track.path, false)
		updateSeek(State.track.seek)
		if (isBootstrapping) EventBus.dispatch(Event(Type.SEEK_UPDATE_USER, Target.SERVICE)) // notify the session
	}

	// playback
	private fun setTrack(path: String, updatePlaylist: Boolean = true) {
		// update playlist
		if (updatePlaylist || State.playlist.isEmpty()) State.playlist.updateItems(path)
		State.playlist.updateIndex(path)

		player.prepareSource(path)
	}

	private fun playTrack(path: String?, updatePlaylist: Boolean = true) {
		if (path == null) return

		setTrack(path, updatePlaylist)

		val focusResult = audioFocusHandler.request()
		if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

		registerReceiver(noisyReceiver, noisyIntentFilter) // headphone removal

		player.playPause(true)
		sendAudioEffectControl(true)
		sendMetadataUpdate(path)
		sendSeekUpdates()
	}
	private fun playPause(play: Boolean) {
		if (play) {
			val focusResult = audioFocusHandler.request()
			if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

			registerReceiver(noisyReceiver, noisyIntentFilter) // headphone removal

		} else {
			audioFocusHandler.abandon()
			unregisterReceiverSafe(noisyReceiver)
		}

		player.playPause(play)
		sendAudioEffectControl(play)
		sendSeekUpdates(play)
	}

	private fun updatePlaybackSpeed(speed: Float = 1F) {
		// the store reports an IllegalStateException crashes over here, so try/catch that sumbitch!
		try {
			val wasPaused = !player.isPlaying
			player.playbackParams = player.playbackParams.setSpeed(speed)

			// apparently, setting the playback speed while paused, resumes playback, so force it here.
			// no need to send any further notifications/events, so directly call the player's pause function
			if (wasPaused) player.playPause(false)
		}
		catch (e: Exception) {
			Log.d(PlaybackManager::class.simpleName, "updatePlaybackSpeed: ${e.message}")
		}
	}

	private fun playNext() {
		if (State.track.hasChapters) {
			val nextChapter = State.track.chapters.getNextChapter(player.currentPositionSafe.toLong())
			if (nextChapter == null) { // already past the last chapter
				playTrack(State.playlist.getNextTrack(false), false) // so get the next track as normal
				return
			}
			updateSeek(nextChapter.startTime.toInt())
			if (!player.isPlaying) playPause(true)

		} else {
			playTrack(State.playlist.getNextTrack(false), false)
		}
	}
	private fun playPrev() {
		if (State.track.hasChapters) {
			val prevChapter = State.track.chapters.getPrevChapter(player.currentPositionSafe.toLong())
			updateSeek(prevChapter.startTime.toInt())
			if (!player.isPlaying) playPause(true)

		} else {
			playTrack(State.playlist.getPreviousTrack(), false)
		}
	}

	// seek
	private fun updateSeek(mils: Int) {
		player.seekTo(mils)
		sendSingleSeekUpdate()
	}
	private fun sendSeekUpdates(toggleDispatch: Boolean = true) {
		timer.cancelSafe()

		if (!toggleDispatch) return

		timer = Timer()
		timer?.schedule(object : TimerTask() {
			override fun run() {
				sendSingleSeekUpdate()
			}

		}, 0L, SEEK_UPDATE_PERIOD)
	}

	private fun fastForward() {
		player.seekTo(player.currentPositionSafe + State.settings.seekJump * 1000)
		sendSingleSeekUpdate()
	}
	private fun rewind() {
		player.seekTo(player.currentPositionSafe - State.settings.seekJump * 1000)
		sendSingleSeekUpdate()
	}

	// used to update the seek (used for when the playback is stopped but the user changes seek)
	private fun sendSingleSeekUpdate() {
		State.track.seek = player.currentPositionSafe
		EventBus.dispatch(Event(Type.SEEK_UPDATE, TARGET))
	}

	// metadata
	private fun sendMetadataUpdate(path: String) {
		State.track.update(path)
		EventBus.dispatch(Event(Type.METADATA_UPDATE, TARGET))
	}

	private fun sendAudioEffectControl(play: Boolean = false) {
		val action = if (play) AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION else AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION

		val intent = Intent(action)
		intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId)
		intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
		sendBroadcast(intent)
	}

	// pause playback on audio focus loss
	override fun onAudioFocusChange(focusChange: Int) {
		if (focusChange != AudioManager.AUDIOFOCUS_GAIN) {
			try {
				player.pause() // throws if app is playing, gets killed, and another app acquires focus (however, shouldn't occur anymore since the focus listener is abandoned in onDestroy)
				EventBus.dispatch(Event(Type.PAUSE, TARGET))

			} catch (e: IllegalStateException) {
				Log.d(PlaybackManager::class.simpleName, "onAudioFocusChange: ${e.message}")
			}
		}
	}

	// on playback completion
	override fun onCompletion(mp: MediaPlayer) {
		// sometimes onComplete is called when it's not actually on complete!!
		// I imagine that this happens due to some race condition where the next track is already loaded, then this hits!!
		if (mp.currentPositionSafe <= ON_COMPLETION_THRESHOLD) return

		var nextTrack = State.playlist.getNextTrack(true)

		// at the end of the playlist, getNextTrack returns null if not on repeat, so the nextTrack is set manually to the starting track...
		val playlistEnd = nextTrack == null
		if (playlistEnd) nextTrack = State.playlist.getTrackByIndex(0)

		if (nextTrack == null) return

		playTrack(nextTrack, false)
		EventBus.dispatch(Event(Type.PLAY_TRACK, TARGET, mapOf("track" to nextTrack)))

		// ...and then paused immediately
		if (playlistEnd) {
			playPause(false)
			EventBus.dispatch(Event(Type.PAUSE, TARGET))
		}
	}

	// event bus handler
	override fun handle(event: Event) {
		if (event.target == TARGET) return

		when (event.type) {
			Type.APP_FOREGROUNDED -> startForegroundSafe()
			Type.PLAY_TRACK -> playTrack(event.data["track"].toString())
			Type.PLAY -> playPause(true)
			Type.PAUSE -> playPause(false)
			Type.SEEK_UPDATE -> updateSeek(event.data["seek"].toString().toInt())
			Type.FF -> fastForward()
			Type.RW -> rewind()
			Type.PLAYBACK_SPEED -> updatePlaybackSpeed(State.settings.playbackSpeed)

			// playlist stuff
			Type.CYCLE_REPEAT -> { State.playlist.cycleRepeatMode() }
			Type.CYCLE_SHUFFLE -> { State.playlist.toggleShuffle() }
			Type.PLAY_PREVIOUS -> playPrev()
			Type.PLAY_NEXT -> playNext()
			Type.PLAY_SELECTED -> playTrack(State.playlist.getTrackByIndex(0), false)

			Type.METADATA_UPDATE -> restoreState()
		}
	}

	override fun onBind(intent: Intent?): IBinder? { return null }

	companion object {
		private const val TARGET = Target.SERVICE
		private const val SEEK_UPDATE_PERIOD = 1000L
		private const val ON_COMPLETION_THRESHOLD = 1000L

		private var instance: PlaybackManager? = null

		val isPlaying: Boolean
			get() = instance?.player.isPlayingSafe

		val audioSessionId: Int
			get() = instance?.player?.audioSessionId ?: 0

		private fun registerSelf(i: PlaybackManager) {
			instance = i
		}
	}
}

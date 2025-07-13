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
import android.media.audiofx.Equalizer
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
import com.minimalist.music.foundation.Moirai
import com.minimalist.music.foundation.ext.getInfo
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
	private val equalizer = Equalizer(0, player.audioSessionId)

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

		equalizer.enabled = true

		audioFocusHandler = AudioFocusHandler(this, this) // audio focus loss
		sessionManager = MediaSessionManager(applicationContext)
		notificationManager = MediaNotificationManager(applicationContext, sessionManager.token)

		// onStartCommand wouldn't have been called at the point when the METADATA_UPDATE event is dispatched (which calls updateState)
		// so a manual call is necessary
		updateState(isBootstrapping = true)

		updateEqualizer() // restore its state
		sendEqualizerInfo(isBootstrapping = true)
	}

	override fun onDestroy() {
		equalizer.release()
		player.release() // destroy the Player instance
		sessionManager.release() // and the media session
		audioFocusHandler.abandon() // ...and the audio focus

		timer.cancelSafe()
		timer = null

		unregisterReceiverSafe(noisyReceiver)

		State.playbackServiceReady = false // clear the static...extremely important

		super.onDestroy()
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
	private fun updateState(isBootstrapping: Boolean = false) {
		if (!State.track.exists) return

		sendTrackUpdate(State.track.path, false)
		updateSeek(State.track.seek)
		if (isBootstrapping) EventBus.dispatch(Event(Type.SEEK_UPDATE, TARGET)) // notify the session
	}
	private fun sendTrackUpdate(path: String, updatePlaylist: Boolean = true) {
		player.prepareSource(path)

		// update playlist
		if (updatePlaylist || State.playlist.isEmpty()) State.playlist.update(path)
		State.playlist.updateIndex(path)
		EventBus.dispatch(Event(Type.PLAYLIST_UPDATE, TARGET, State.playlist.serialize()))
	}

	// playback
	private fun playTrack(path: String?, updatePlaylist: Boolean = true) {
		if (path == null) return

		sendTrackUpdate(path, updatePlaylist)

		val focusResult = audioFocusHandler.request()
		if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

		registerReceiver(noisyReceiver, noisyIntentFilter) // headphone removal

		player.playPause(true)
		sendMetadataUpdate(path)
		sendPeriodicSeekUpdates()
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
		sendPeriodicSeekUpdates(play)
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
	private fun sendPeriodicSeekUpdates(toggle: Boolean = true) {
		timer.cancelSafe()

		if (!toggle) return

		timer = Timer().apply {
			schedule(object : TimerTask() {
				override fun run() {
					sendSingleSeekUpdate()
				}

			}, 0L, SEEK_UPDATE_PERIOD)
		}
	}
	private fun sendSingleSeekUpdate() {
		State.track.seek = player.currentPositionSafe
		EventBus.dispatch(Event(Type.SEEK_TICK, TARGET, mapOf("seek" to State.track.seek)))
	}

	private fun fastForward() {
		player.seekTo(player.currentPositionSafe + State.settings.seekJump)
		sendSingleSeekUpdate()
	}
	private fun rewind() {
		player.seekTo(player.currentPositionSafe - State.settings.seekJump)
		sendSingleSeekUpdate()
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

	// metadata
	private fun sendMetadataUpdate(path: String) {
		State.track.update(path)
		EventBus.dispatch(Event(Type.METADATA_UPDATE, TARGET, State.track.serialize()))
	}

	// equalizer
	private fun updateEqualizer() {
		updateEqualizerPreset(State.settings.equalizerPreset)
		State.settings.equalizerBands.withIndex().forEach {
			updateEqualizerBand(it.index.toShort(), it.value)
		}
	}
	private fun updateEqualizerPreset(preset: Short) {
		State.settings.equalizerPreset = preset
		equalizer.usePreset(preset)
		sendEqualizerInfo()
	}
	private fun updateEqualizerBand(band: Short, level: Short) {
		State.settings.equalizerBands[band.toInt()] = level
		equalizer.setBandLevel(band, level)
	}
	private fun sendEqualizerInfo(isBootstrapping: Boolean = false) {
		if (isBootstrapping) {
			Moirai.BG.postDelayed({
				EventBus.dispatch(Event(Type.EQUALIZER_INFO, TARGET, equalizer.getInfo()))

			}, 1000)
		}
		else EventBus.dispatch(Event(Type.EQUALIZER_INFO, TARGET, equalizer.getInfo()))
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
	@Suppress("UNCHECKED_CAST")
	override fun handle(event: Event) {
		if (event.target == TARGET) return

		when (event.type) {
			Type.APP_FOREGROUNDED -> startForegroundSafe()

			Type.METADATA_UPDATE -> updateState()

			Type.PLAY_TRACK -> playTrack(event.data["path"].toString())
			Type.PLAY -> playPause(true)
			Type.PAUSE -> playPause(false)
			Type.SEEK_UPDATE -> updateSeek(event.data["seek"].toString().toInt())
			Type.FF -> fastForward()
			Type.RW -> rewind()
			Type.PLAY_PREV -> playPrev()
			Type.PLAY_NEXT -> playNext()

			Type.QUEUE_PLAY_SELECTED -> {
				State.playlist.update(event.data["tracks"] as ArrayList<String>)
				EventBus.dispatch(Event(Type.PLAYLIST_UPDATE, Target.ACTIVITY, mapOf("files" to State.playlist.serialize())))
				playTrack(State.playlist.getTrackByIndex(0), false)
			}
			Type.QUEUE_ADD_SELECTED -> {
				State.playlist.update(event.data["tracks"] as ArrayList<String>, true)
				EventBus.dispatch(Event(Type.PLAYLIST_UPDATE, Target.ACTIVITY, State.playlist.serialize()))
			}

			Type.TOGGLE_SHUFFLE -> State.settings.shuffle = event.data["value"].toString().toBoolean()
			Type.TOGGLE_REPEAT -> State.settings.repeat = event.data["value"].toString().toInt()
			Type.PLAYBACK_SPEED_CHANGE -> updatePlaybackSpeed(State.settings.playbackSpeed)

			Type.SLEEP_TIMER_TOGGLE -> {
				val active = event.data["value"].toString().toBoolean()
				if (active) SleepTimer.start(State.settings.sleepTimer.toLong())
				else SleepTimer.cancel()
			}

			Type.EQUALIZER_PRESET_CHANGE -> updateEqualizerPreset(event.data["value"].toString().toShort()) // TODO need to send equalizer info again??
			Type.EQUALIZER_BAND_CHANGE -> updateEqualizerBand(event.data["band"].toString().toShort(), event.data["value"].toString().toShort())
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

		private fun registerSelf(i: PlaybackManager) {
			instance = i
		}
	}
}

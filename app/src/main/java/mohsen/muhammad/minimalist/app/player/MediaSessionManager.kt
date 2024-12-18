package mohsen.muhammad.minimalist.app.player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import mohsen.muhammad.minimalist.core.Moirai
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.putEncodedBitmap
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent

/**
 * Created by muhammad.mohsen on 5/11/2019.
 * https://developer.android.com/guide/topics/media-apps/working-with-a-media-session
 * https://developer.android.com/guide/topics/media-apps/mediabuttons
 * Thanks, Google!!
 */
class MediaSessionManager(context: Context): MediaSessionCompat.Callback(), EventBus.Subscriber {

	private var stateBuilder: PlaybackStateCompat.Builder
	private var metadataBuilder: MediaMetadataCompat.Builder

	private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, MediaSessionManager::javaClass.name).apply {
		metadataBuilder = MediaMetadataCompat.Builder()
		stateBuilder = PlaybackStateCompat.Builder()
		stateBuilder.setActions(SUPPORTED_ACTIONS) // actions
		isActive = true

		setCallback(this@MediaSessionManager)

		EventBus.subscribe(this@MediaSessionManager)
	}

	val token: MediaSessionCompat.Token = mediaSession.sessionToken

	fun release() {
		mediaSession.isActive = false
		mediaSession.release()
	}

	// media button callbacks
	override fun onPlay() {
		super.onPlay()
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.PLAY))
	}
	override fun onPause() {
		super.onPause()
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.PAUSE))
	}
	override fun onStop() {
		super.onStop()
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.PAUSE))
	}
	override fun onSkipToNext() {
		super.onSkipToNext()
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.PLAY_NEXT))
	}
	override fun onSkipToPrevious() {
		super.onSkipToPrevious()
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.PLAY_PREVIOUS))
	}

	override fun onSeekTo(pos: Long) {
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.SEEK_UPDATE, pos.toString()))
		receive(SystemEvent(EventSource.SESSION, EventType.SEEK_UPDATE_USER))

	}

	override fun receive(data: EventBus.EventData) {
		if (data !is SystemEvent) return
		if (!SUPPORTED_EVENTS.contains(data.type)) return

		// state
		when (data.type) {
			EventType.PLAY -> stateBuilder.setState(PLAYING, State.Track.seek.toLong(), State.playbackSpeed)
			EventType.PAUSE -> stateBuilder.setState(PAUSED, State.Track.seek.toLong(), 0F)

			EventType.PLAY_NEXT, EventType.PLAY_PREVIOUS, EventType.PLAY_SELECTED, EventType.PLAY_ITEM ->
				stateBuilder.setState(PLAYING, 0, State.playbackSpeed)

			EventType.SEEK_UPDATE_USER, EventType.PLAYBACK_SPEED ->
				stateBuilder.setState(if (State.isPlaying) PLAYING else PAUSED, State.Track.seek.toLong(), if (State.isPlaying) State.playbackSpeed else 0F)
		}
		mediaSession.setPlaybackState(stateBuilder.build())

		// there was a check here that prevented updating the metadata except for a couple of events
		// not updating the metadata, I think, causes an issue where the notification seekbar stops working (on Android 10, at least)
		// to repro, select a track with album art, close the app, open it again, seek from the app's UI, play, check the notification
		if (!intArrayOf(EventType.METADATA_UPDATE, EventType.PLAY, EventType.SEEK_UPDATE_USER).contains(data.type)) return

		// metadata
		Moirai.BG.post {
			metadataBuilder.apply {
				putString(MediaMetadataCompat.METADATA_KEY_TITLE, State.Track.title)
				putString(MediaMetadataCompat.METADATA_KEY_ARTIST, State.Track.artist)
				putString(MediaMetadataCompat.METADATA_KEY_ALBUM, State.Track.album)
				putLong(MediaMetadataCompat.METADATA_KEY_DURATION, State.Track.duration)
				putEncodedBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, State.Track.albumArt) // that's why a background thread is used
			}

			mediaSession.isActive = true
			mediaSession.setMetadata(metadataBuilder.build()) // will this blow up in my face??...it didn't
		}
	}

	companion object {
		const val PLAYING = PlaybackStateCompat.STATE_PLAYING
		const val PAUSED = PlaybackStateCompat.STATE_PAUSED

		private const val SUPPORTED_ACTIONS = PlaybackStateCompat.ACTION_PLAY or
				PlaybackStateCompat.ACTION_PLAY_PAUSE or
				PlaybackStateCompat.ACTION_PAUSE or
				PlaybackStateCompat.ACTION_STOP or
				PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
				PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
				PlaybackStateCompat.ACTION_SEEK_TO

		private val SUPPORTED_EVENTS = arrayOf(EventType.PLAY_NEXT,
				EventType.METADATA_UPDATE,
				EventType.PLAY_PREVIOUS,
				EventType.PLAY_SELECTED,
				EventType.PLAY_ITEM,
				EventType.PLAY,
				EventType.PAUSE,
				EventType.SEEK_UPDATE_USER)
	}
}

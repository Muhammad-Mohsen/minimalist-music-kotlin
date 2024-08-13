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
		updatePlaybackState(EventType.SEEK_UPDATE, pos)
		EventBus.send(SystemEvent(EventSource.SESSION, EventType.SEEK_UPDATE, pos.toString()))
	}

	private fun updatePlaybackState(state: Int, seek: Long = 0) {
		// state
		when (state) {
			EventType.PLAY_NEXT, // needs metadata update
			EventType.PLAY_PREVIOUS, // needs metadata update
			EventType.PLAY,
			EventType.PLAY_SELECTED,
			EventType.PLAY_ITEM -> stateBuilder.setState(PLAYING, State.Track.seek.toLong(), 1F) // needs metadata update
			EventType.PAUSE -> stateBuilder.setState(PAUSED, State.Track.seek.toLong(), 0F)
			EventType.SEEK_UPDATE -> stateBuilder.setState(if (State.isPlaying) PLAYING else PAUSED, seek, if (State.isPlaying) 1F else 0F)
		}
		mediaSession.setPlaybackState(stateBuilder.build())

		if (!intArrayOf(EventType.METADATA_UPDATE, EventType.PLAY).contains(state)) return // only update the metadata if necessary. EventType.PLAY is needed because on startup, the playback is stopped

		// metadata (in a bg thread because of the bitmap)
		Moirai.BG.post {
			metadataBuilder.apply {
				putString(MediaMetadataCompat.METADATA_KEY_TITLE, State.Track.title)
				putString(MediaMetadataCompat.METADATA_KEY_ARTIST, State.Track.artist)
				putString(MediaMetadataCompat.METADATA_KEY_ALBUM, State.Track.album)
				putLong(MediaMetadataCompat.METADATA_KEY_DURATION, State.Track.duration)
				putEncodedBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, State.Track.albumArt)
			}

			mediaSession.isActive = true
			mediaSession.setMetadata(metadataBuilder.build()) // will this blow up in my face??...it didn't
		}
	}

	override fun receive(data: EventBus.EventData) {
		if (data !is SystemEvent) return
		when (data.type) {
			EventType.PLAY,
			EventType.PAUSE,
			EventType.PLAY_ITEM,
			EventType.PLAY_NEXT,
			EventType.PLAY_PREVIOUS,
			EventType.PLAY_SELECTED,
			EventType.METADATA_UPDATE -> updatePlaybackState(data.type)
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
	}
}

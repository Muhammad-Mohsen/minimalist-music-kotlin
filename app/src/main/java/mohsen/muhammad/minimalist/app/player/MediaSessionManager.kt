package mohsen.muhammad.minimalist.app.player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import mohsen.muhammad.minimalist.core.evt.EventBus
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

		stateBuilder = PlaybackStateCompat.Builder()
		metadataBuilder = MediaMetadataCompat.Builder()

		setCallback(this@MediaSessionManager)
		isActive = true

		EventBus.subscribe(this@MediaSessionManager)
	}

	val token: MediaSessionCompat.Token
		get() = mediaSession.sessionToken

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

	private fun updatePlaybackState(state: Int) {
		// set actions
		stateBuilder.setActions(SUPPORTED_ACTIONS)

		// update state
		when (state) {
			EventType.PLAY_NEXT,
			EventType.PLAY_PREVIOUS,
			EventType.PLAY,
			EventType.PLAY_ITEM -> stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, State.Track.seek.toLong(), 1F)
			EventType.PAUSE -> stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, State.Track.seek.toLong(), 0F)
		}

		mediaSession.setPlaybackState(stateBuilder.build())

		// set metadata
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, State.Track.title)
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, State.Track.artist)
		metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, State.Track.album)

		mediaSession.setMetadata(metadataBuilder.build())
	}

	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent) {
			when (data.type) {
				EventType.PLAY,
				EventType.PAUSE,
				EventType.PLAY_ITEM,
				EventType.PLAY_NEXT,
				EventType.PLAY_PREVIOUS,
				EventType.METADATA_UPDATE -> updatePlaybackState(data.type)
			}
		}
	}

	companion object {
		private const val SUPPORTED_ACTIONS = PlaybackStateCompat.ACTION_PLAY or
					PlaybackStateCompat.ACTION_PLAY_PAUSE or
					PlaybackStateCompat.ACTION_PAUSE or
					PlaybackStateCompat.ACTION_STOP or
					PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
					PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
	}
}

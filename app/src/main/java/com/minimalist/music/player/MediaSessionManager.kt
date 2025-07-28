package com.minimalist.music.player

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.data.state.State
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Type
import com.minimalist.music.foundation.EventBus.Target

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
		EventBus.dispatch(Event(Type.PLAY, Target.SESSION))
	}
	override fun onPause() {
		super.onPause()
		EventBus.dispatch(Event(Type.PAUSE, Target.SESSION))
	}
	override fun onStop() {
		super.onStop()
		EventBus.dispatch(Event(Type.PAUSE, Target.SESSION))
	}
	override fun onSkipToNext() {
		super.onSkipToNext()
		EventBus.dispatch(Event(Type.PLAY_NEXT, Target.SESSION))
	}
	override fun onSkipToPrevious() {
		super.onSkipToPrevious()
		EventBus.dispatch(Event(Type.PLAY_PREV, Target.SESSION))
	}

	override fun onFastForward() {
		super.onFastForward()
		EventBus.dispatch(Event(Type.FF, Target.SESSION))
	}

	override fun onRewind() {
		super.onRewind()
		EventBus.dispatch(Event(Type.RW, Target.SESSION))
	}

	override fun onSeekTo(pos: Long) {
		EventBus.dispatch(Event(Type.SEEK_UPDATE, Target.SESSION, mapOf("seek" to pos)))
	}

	override fun handle(event: Event) {
		// state
		when (event.type) {
			Type.PLAY -> stateBuilder.setState(PLAYING, State.track.seek.toLong(), State.settings.playbackSpeed)
			Type.PAUSE -> stateBuilder.setState(PAUSED, State.track.seek.toLong(), 0F)

			Type.PLAY_NEXT, Type.PLAY_PREV, Type.QUEUE_PLAY_SELECTED, Type.PLAY_TRACK ->
				stateBuilder.setState(PLAYING, 0, State.settings.playbackSpeed)

			Type.SEEK_UPDATE, Type.PLAYBACK_SPEED_CHANGE ->
				stateBuilder.setState(if (State.isPlaying) PLAYING else PAUSED, State.track.seek.toLong(), if (State.isPlaying) State.settings.playbackSpeed else 0F)
		}
		mediaSession.setPlaybackState(stateBuilder.build())

		// there was a check here that prevented updating the metadata except for a couple of events
		// not updating the metadata, I think, causes an issue where the notification seekbar stops working (on Android 10, at least)
		// to repro, select a track with album art, close the app, open it again, seek from the app's UI, play, check the notification
		if (!arrayOf(Type.METADATA_UPDATE, Type.PLAY, Type.SEEK_UPDATE, Type.SEEK_UPDATE).contains(event.type)) return

		// metadata
		metadataBuilder.apply {
			putString(MediaMetadataCompat.METADATA_KEY_TITLE, State.track.name)
			putString(MediaMetadataCompat.METADATA_KEY_ARTIST, State.track.artist)
			putString(MediaMetadataCompat.METADATA_KEY_ALBUM, State.track.album)
			putLong(MediaMetadataCompat.METADATA_KEY_DURATION, State.track.duration)
			putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, State.track.albumArt?.decoded)
		}

		mediaSession.isActive = true
		mediaSession.setMetadata(metadataBuilder.build())
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
				PlaybackStateCompat.ACTION_SEEK_TO or
				PlaybackStateCompat.ACTION_FAST_FORWARD or
				PlaybackStateCompat.ACTION_REWIND
	}
}

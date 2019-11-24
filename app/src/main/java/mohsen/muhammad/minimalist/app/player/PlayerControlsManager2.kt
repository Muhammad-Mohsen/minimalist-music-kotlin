package mohsen.muhammad.minimalist.app.player

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.media_controls_2.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.core.ext.setImageDrawable
import mohsen.muhammad.minimalist.data.*
import java.lang.ref.WeakReference

/**
 * Created by muhammad.mohsen on 12/23/2018.
 * Manages the player controls section of the UI (omni button, current track, seek, etc.)
 */

class PlayerControlsManager2(controlsStrongRef: ConstraintLayout) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference<ConstraintLayout>(controlsStrongRef)
	internal val controls: ConstraintLayout?
		get() = controlsWeakRef.get()

	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		// seek change
		controls?.seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (p2) sendSeek(p1)
			}
		})

		controls?.buttonOmni?.setOnClickListener {
			togglePlayPauseButton(!State.isPlaying) // UI
			EventBus.send(SystemEvent(EventSource.CONTROLS, if (!State.isPlaying) EventType.PLAY else EventType.PAUSE)) // Event
		}
		controls?.buttonNext?.setOnClickListener {
			togglePlayPauseButton(true)
			controls?.buttonNext?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_NEXT)) {
				controls?.buttonNext?.setImageDrawable(R.drawable.next000)
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_NEXT))
		}
		controls?.buttonPrev?.setOnClickListener {
			togglePlayPauseButton(true)
			controls?.buttonPrev?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_NEXT)) {
				controls?.buttonPrev?.setImageDrawable(R.drawable.next000)
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_PREVIOUS))
		}
		controls?.buttonRepeat?.setOnClickListener {
			controls?.buttonRepeat?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_REPEAT)) {
				controls?.buttonRepeat?.setImageDrawable(repeatIcons[State.Playlist.repeat])
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_REPEAT))
		}
		controls?.buttonShuffle?.setOnClickListener {
			controls?.buttonShuffle?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_SHUFFLE)) { // do the animation
				val shuffleIcon = if (State.Playlist.shuffle) shuffleIcons[1] else shuffleIcons[0]
				controls?.buttonShuffle?.setImageDrawable(shuffleIcon)
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_SHUFFLE))
		}
	}

	private fun updateMetadata() {
		controls?.textViewTitle?.setText(State.Track.title)

		// if the artist exists, set both album and artist (we're guaranteed album info in the form of the parent dir name)
		if (State.Track.artist.isNotEmpty()) controls?.textViewSubtitle?.setText(controls?.context?.getString(R.string.trackAlbumArtist, State.Track.album, State.Track.artist))
		// if there's no artist info, only set the album
		else controls?.textViewSubtitle?.setText(State.Track.album)

		controls?.textViewDuration?.text = State.Track.readableDuration

		controls?.seekBar?.max = State.Track.duration.toInt()
		controls?.seekBar?.progress = State.Track.seek
		controls?.textViewSeek?.text = State.Track.readableSeek

		controls?.buttonRepeat?.setImageDrawable(repeatIcons[State.Playlist.repeat])
		controls?.buttonShuffle?.setImageDrawable(if (State.Playlist.shuffle) shuffleIcons[1] else shuffleIcons[0])
	}

	private fun updateSeek() {
		controls?.seekBar?.progress = State.Track.seek
		controls?.textViewSeek?.text = State.Track.readableSeek
	}

	private fun sendSeek(seek: Int) {
		EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.SEEK_UPDATE, seek.toString()))
	}

	private fun togglePlayPauseButton(play: Boolean) {
		val animId = if (!play) R.drawable.anim_pause_play else R.drawable.anim_play_pause

		if (controls?.buttonOmni?.tag == animId) return // if the same animation is shown, do nothing

		controls?.buttonOmni?.animateDrawable(animId)
		controls?.buttonOmni?.tag = animId // set the tag
	}

	private fun getButtonAnimationByIndex(buttonIndex: Int): Int {
		return when (buttonIndex) {
			FabMenu.BUTTON_NEXT -> R.drawable.anim_next
			FabMenu.BUTTON_REPEAT -> {
				repeatAnimations[(State.Playlist.repeat + 1) % repeatAnimations.size]
			}
			FabMenu.BUTTON_SHUFFLE -> {
				if (State.Playlist.shuffle) R.drawable.anim_shuffle_inactive
				else R.drawable.anim_shuffle_active
			}
			else -> R.drawable.anim_next // FabMenu.BUTTON_PREV
		}
	}

	override fun receive(data: EventBus.EventData) {

		// make sure we're running on main
		Handler(Looper.getMainLooper()).post {

			if (data !is SystemEvent) return@post // not interested in event types other then SystemEvent
			if (data.source == EventSource.CONTROLS) return@post // not interested in events that were sent from here

			when (data.type) {
				EventType.PLAY, EventType.PLAY_ITEM, EventType.PLAY_NEXT, EventType.PLAY_PREVIOUS -> togglePlayPauseButton(true) // show the pause icon
				EventType.PAUSE -> togglePlayPauseButton(false)
				EventType.METADATA_UPDATE -> updateMetadata()
				EventType.SEEK_UPDATE -> updateSeek()
			}

		}
	}
}

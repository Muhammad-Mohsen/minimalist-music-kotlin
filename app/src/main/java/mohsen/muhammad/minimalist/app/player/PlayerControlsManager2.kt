package mohsen.muhammad.minimalist.app.player

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.core.ext.context
import mohsen.muhammad.minimalist.core.ext.resources
import mohsen.muhammad.minimalist.core.ext.setImageDrawable
import mohsen.muhammad.minimalist.data.*
import mohsen.muhammad.minimalist.databinding.MediaControls2Binding
import java.lang.ref.WeakReference

/**
 * Created by muhammad.mohsen on 12/23/2018.
 * Manages the player controls section of the UI (omni button, current track, seek, etc.)
 */

class PlayerControlsManager2(controlsStrongRef: ConstraintLayout) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference(controlsStrongRef)
	private val binding: MediaControls2Binding?
		get() {
			val nullSafeControls = controlsWeakRef.get() ?: return null
			return MediaControls2Binding.bind(nullSafeControls)
		}

	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		// seek change
		binding?.seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (p2) sendSeek(p1)
			}
		})

		binding?.buttonOmni?.setOnClickListener {
			togglePlayPauseButton(!State.isPlaying) // UI
			EventBus.send(SystemEvent(EventSource.CONTROLS, if (!State.isPlaying) EventType.PLAY else EventType.PAUSE)) // Event
		}
		binding?.buttonNext?.setOnClickListener {
			togglePlayPauseButton(true)
			binding?.buttonNext?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_NEXT)) {
				binding?.buttonNext?.setImageDrawable(R.drawable.next000)
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_NEXT))
		}
		binding?.buttonPrev?.setOnClickListener {
			togglePlayPauseButton(true)
			binding?.buttonPrev?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_NEXT)) {
				binding?.buttonPrev?.setImageDrawable(R.drawable.next000)
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_PREVIOUS))
		}
		// quick and dirty, but it's better than nothing
		binding?.buttonNext?.setOnLongClickListener {
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.FF))
			return@setOnLongClickListener true
		}
		binding?.buttonPrev?.setOnLongClickListener {
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.RW))
			return@setOnLongClickListener true
		}
		binding?.buttonRepeat?.setOnClickListener {
			binding?.buttonRepeat?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_REPEAT)) {
				binding?.buttonRepeat?.setImageDrawable(repeatIcons[State.playlist.repeat])
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_REPEAT))
		}
		binding?.buttonShuffle?.setOnClickListener {
			binding?.buttonShuffle?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_SHUFFLE)) { // do the animation
				val shuffleIcon = if (State.playlist.shuffle) shuffleIcons[1] else shuffleIcons[0]
				binding?.buttonShuffle?.setImageDrawable(shuffleIcon)
			}
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_SHUFFLE))
		}
	}

	private fun updateMetadata() {
		binding?.textViewTitle?.setText(State.Track.title)

		// if the artist exists, set both album and artist (we're guaranteed album info in the form of the parent dir name)
		if (State.Track.artist.isNotEmpty()) binding?.textViewSubtitle?.setText(binding?.resources?.getString(R.string.trackAlbumArtist, State.Track.album, State.Track.artist))
		// if there's no artist info, only set the album
		else binding?.textViewSubtitle?.setText(State.Track.album)

		binding?.textViewDuration?.text = State.Track.readableDuration

		binding?.seekBar?.max = State.Track.duration.toInt()
		binding?.seekBar?.progress = State.Track.seek
		binding?.textViewSeek?.text = State.Track.readableSeek

		binding?.buttonRepeat?.setImageDrawable(repeatIcons[State.playlist.repeat])
		binding?.buttonShuffle?.setImageDrawable(if (State.playlist.shuffle) shuffleIcons[1] else shuffleIcons[0])
	}

	private fun updateSeek() {
		binding?.seekBar?.progress = State.Track.seek
		binding?.textViewSeek?.text = State.Track.readableSeek
	}

	private fun sendSeek(seek: Int) {
		EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.SEEK_UPDATE, seek.toString()))
	}

	private fun togglePlayPauseButton(play: Boolean) {
		val animId = if (!play) R.drawable.anim_pause_play else R.drawable.anim_play_pause

		if (binding?.buttonOmni?.tag == animId) return // if the same animation is shown, do nothing

		binding?.buttonOmni?.animateDrawable(animId)
		binding?.buttonOmni?.tag = animId // set the tag
	}

	private fun getButtonAnimationByIndex(buttonIndex: Int): Int {
		return when (buttonIndex) {
			FabMenu.BUTTON_NEXT -> R.drawable.anim_next
			FabMenu.BUTTON_REPEAT -> {
				repeatAnimations[(State.playlist.repeat + 1) % repeatAnimations.size]
			}
			FabMenu.BUTTON_SHUFFLE -> {
				if (State.playlist.shuffle) R.drawable.anim_shuffle_inactive
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
				EventType.PLAY, EventType.PLAY_ITEM, EventType.PLAY_NEXT, EventType.PLAY_PREVIOUS, EventType.PLAY_SELECTED -> togglePlayPauseButton(true) // show the pause icon
				EventType.PAUSE -> togglePlayPauseButton(false)
				EventType.METADATA_UPDATE -> updateMetadata()
				EventType.SEEK_UPDATE -> updateSeek()
			}

		}
	}
}

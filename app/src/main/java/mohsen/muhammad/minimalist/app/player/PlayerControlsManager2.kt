package mohsen.muhammad.minimalist.app.player

import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.SeekBar
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.*
import mohsen.muhammad.minimalist.data.*
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding
import mohsen.muhammad.minimalist.databinding.MediaControls2Binding
import java.lang.ref.WeakReference

/**
 * Created by muhammad.mohsen on 12/23/2018.
 * Manages the player controls section of the UI (omni button, current track, seek, etc.)
 */

class PlayerControlsManager2(binding: MainFragmentBinding) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference(binding.layoutControls2.root)
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
			binding?.buttonNext?.animateDrawable(R.drawable.anim_next)
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_NEXT))
		}
		binding?.buttonPrev?.setOnClickListener {
			togglePlayPauseButton(true)
			binding?.buttonPrev?.animateDrawable(R.drawable.anim_next)
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
			binding?.buttonRepeat?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_REPEAT))
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_REPEAT))
		}
		binding?.buttonShuffle?.setOnClickListener {
			binding?.buttonShuffle?.animateDrawable(getButtonAnimationByIndex(FabMenu.BUTTON_SHUFFLE))
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.CYCLE_SHUFFLE))
		}
		binding?.buttonAlbumArt?.setOnClickListener {
			binding?.buttonAlbumArt?.let {
				val animId = if (it.tag == R.drawable.anim_collapse_expand) R.drawable.anim_expand_collapse else R.drawable.anim_collapse_expand

				val art = (binding?.imageViewAlbumArt?.drawable as BitmapDrawable).bitmap
				val height = if (it.tag == R.drawable.anim_collapse_expand) Const.Dimen.ALBUM_ART_COLLAPSED.toDip(it.context)
				else art.height * (binding?.imageViewAlbumArt?.width ?: 1) / art.width

				it.tag = animId
				it.animateDrawable(animId)
				binding?.mainPanel?.animateHeight(height.toInt(), 210)
			}
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

		binding?.buttonAlbumArt?.visibility = if (State.Track.albumArt != null) View.VISIBLE else View.GONE
		binding?.imageViewAlbumArt?.setEncodedBitmapAsync(State.Track.albumArt) // album art

		// thanks https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0
		// updateChapters uses the container's (frameLayoutChapters) width to determine the margins. On app startup, when this is called,
		// the container is not yet laid out, and thus, width returns 0, so we just post it after
		binding?.frameLayoutChapters?.post {
			updateChapters(binding?.frameLayoutChapters!!)
		}
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
		EventBus.main.post {

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

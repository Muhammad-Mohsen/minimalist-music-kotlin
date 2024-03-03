package mohsen.muhammad.minimalist.app.player

import android.graphics.drawable.BitmapDrawable
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.SeekBar
import androidx.core.text.bold
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.Moirai
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.core.ext.animateHeight
import mohsen.muhammad.minimalist.core.ext.setEncodedBitmapAsync
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding

/**
 * Created by muhammad.mohsen on 12/23/2018.
 * Manages the player controls section of the UI (omni button, current track, seek, etc.)
 */

class PlayerControlsManager2(mainBinding: MainFragmentBinding) : EventBus.Subscriber {

	private val binding = mainBinding.layoutControls2

	// registers event handlers
	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		// seek change
		binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (p2) sendSeek(p1)
			}
		})

		binding.buttonOmni.setOnClickListener {
			togglePlayPauseButton(!State.isPlaying) // UI
			EventBus.send(SystemEvent(EventSource.CONTROLS, if (!State.isPlaying) EventType.PLAY else EventType.PAUSE)) // Event
		}
		binding.buttonNext.setOnClickListener {
			togglePlayPauseButton(true)
			binding.buttonNext.animateDrawable(R.drawable.anim_next)
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_NEXT))
		}
		binding.buttonPrev.setOnClickListener {
			togglePlayPauseButton(true)
			binding.buttonPrev.animateDrawable(R.drawable.anim_next)
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.PLAY_PREVIOUS))
		}

		binding.buttonNext.setOnLongClickListener {
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.FF))
			return@setOnLongClickListener true
		}
		binding.buttonPrev.setOnLongClickListener {
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.RW))
			return@setOnLongClickListener true
		}

		binding.buttonSearch.setOnClickListener {
			State.isSearchModeActive = true
			EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.SEARCH_MODE))
		}
		binding.buttonAlbumArt.setOnClickListener {
			binding.buttonAlbumArt.let {
				val animId = if (it.tag == R.drawable.anim_collapse_expand) R.drawable.anim_expand_collapse else R.drawable.anim_collapse_expand

				val art = (binding.imageViewAlbumArt.drawable as BitmapDrawable).bitmap
				val height = if (it.tag == R.drawable.anim_collapse_expand) it.resources.getDimension(R.dimen.albumArtCollapsedHeight)
				else art.height * (binding.imageViewAlbumArt.width) / art.width

				it.tag = animId
				it.animateDrawable(animId)
				binding.mainPanel.animateHeight(height.toInt(), 500)
			}
		}
	}

	private fun updateMetadata() {
		binding.textViewTitle.setText(State.Track.title)

		// if the artist exists, set both album and artist (we're guaranteed album info in the form of the parent dir name)
		binding.textViewSubtitle.setText(SpannableStringBuilder()
			.bold { append(State.Track.album) }
			.append(if (State.Track.artist.isNotEmpty()) " | ${State.Track.artist}" else ""))

		binding.textViewDuration.text = State.Track.readableDuration

		binding.seekBar.max = State.Track.duration.toInt()
		binding.seekBar.progress = State.Track.seek
		binding.textViewSeek.text = State.Track.readableSeek

		binding.buttonAlbumArt.visibility = if (State.Track.albumArt != null) View.VISIBLE else View.GONE
		binding.imageViewAlbumArt.setEncodedBitmapAsync(State.Track.albumArt) // album art

		// collapse the album art panel if the new file doesn't have any
		if (State.Track.albumArt == null && binding.buttonAlbumArt.tag == R.drawable.anim_collapse_expand) {
			binding.buttonAlbumArt.let {
				it.tag = R.drawable.anim_expand_collapse
				it.animateDrawable(R.drawable.anim_expand_collapse)
				binding.mainPanel.animateHeight(it.resources.getDimension(R.dimen.albumArtCollapsedHeight).toInt(), ALBUM_ART_ANIM_DURATION)
			}
		}

		// thanks https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0
		// updateChapters uses the container's (frameLayoutChapters) width to determine the margins. On app startup, when this is called,
		// the container is not yet laid out, and thus, width returns 0, so we just post it after
		binding.frameLayoutChapters.post {
			updateChapters(binding.frameLayoutChapters)
		}
	}

	private fun updateSeek() {
		binding.seekBar.progress = State.Track.seek
		binding.textViewSeek.text = State.Track.readableSeek
	}
	private fun sendSeek(seek: Int) {
		EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.SEEK_UPDATE, seek.toString()))
	}

	private fun togglePlayPauseButton(play: Boolean) {
		val animId = if (!play) R.drawable.anim_pause_play else R.drawable.anim_play_pause

		if (binding.buttonOmni.tag == animId) return // if the same animation is shown, do nothing

		binding.buttonOmni.animateDrawable(animId)
		binding.buttonOmni.tag = animId // set the tag
	}

	override fun receive(data: EventBus.EventData) {

		// make sure we're running on main
		Moirai.MAIN.post {

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

	companion object {
		private const val ALBUM_ART_ANIM_DURATION = 210L
	}

}

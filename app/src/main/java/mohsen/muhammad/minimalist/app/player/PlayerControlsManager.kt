package mohsen.muhammad.minimalist.app.player

import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.media_controls.view.*
import kotlinx.android.synthetic.main.media_information.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.data.PlaybackEvent
import mohsen.muhammad.minimalist.data.PlaybackEventSource
import mohsen.muhammad.minimalist.data.PlaybackEventType
import java.lang.ref.WeakReference

/**
 * Created by muhammad.mohsen on 12/23/2018.
 */

class PlayerControlsManager(controlsStrongRef: FrameLayout) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference<FrameLayout>(controlsStrongRef)
	private val controls: FrameLayout?
		get() = controlsWeakRef.get()

	private val toPlay
		get() = controls?.buttonPlayPause?.tag == R.drawable.anim_play_pause

	fun initialize() {

		EventBus.subscribe(this)

		controls?.buttonPlayPause?.setOnClickListener {
			togglePlayPauseUi(toPlay)

			// dispatch the event
			val eventType = if (toPlay) PlaybackEventType.PLAY else PlaybackEventType.PAUSE
			EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, eventType))
		}

		controls?.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

			private var initiatedExternally: Boolean = false // indicates whether the seek change was initiated externally (by a human)

			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (p2) sendSeek(p1)
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {}
			override fun onStopTrackingTouch(p0: SeekBar?) {}
		})
	}

	private fun togglePlayPauseUi(play: Boolean) {

		if (toPlay == !play) return // if the same icon is shown, don't do anything

		controls?.buttonPlayPause?.animateDrawable(if (play) R.drawable.anim_pause_play else R.drawable.anim_play_pause)
	}

	private fun updateProgress(progressData: String) {
		val progress = progressData.split(";")

		controls?.textViewSeek?.text = progress[1]
		controls?.seekBar?.progress = progress[0].toInt()
	}

	private fun updateMetadata(metadataString: String) {
		val metadata = metadataString.split(";")

		controls?.textViewTitle?.text = metadata[0]
		controls?.textViewSubtitle?.text = controls?.context?.getString(R.string.trackAlbumArtist, metadata[1], metadata[2])
		controls?.textViewDuration?.text = metadata[3]

		controls?.seekBar?. max = metadata[4].toInt()
	}

	private fun sendSeek(seek: Int) {
		EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, PlaybackEventType.UPDATE_SEEK, seek.toString()))
	}

	private fun toggleFabMenu(show: Boolean) {

	}

	override fun receive(data: EventBus.EventData) {

		// make sure we're running on main
		Handler(Looper.getMainLooper()).post {

			if (data is PlaybackEvent && data.source != PlaybackEventSource.CONTROLS) { // if we're not the source
				when (data.type) {
					PlaybackEventType.PLAY, PlaybackEventType.PLAY_ITEM -> togglePlayPauseUi(false) // show the pause icon
					PlaybackEventType.UPDATE_METADATA -> updateMetadata(data.extras)
					PlaybackEventType.UPDATE_SEEK -> updateProgress(data.extras)
				}
			}

		}
	}
}

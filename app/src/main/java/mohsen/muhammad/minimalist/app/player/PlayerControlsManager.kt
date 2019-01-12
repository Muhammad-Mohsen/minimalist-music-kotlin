package mohsen.muhammad.minimalist.app.player

import android.widget.FrameLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.media_controls.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.animateDrawable

/**
 * Created by muhammad.mohsen on 12/23/2018.
 */

class PlayerControlsManager(private val controls: FrameLayout) {

	private var toPlay = false
		get() = controls.buttonPlayPause.tag == R.drawable.anim_play_pause

	fun initialize() {
		controls.buttonPlayPause.setOnClickListener {
			// Do stuff

			togglePlayPause(toPlay)
		}

		controls.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

			private var initiatedExternally: Boolean = false // indicates whether the seek change was initiated externally (by a human)

			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				// TODO update the service if externally initiated
				// PlayerService.instance?.updateSeek()
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {
				initiatedExternally = true
			}

			override fun onStopTrackingTouch(p0: SeekBar?) {
				initiatedExternally = false
			}

		})
	}

	private fun togglePlayPause(play: Boolean) {
		controls.buttonPlayPause.animateDrawable(if (play) R.drawable.anim_pause_play else R.drawable.anim_play_pause)
	}

	fun updateProgress(current: Float, total: Float) {

	}

	fun toggleFabMenu(show: Boolean) {

	}
}

package mohsen.muhammad.minimalist.app.player

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.media_controls.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.FabMenu
import mohsen.muhammad.minimalist.data.PlaybackEvent
import mohsen.muhammad.minimalist.data.PlaybackEventSource
import mohsen.muhammad.minimalist.data.PlaybackEventType
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Created by muhammad.mohsen on 12/23/2018.
 */

class PlayerControlsManager(controlsStrongRef: ConstraintLayout) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference<ConstraintLayout>(controlsStrongRef)
	internal val controls: ConstraintLayout?
		get() = controlsWeakRef.get()

	private val handler = Handler()
	private lateinit var gestureRunnable: Runnable

	fun initialize() {

		EventBus.subscribe(this)

		// fab menu expansion animation runnable
		gestureRunnable = Runnable {
			toggleFabMenuButtons(true)
			toggleFabMenuBackground(true)

			controls?.buttonOmni?.isPressed = false
		}

		// the touch listener to rule them all
		controls?.buttonOmni?.setOnTouchListener { view, motionEvent ->

			val eventTimeDelta = abs(SystemClock.uptimeMillis() - motionEvent.downTime)

			when (motionEvent.action) {

				// DOWN
				MotionEvent.ACTION_DOWN -> {
					view.isPressed = true
					handler.postDelayed(gestureRunnable, FabMenu.DELAY) // show the fab menu/overlay (after the long press delay)
				}

				// MOVE
				MotionEvent.ACTION_MOVE -> {

					val distanceDelta = calculateFabGestureDistance(motionEvent, view) // calculate distance

					if (distanceDelta < FabMenu.GESTURE_MIN_DISTANCE) {
						toggleFabMenuButtonHighlight()
						return@setOnTouchListener true
					}

					val angleDelta = calculateFabGestureAngle(motionEvent, view) // calculate angle
					val buttonIndex = getButtonByAngle(angleDelta)

					toggleFabMenuButtonHighlight(buttonIndex)
				}

				// UP
				MotionEvent.ACTION_UP -> {
					onTouchEnded()

					if (eventTimeDelta < ViewConfiguration.getLongPressTimeout()) { // treat this as a normal click
						togglePlayPauseUi(!PlaybackManager.isPlaying)

						// dispatch the event
						val eventType = if (!PlaybackManager.isPlaying) PlaybackEventType.PLAY else PlaybackEventType.PAUSE
						EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, eventType))

					} else {

						val distanceDelta = calculateFabGestureDistance(motionEvent, view) // calculate distance
						if (distanceDelta < FabMenu.GESTURE_MIN_DISTANCE) {
							toggleFabMenuButtonHighlight()
							return@setOnTouchListener true
						}

						val angleDelta = calculateFabGestureAngle(motionEvent, view) // calculate angle
						onFabMenuButtonClick(angleDelta)
					}
				}

				// CANCEL
				MotionEvent.ACTION_CANCEL -> onTouchEnded()
			}

			return@setOnTouchListener true
		}

		// seek change
		controls?.seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				if (p2) sendSeek(p1)
			}
		})
	}

	private fun updateMetadata(metadataString: String) {
		val metadata = metadataString.split(";")

		controls?.textViewTitle?.text = metadata[0]
		controls?.textViewSubtitle?.text = controls?.context?.getString(R.string.trackAlbumArtist, metadata[1], metadata[2])
		controls?.textViewDuration?.text = metadata[3]

		controls?.seekBar?. max = metadata[4].toInt()
	}

	private fun updateSeek(progressData: String) {
		val progress = progressData.split(";")

		controls?.textViewSeek?.text = progress[1]
		controls?.seekBar?.progress = progress[0].toInt()
	}

	private fun sendSeek(seek: Int) {
		EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, PlaybackEventType.UPDATE_SEEK, seek.toString()))
	}

	private fun toggleFabMenuButtons(expand: Boolean) {
		toggleSingleFabMenuButton(controls?.buttonNext, expand)
		toggleSingleFabMenuButton(controls?.buttonPrev, expand)
		toggleSingleFabMenuButton(controls?.buttonRepeat, expand)
		toggleSingleFabMenuButton(controls?.buttonShuffle, expand)
	}

	private fun onFabMenuButtonClick(angle: Float) {
		val buttonIndex = getButtonByAngle(angle)
		val eventType = fabButtonEventMap[buttonIndex]

		// TODO animation

		if (eventType != null) EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, eventType))
	}

	private fun onTouchEnded() {
		controls?.buttonOmni?.isPressed = false

		handler.removeCallbacks(gestureRunnable) // remove the callback to show the fab menu if possible
		toggleFabMenuButtons(false) // collapse the fab menu if visible
		toggleFabMenuButtonHighlight()
		toggleFabMenuBackground(false) // hide the overlay
	}

	override fun receive(data: EventBus.EventData) {

		// make sure we're running on main
		Handler(Looper.getMainLooper()).post {

			if (data !is PlaybackEvent) return@post // not interested in event types other then PlaybackEvent
			if (data.source == PlaybackEventSource.CONTROLS) return@post // not interested in events that were sent from here

			when (data.type) {
				PlaybackEventType.PLAY, PlaybackEventType.PLAY_ITEM -> togglePlayPauseUi(true) // show the pause icon
				PlaybackEventType.UPDATE_METADATA -> updateMetadata(data.extras)
				PlaybackEventType.UPDATE_SEEK -> updateSeek(data.extras)
			}

		}
	}
}

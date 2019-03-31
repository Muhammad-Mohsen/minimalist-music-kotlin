package mohsen.muhammad.minimalist.app.player

import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.media_controls.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnSeekBarChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.data.PlaybackEvent
import mohsen.muhammad.minimalist.data.PlaybackEventSource
import mohsen.muhammad.minimalist.data.PlaybackEventType
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Created by muhammad.mohsen on 12/23/2018.
 */

class PlayerControlsManager(controlsStrongRef: ConstraintLayout) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference<ConstraintLayout>(controlsStrongRef)
	private val controls: ConstraintLayout?
		get() = controlsWeakRef.get()

	private val toPlay
		get() = controls?.buttonOmni?.tag == R.drawable.anim_play_pause

	private val handler = Handler()
	private lateinit var gestureRunnable: Runnable

	fun initialize() {

		EventBus.subscribe(this)

		gestureRunnable = Runnable {
			toggleFabMenuButtons(true)
			toggleFabMenuBackground(true)

			controls?.buttonOmni?.isPressed = false
		}

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

					val distanceDelta = calculateFabGestureDistance(motionEvent.x, motionEvent.y, view.x, view.y) // calculate distance
					val angleDelta = calculateFabGestureAngle(motionEvent.x, motionEvent.y, view.x, view.y) // calculate angle

					// Log.d("Controls", "distanceDelta $distanceDelta")

					// TODO update menu highlight based on distance/angle deltas
				}

				// UP
				MotionEvent.ACTION_UP -> {
					onTouchEnded()

					if (eventTimeDelta < ViewConfiguration.getLongPressTimeout()) { // treat this as a normal click
						togglePlayPauseUi(toPlay)

						// dispatch the event
						val eventType = if (toPlay) PlaybackEventType.PLAY else PlaybackEventType.PAUSE
						EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, eventType))

					} else {
						val angleDelta = calculateFabGestureAngle(motionEvent.x, motionEvent.y, view.x, view.y) // calculate angle
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

	private fun togglePlayPauseUi(play: Boolean) {
		if (toPlay == !play) return // if the same icon is shown, don't do anything
		controls?.buttonOmni?.animateDrawable(if (play) R.drawable.anim_pause_play else R.drawable.anim_play_pause)
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

	private fun toggleFabMenuButtons(show: Boolean) {
		toggleSingleFabMenuButton(controls?.buttonNext, show)
		toggleSingleFabMenuButton(controls?.buttonPrev, show)
		toggleSingleFabMenuButton(controls?.buttonRepeat, show)
		toggleSingleFabMenuButton(controls?.buttonShuffle, show)
	}
	private fun toggleSingleFabMenuButton(button: View?, show: Boolean) {
		val params = button?.layoutParams as? ConstraintLayout.LayoutParams

		val expandedRadius = controls?.resources?.getDimension(R.dimen.fabMenuExpandedRadius)?.toInt() ?: 0

		val currentRadius = params?.circleRadius ?: 0
		val finalRadius = if (show) expandedRadius else 0

		if (currentRadius == finalRadius) return

		// can't imagine that creating a value animator per button is ideal
		ValueAnimator.ofInt(currentRadius, finalRadius).apply {
			duration = FabMenu.DURATION
			interpolator = AccelerateDecelerateInterpolator()
			start()
			addUpdateListener {
				params?.circleRadius = it.animatedValue as Int
				button?.layoutParams = params
			}
		}
	}

	private fun toggleFabMenuBackground(show: Boolean) {

		val params = controls?.fabMenuBackground?.layoutParams as? ConstraintLayout.LayoutParams

		val expandedDiameter = controls?.resources?.getDimension(R.dimen.fabMenuExpandedBackgroundDiameter)?.toInt() ?: 0

		val currentDiameter = params?.width ?: 0
		val finalDiameter = if (show) expandedDiameter else 0

		if (currentDiameter == finalDiameter) return

		// can't imagine that creating a value animator per button is ideal
		ValueAnimator.ofInt(currentDiameter, finalDiameter).apply {
			duration = FabMenu.DURATION
			interpolator = AccelerateDecelerateInterpolator()
			start()
			addUpdateListener {
				params?.width = it.animatedValue as Int
				params?.height = it.animatedValue as Int
				controls?.fabMenuBackground?.layoutParams = params
			}
		}
	}

	private fun calculateFabGestureAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
		val x = (x1 - x2).absoluteValue.toDouble()
		val y = (y1 - y2).absoluteValue.toDouble()

		return Math.atan2(y, x).toFloat()
	}

	private fun calculateFabGestureDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
		val x = (x1 - x2).absoluteValue.toDouble()
		val y = (y1 - y2).absoluteValue.toDouble()

		return Math.hypot(x, y).toFloat()
	}

	private fun onFabMenuButtonClick(angle: Float) {
		// TODO animation

		val eventType = when {
			FabMenu.inRange(angle, FabMenu.angleNext) -> PlaybackEventType.PLAY_NEXT
			FabMenu.inRange(angle, FabMenu.angleRepeat) -> PlaybackEventType.CYCLE_REPEAT
			FabMenu.inRange(angle, FabMenu.angleShuffle) -> PlaybackEventType.CYCLE_SHUFFLE
			else -> PlaybackEventType.PLAY_PREVIOUS // PREV
		}

		Log.d("CONTROLS", "event: $eventType")

		EventBus.send(PlaybackEvent(PlaybackEventSource.CONTROLS, eventType))
	}

	private fun onTouchEnded() {
		controls?.buttonOmni?.isPressed = false

		handler.removeCallbacks(gestureRunnable) // remove the callback to show the fab menu if possible
		toggleFabMenuButtons(false) // collapse the fab menu if visible
		toggleFabMenuBackground(false) // hide the overlay
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

	companion object {

		object FabMenu {
			val DELAY = ViewConfiguration.getTapTimeout().toLong() * 2
			const val DURATION = 200L

			val angleNext = arrayOf(360F, 343F)
			val angleRepeat = arrayOf(342F, 308F)
			val angleShuffle = arrayOf(307F, 272F)
			val anglePrev = arrayOf(271F, 255F)

			fun inRange(value: Float, arr: Array<Float>): Boolean {
				return value <= arr[0] && value >= arr[1]
			}
		}

	}
}

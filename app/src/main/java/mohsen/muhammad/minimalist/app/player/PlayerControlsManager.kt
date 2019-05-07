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
import mohsen.muhammad.minimalist.data.*
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Created by muhammad.mohsen on 12/23/2018.
 * Manages the player controls section of the UI (omni button, current track, seek, etc.)
 */

class PlayerControlsManager(controlsStrongRef: ConstraintLayout) : EventBus.Subscriber {

	// just to ensure that we don't ever leak!
	private val controlsWeakRef = WeakReference<ConstraintLayout>(controlsStrongRef)
	internal val controls: ConstraintLayout?
		get() = controlsWeakRef.get()

	// handler is used to kickoff a delayed runnable (gestureRunnable) to show the fab menu
	private val handler = Handler()
	private lateinit var gestureRunnable: Runnable

	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		// fab menu expansion animation runnable
		gestureRunnable = Runnable {
			toggleFabMenuButtonExpansion(true)
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
						togglePlayPauseButton(!PlaybackManager.isPlaying)

						// dispatch the event
						val eventType = if (!PlaybackManager.isPlaying) EventType.PLAY else EventType.PAUSE
						EventBus.send(SystemEvent(EventSource.CONTROLS, eventType))

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

	private fun updateMetadata() {
		controls?.textViewTitle?.setText(State.Track.title)
		controls?.textViewSubtitle?.setText(controls?.context?.getString(R.string.trackAlbumArtist, State.Track.album, State.Track.artist))
		controls?.textViewDuration?.text = State.Track.readableDuration

		controls?.seekBar?.max = State.Track.duration.toInt()
		controls?.seekBar?.progress = State.Track.seek
		controls?.textViewSeek?.text = State.Track.readableSeek
	}

	private fun updateSeek() {
		controls?.seekBar?.progress = State.Track.seek
		controls?.textViewSeek?.text = State.Track.readableSeek
	}

	private fun sendSeek(seek: Int) {
		EventBus.send(SystemEvent(EventSource.CONTROLS, EventType.SEEK_UPDATE, seek.toString()))
	}

	private fun onFabMenuButtonClick(angle: Float) {
		val buttonIndex = getButtonByAngle(angle)
		val eventType = fabMenuButtonEventMap[buttonIndex]

		animateFabMenuButton(buttonIndex) // animate the button (overlay animation)
		updateFabMenuUi(buttonIndex) // updates the shuffle and repeat buttons to show the correct icons (when the menu is expanded)

		if (eventType != null) EventBus.send(SystemEvent(EventSource.CONTROLS, eventType)) // dispatch the appropriate event
		if (buttonIndex == FabMenu.BUTTON_PREV || buttonIndex == FabMenu.BUTTON_NEXT) togglePlayPauseButton(true) // for next/previous buttons, do the to_play animation (if paused)
	}

	private fun onTouchEnded() {
		controls?.buttonOmni?.isPressed = false

		handler.removeCallbacks(gestureRunnable) // remove the callback to show the fab menu if possible
		toggleFabMenuButtonExpansion(false) // collapse the fab menu if visible
		toggleFabMenuButtonHighlight()
		toggleFabMenuBackground(false) // hide the overlay
	}

	override fun receive(data: EventBus.EventData) {

		// make sure we're running on main
		Handler(Looper.getMainLooper()).post {

			if (data !is SystemEvent) return@post // not interested in event types other then SystemEvent
			if (data.source == EventSource.CONTROLS) return@post // not interested in events that were sent from here

			when (data.type) {
				EventType.PLAY, EventType.PLAY_ITEM -> togglePlayPauseButton(true) // show the pause icon
				EventType.METADATA_UPDATE -> updateMetadata()
				EventType.SEEK_UPDATE -> updateSeek()
				EventType.PAUSE -> togglePlayPauseButton(false)
			}

		}
	}
}

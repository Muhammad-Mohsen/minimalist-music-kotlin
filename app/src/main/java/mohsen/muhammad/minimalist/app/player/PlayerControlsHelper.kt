package mohsen.muhammad.minimalist.app.player

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.ext.*
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.FabMenu
import mohsen.muhammad.minimalist.data.State
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.hypot

/**
 * Created by muhammad.mohsen on 3/30/2019.
 * extends PlayerControlsManager with functions that are mostly related to the FAB and FAB menu
 * storing the UI state in tags may be problematic. So far, though, that hasn't proved to be the case
 */

internal fun PlayerControlsManager.togglePlayPauseButton(play: Boolean) {
	val animId = if (!play) R.drawable.anim_pause_play else R.drawable.anim_play_pause

	if (binding?.buttonOmni?.tag == animId) return // if the same animation is shown, do nothing

	binding?.buttonOmni?.animateDrawable(animId)
	binding?.buttonOmni?.tag = animId // set the tag
}

// does the little animation when selecting one of the fab menu buttons (next, shuffle, etc.)
internal fun PlayerControlsManager.animateFabMenuButton(buttonIndex: Int) {

	// flip the thing for prev
	binding?.fabButtonAnimationOverlay?.rotation = if (buttonIndex == FabMenu.BUTTON_PREV) 180F else 0F

	// make the overlay visible
	binding?.fabButtonAnimationOverlay?.fadeIn(0L)

	// do the animation
	binding?.fabButtonAnimationOverlay?.animateDrawable(getButtonAnimationByIndex(buttonIndex)) {

		// hide the overlay (after the animation completes)
		binding?.fabButtonAnimationOverlay?.fadeOut(200L)
	}
}

// gets the proper animation by fab index (repeat/shuffle buttons have multiple animations)
internal fun PlayerControlsManager.getButtonAnimationByIndex(buttonIndex: Int): Int {
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

// updates the fab menu button iconography for shuffle and repeat buttons
internal fun PlayerControlsManager.updateFabMenuUi(buttonIndex: Int) {

	if (buttonIndex == FabMenu.BUTTON_REPEAT) {
		val updatedRepeat = (State.playlist.repeat + 1) % repeatIcons.size
		binding?.buttonRepeat?.setImageDrawable(repeatIcons[updatedRepeat])

	} else if (buttonIndex == FabMenu.BUTTON_SHUFFLE) {
		// set the tint color (active/inactive)
		val shuffleIcon = if (State.playlist.shuffle) shuffleIcons[0] else shuffleIcons[1]
		binding?.buttonShuffle?.setImageDrawable(shuffleIcon)
	}

	// otherwise, do nothing
}

// initializes FAB buttons' state (repeat and shuffle)
internal fun PlayerControlsManager.initializeFabMenuUi() {
	binding?.buttonRepeat?.setImageDrawable(repeatIcons[State.playlist.repeat])

	val shuffleIcon = if (State.playlist.shuffle) shuffleIcons[1] else shuffleIcons[0]
	binding?.buttonShuffle?.setImageDrawable(shuffleIcon)
}

// expands/collapses the actual fab menu buttons
internal fun PlayerControlsManager.toggleFabMenuButtonExpansion(show: Boolean) {
	val buttons = arrayOf(binding?.buttonNext, binding?.buttonRepeat, binding?.buttonPrev, binding?.buttonShuffle)
	val layoutParamsList = buttons.map { button -> button?.layoutParams as? ConstraintLayout.LayoutParams }

	val expandedRadius = binding?.resources?.getDimension(R.dimen.fabMenuExpandedRadius)?.toInt() ?: 0 // this is shit

	val currentRadii = layoutParamsList.map { params -> params?.circleRadius }
	val finalRadius = if (show) expandedRadius else 0

	if (currentRadii.isEmpty()) return
	if (currentRadii.first() == finalRadius) return

	ValueAnimator.ofInt(currentRadii.first() ?: 0, finalRadius).apply {
		duration = FabMenu.DURATION
		interpolator = AccelerateDecelerateInterpolator()
		start()
		addUpdateListener {
			for ((i, params) in layoutParamsList.withIndex()) {
				params?.circleRadius = it.animatedValue as Int
				buttons[i]?.layoutParams = params
			}
		}
	}
}

// expands/collapses the white circle background
internal fun PlayerControlsManager.toggleFabMenuBackground(show: Boolean) {

	val expandedScale = binding?.resources?.getInteger(R.integer.fabMenuExpandedBackgroundScale) ?: 0 // also shit

	val currentScale = binding?.fabMenuBackground?.scaleX ?: 0F
	val finalScale = if (show) expandedScale.toFloat() else 0F

	if (currentScale == finalScale) return

	ValueAnimator.ofFloat(currentScale, finalScale).apply {
		duration = FabMenu.DURATION
		interpolator = AccelerateDecelerateInterpolator()
		start()
		addUpdateListener {
			binding?.fabMenuBackground?.scaleX = it.animatedValue as Float
			binding?.fabMenuBackground?.scaleY = it.animatedValue as Float
		}
	}
}

// the returned angle doesn't take cartesian quadrants into account
internal fun calculateFabGestureAngle(motionEvent: MotionEvent, view: View): Float {

	val x1 = motionEvent.rawX
	val y1 = motionEvent.rawY

	val x2 = view.x + view.pivotX
	val y2 = view.y + view.pivotY

	// the (x1, y1) is the DC shift (the location of the omni button)
	// the shift is done so that the angle is calculated correctly (the screen origin is the top left)
	val x = (x2 - x1).toDouble()
	val y = (y2 - y1).toDouble()

	return (atan(y / x) * 180 / PI).toFloat()
}

internal fun calculateFabGestureDistance(motionEvent: MotionEvent, view: View): Float {
	val x1 = motionEvent.rawX
	val y1 = motionEvent.rawY

	val x2 = view.x + view.pivotX
	val y2 = view.y + view.pivotY

	val x = (x1 - x2).absoluteValue
	val y = (y1 - y2).absoluteValue

	return hypot(x, y)
}

// returns button index by the given angle
internal fun getButtonByAngle(angle: Float): Int {
	val angleBracket = (90 + FabMenu.ANGLE_OFFSET - angle) / FabMenu.ANGLE_BRACKET_ARC
	return angleBracket.toInt()
}

// how awful is that??!!
internal fun PlayerControlsManager.toggleFabMenuButtonHighlight(buttonIndex: Int? = null) {
	binding?.buttonNext?.isPressed = false || buttonIndex == FabMenu.BUTTON_NEXT
	binding?.buttonRepeat?.isPressed = false || buttonIndex == FabMenu.BUTTON_REPEAT
	binding?.buttonShuffle?.isPressed = false || buttonIndex == FabMenu.BUTTON_SHUFFLE
	binding?.buttonPrev?.isPressed = false || buttonIndex == FabMenu.BUTTON_PREV
}

internal val fabMenuButtonEventMap = mapOf(
	FabMenu.BUTTON_NEXT to EventType.PLAY_NEXT,
	FabMenu.BUTTON_REPEAT to EventType.CYCLE_REPEAT,
	FabMenu.BUTTON_SHUFFLE to EventType.CYCLE_SHUFFLE,
	FabMenu.BUTTON_PREV to EventType.PLAY_PREVIOUS
)

internal val shuffleIcons = arrayOf(R.drawable.shuffle031, R.drawable.shuffle015) // inactive, active

internal val repeatIcons = arrayOf(R.drawable.repeat040, R.drawable.repeat015, R.drawable.repeat022) // inactive, active, one
internal val repeatAnimations = arrayOf(R.drawable.anim_repeat_inactive, R.drawable.anim_repeat_active, R.drawable.anim_repeat_one)
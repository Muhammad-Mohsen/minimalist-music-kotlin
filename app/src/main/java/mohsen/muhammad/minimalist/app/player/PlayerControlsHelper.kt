package mohsen.muhammad.minimalist.app.player

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.media_controls.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.core.ext.fadeIn
import mohsen.muhammad.minimalist.core.ext.fadeOut
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.FabMenu
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

	if (controls?.buttonOmni?.tag == animId) return // if the same animation is shown, do nothing

	controls?.buttonOmni?.animateDrawable(animId)
	controls?.buttonOmni?.tag = animId // set the tag
}

// does the little animation when selecting one of the fab menu buttons (next, shuffle, etc.)
internal fun PlayerControlsManager.animateFabMenuButton(buttonIndex: Int) {

	// flip the thing for prev
	controls?.fabButtonAnimationOverlay?.rotation = if (buttonIndex == FabMenu.BUTTON_PREV) 180F else 0F

	// make the overlay visible
	controls?.fabButtonAnimationOverlay?.fadeIn(0L)

	// do the animation
	controls?.fabButtonAnimationOverlay?.animateDrawable(getButtonAnimationByIndex(buttonIndex)) {

		// hide the overlay (after the animation completes)
		controls?.fabButtonAnimationOverlay?.fadeOut(200L)
	}
}

// gets the proper animation by fab index (repeat/shuffle buttons have multiple animations)
internal fun PlayerControlsManager.getButtonAnimationByIndex(buttonIndex: Int): Int {
	return when (buttonIndex) {
		FabMenu.BUTTON_NEXT -> R.drawable.anim_next
		FabMenu.BUTTON_REPEAT -> {
			val currentRepeat = controls?.buttonRepeat?.tag as? Int ?: return repeatAnimations[0]
			repeatAnimations[(currentRepeat + 1) % repeatAnimations.size]
		}
		FabMenu.BUTTON_SHUFFLE -> {
			val currentShuffle = controls?.buttonShuffle?.tag as? Boolean
			if (currentShuffle == true) R.drawable.anim_shuffle_inactive
			else R.drawable.anim_shuffle_active
		}
		else -> R.drawable.anim_next // FabMenu.BUTTON_PREV
	}
}

// updates the fab menu button iconography for shuffle and repeat buttons (also their tags)
internal fun PlayerControlsManager.updateFabMenuUi(buttonIndex: Int) {
	val context = controls?.context ?: return

	if (buttonIndex == FabMenu.BUTTON_REPEAT) {
		val currentRepeat = controls?.buttonRepeat?.tag as? Int ?: 0
		val updatedRepeat = (currentRepeat + 1) % repeatIcons.size

		// set the drawable
		controls?.buttonRepeat?.setImageDrawable(ContextCompat.getDrawable(context, repeatIcons[updatedRepeat]))

		val repeatTint = if (updatedRepeat == 0) R.color.colorPrimaryLight else R.color.colorOnBackgroundDark
		controls?.buttonRepeat?.setColorFilter(ContextCompat.getColor(context, repeatTint)) // set the tint color (active/inactive)

		controls?.buttonRepeat?.tag = updatedRepeat // update the tag

	} else if (buttonIndex == FabMenu.BUTTON_SHUFFLE) {
		val currentShuffle = controls?.buttonShuffle?.tag as? Boolean ?: false
		val shuffleTint = if (currentShuffle) R.color.colorPrimaryLight else R.color.colorOnBackgroundDark

		controls?.buttonShuffle?.setColorFilter(ContextCompat.getColor(context, shuffleTint)) // set the tint color (active/inactive)

		controls?.buttonShuffle?.tag = !currentShuffle // update the tag
	}

	// otherwise, do nothing
}

// expands/collapses the actual fab menu buttons
internal fun PlayerControlsManager.toggleFabMenuButtonExpansion(show: Boolean) {
	val buttons = arrayOf(controls?.buttonNext, controls?.buttonRepeat, controls?.buttonPrev, controls?.buttonShuffle)
	val layoutParamsList = buttons.map { button -> button?.layoutParams as? ConstraintLayout.LayoutParams }

	val expandedRadius = controls?.resources?.getDimension(R.dimen.fabMenuExpandedRadius)?.toInt() ?: 0

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

	val expandedScale = controls?.resources?.getInteger(R.integer.fabMenuExpandedBackgroundScale) ?: 0

	val currentScale = controls?.fabMenuBackground?.scaleX ?: 0F
	val finalScale = if (show) expandedScale.toFloat() else 0F

	if (currentScale == finalScale) return

	ValueAnimator.ofFloat(currentScale, finalScale).apply {
		duration = FabMenu.DURATION
		interpolator = AccelerateDecelerateInterpolator()
		start()
		addUpdateListener {
			controls?.fabMenuBackground?.scaleX = it.animatedValue as Float
			controls?.fabMenuBackground?.scaleY = it.animatedValue as Float
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
	controls?.buttonNext?.isPressed = false || buttonIndex == FabMenu.BUTTON_NEXT
	controls?.buttonRepeat?.isPressed = false || buttonIndex == FabMenu.BUTTON_REPEAT
	controls?.buttonShuffle?.isPressed = false || buttonIndex == FabMenu.BUTTON_SHUFFLE
	controls?.buttonPrev?.isPressed = false || buttonIndex == FabMenu.BUTTON_PREV
}

internal val fabMenuButtonEventMap = mapOf(
	FabMenu.BUTTON_NEXT to EventType.PLAY_NEXT,
	FabMenu.BUTTON_REPEAT to EventType.CYCLE_REPEAT,
	FabMenu.BUTTON_SHUFFLE to EventType.CYCLE_SHUFFLE,
	FabMenu.BUTTON_PREV to EventType.PLAY_PREVIOUS
)

internal val repeatIcons = arrayOf(R.drawable.repeat015, R.drawable.repeat015, R.drawable.repeat_one015) // inactive, active, one
internal val repeatAnimations = arrayOf(R.drawable.anim_repeat_inactive, R.drawable.anim_repeat_active, R.drawable.anim_repeat_one)
package mohsen.muhammad.minimalist.app.player

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.media_controls.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.data.FabMenu
import mohsen.muhammad.minimalist.data.PlaybackEventType
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan
import kotlin.math.hypot

/**
 * Created by muhammad.mohsen on 3/30/2019.
 * extends PlayerControlsManager with functions that are mostly related to the FAB
 */

internal fun PlayerControlsManager.togglePlayPauseUi(play: Boolean) {
	val animId = if (!play) R.drawable.anim_pause_play else R.drawable.anim_play_pause
	controls?.buttonOmni?.animateDrawable(animId)
}

internal fun PlayerControlsManager.toggleSingleFabMenuButton(button: View?, show: Boolean) {
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

internal fun calculateFabGestureAngle(motionEvent: MotionEvent, view: View): Float {

	val x1 = motionEvent.rawX
	val y1 = motionEvent.rawY

	val x2 = view.x + view.pivotX
	val y2 = view.y + view.pivotY

	return calculateFabGestureAngle(x1, y1, x2, y2)
}

private fun calculateFabGestureAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
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

	return calculateFabGestureDistance(x1, y1, x2, y2)
}

private fun calculateFabGestureDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
	val x = (x1 - x2).absoluteValue
	val y = (y1 - y2).absoluteValue

	return hypot(x, y)
}

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

internal val fabButtonEventMap = mapOf(
	FabMenu.BUTTON_NEXT to PlaybackEventType.PLAY_NEXT,
	FabMenu.BUTTON_REPEAT to PlaybackEventType.CYCLE_REPEAT,
	FabMenu.BUTTON_SHUFFLE to PlaybackEventType.CYCLE_SHUFFLE,
	FabMenu.BUTTON_PREV to PlaybackEventType.PLAY_PREVIOUS
)
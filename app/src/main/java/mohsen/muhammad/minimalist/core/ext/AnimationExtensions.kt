package mohsen.muhammad.minimalist.core.ext

import android.animation.ValueAnimator
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * animation extensions
 */

fun ImageView.animateDrawable(drawableResourceId: Int, endAction: (() -> Unit)? = null) {
    val drawable = ContextCompat.getDrawable(this.context, drawableResourceId) // get the frame animation drawable

	(this.drawable as? AnimationDrawable)?.stop() // stop any animation that might be running

	setImageDrawable(drawable)
	(this.drawable as AnimationDrawable).start(endAction)

    // set the drawable ID as the tag, so we know the final state of a given view
    // (e.g. to know that the back/root button is currently displaying the root icon)
	this.tag = drawableResourceId
}

// the AnimationDrawable API doesn't have a total duration prop!!
val AnimationDrawable.totalDuration: Long
	get() {
		var totalDuration = 0L
		for (i in 0 until numberOfFrames) totalDuration += getDuration(i)

		return totalDuration
	}

// the AnimationDrawable API doesn't have an OnCompleteListener!!
fun AnimationDrawable.start(endAction: (() -> Unit)? = null) {
	start()

	if (endAction == null) return
	Handler(Looper.getMainLooper()).postDelayed({
		try {
			endAction.invoke()

		} catch (e: Exception) {
			e.printStackTrace()
		}

	}, totalDuration)
}

// fades in/out a view with the PropertyAnimator API which is pretty awesome
fun View.fadeIn(duration: Long, delay: Long = 0L, endAction: (() -> Unit)? = null) {
	visibility = View.VISIBLE

	// if the duration is 0, just set the alpha
	if (duration == 0L) {
		alpha = 1F
		endAction?.invoke()

		return
	}

	ViewCompat.animate(this)
		.setStartDelay(delay)
		.setDuration(duration)
		.alpha(1F)
		.withEndAction {
			endAction?.invoke()
		}
}
fun View.fadeOut(duration: Long, delay: Long = 0L) {
	ViewCompat.animate(this)
		.setStartDelay(delay)
		.setDuration(duration)
		.alpha(0f)
		.withEndAction {
			visibility = View.GONE
		}
}

fun View.slideY(to: Float, duration: Long, delay: Long = 0L, endAction: (() -> Unit)? = null) {
	val metrics = resources.displayMetrics
	val toPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, to, metrics)

	ViewCompat.animate(this)
		.setStartDelay(delay)
		.setDuration(duration)
		.translationY(toPixels)
		.setInterpolator(AccelerateDecelerateInterpolator())
		.withEndAction {
			endAction?.invoke()
		}
}

fun View.scale(to: Float, duration: Long, delay: Long = 0L, endAction: (() -> Unit)? = null) {
	ViewCompat.animate(this)
		.setStartDelay(delay)
		.setDuration(duration)
		.scaleX(to)
		.scaleY(to)
		.setInterpolator(AccelerateDecelerateInterpolator())
		.withEndAction {
			endAction?.invoke()
		}
}

// bad API design
fun View.animateHeight(to: Int, duration: Long) {
	val valueAnimator = ValueAnimator.ofInt(this.measuredHeight, to)
	valueAnimator.duration = duration

	valueAnimator.addUpdateListener {
		val animatedValue = valueAnimator.animatedValue as Int
		val layoutParams = this.layoutParams
		layoutParams.height = animatedValue
		this.layoutParams = layoutParams
	}

	valueAnimator.start()
}

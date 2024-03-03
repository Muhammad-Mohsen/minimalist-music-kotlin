package mohsen.muhammad.minimalist.core.ext

import android.animation.ValueAnimator
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import mohsen.muhammad.minimalist.core.Moirai
import mohsen.muhammad.minimalist.data.Const


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
	Moirai.MAIN.postDelayed({
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
fun View.fadeOut(duration: Long, delay: Long = 0L, endAction: (() -> Unit) = fun () { visibility = View.GONE }) {
	ViewCompat.animate(this)
		.setStartDelay(delay)
		.setDuration(duration)
		.alpha(0f)
		.withEndAction {
			endAction()
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

// bad API design...can't remember why I said that!
fun View.animateHeight(to: Int, duration: Long) {
	val valueAnimator = ValueAnimator.ofInt(this.measuredHeight, to)
	valueAnimator.duration = duration

	valueAnimator.addUpdateListener {
		val layoutParams = this.layoutParams
		layoutParams.height = valueAnimator.animatedValue as Int
		this.layoutParams = layoutParams
	}
	valueAnimator.interpolator = Const.exponentialInterpolator
	valueAnimator.start()
}

fun View.slideY(delta: Int) {
	val params = when (parent) {
		is LinearLayoutCompat -> layoutParams as LinearLayoutCompat.LayoutParams
		is ConstraintLayout -> layoutParams as ConstraintLayout.LayoutParams
		else -> layoutParams as FrameLayout.LayoutParams
	}

	params.bottomMargin += delta
	if (params.bottomMargin > 0) params.bottomMargin = 0

	layoutParams = params
}

fun View.animateLayoutMargins(@DimenRes left: Int, @DimenRes top: Int, @DimenRes right: Int, @DimenRes bottom: Int, duration: Long, interpolator: Interpolator? = null) {
	val anim = object : Animation() {

		val params = when (parent) {
			is LinearLayoutCompat -> layoutParams as LinearLayoutCompat.LayoutParams
			is ConstraintLayout -> layoutParams as ConstraintLayout.LayoutParams
			else -> layoutParams as FrameLayout.LayoutParams
		}

		val il = params.leftMargin
		val it = params.topMargin
		val ir = params.rightMargin
		val ib = params.bottomMargin

		val dl = resources.getDimension(left) - params.leftMargin
		val dt = resources.getDimension(top) - params.topMargin
		val dr = resources.getDimension(right) - params.rightMargin
		val db = resources.getDimension(bottom) - params.bottomMargin

		@Override
		override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
			params.leftMargin = il + (dl * interpolatedTime).toInt()
			params.topMargin = it + (dt * interpolatedTime).toInt()
			params.rightMargin = ir + (dr * interpolatedTime).toInt()
			params.bottomMargin = ib + (db * interpolatedTime).toInt()

			layoutParams = params
		}
	}
	anim.duration = duration
	if (interpolator != null) anim.interpolator = interpolator

	startAnimation(anim)
}
fun View.animateLayoutMargins(@DimenRes horizontal: Int, @DimenRes vertical: Int, duration: Long, interpolator: Interpolator? = null) {
	animateLayoutMargins(horizontal, vertical, horizontal, vertical, duration, interpolator)
}
fun View.animateLayoutMargins(@DimenRes margin: Int, duration: Long, interpolator: Interpolator? = null) {
	animateLayoutMargins(margin, margin, duration, interpolator)
}

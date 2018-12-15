package mohsen.muhammad.minimalist.core

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import mohsen.muhammad.minimalist.R


/**
 * Created by muhammad.mohsen on 4/14/2018.
 * Drawable extension functions
 */


fun GradientDrawable.setDrawableStroke(width: Float, @ColorInt color: Int) {
	this.setStroke(width.toInt(), color)
}

fun GradientDrawable.setDrawableStroke(width: Float, @ColorInt color: Int, dashWidth: Float, dashGap: Float) {
	this.setStroke(width.toInt(), color, dashWidth, dashGap)
}

// sets the fill color
fun GradientDrawable.setDrawableFillColor(@ColorInt color: Int) {
	this.setColor(color)
}

fun GradientDrawable.setDrawableCornerRadii(radii: FloatArray) {
	this.cornerRadii = radii
}

fun View.setRoundedBackground(attributes: AttributeSet?) {
	background = ContextCompat.getDrawable(context, R.drawable.background_extended_view)

	if (attributes != null) {

		// mutate has to be called so that the following changes don't affect other instances of the drawable
		background.mutate()

		applyExtendedViewAttr(attributes)
		applyCornerAttr(attributes)
	}
}

fun View.setViewStroke(widthDp: Int, @ColorInt color: Int) {
	// convert the given width (which should be in dp) to pixels
	val widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp.toFloat(), resources.displayMetrics).toInt()

	val backgroundShape = (background as LayerDrawable).findDrawableByLayerId(R.id.roundedCardShape) as GradientDrawable
	backgroundShape.setStroke(widthPx, color)
}

fun View.setViewRippleColor(@ColorInt rippleColor: Int) {
	(background as RippleDrawable).setColor(ColorStateList(
		arrayOf(intArrayOf()),
		intArrayOf(rippleColor)
	))
}

fun View.setViewFillColor(@ColorInt fillColor: Int) {
	val backgroundShape = (background as LayerDrawable).findDrawableByLayerId(R.id.roundedCardShape) as GradientDrawable
	backgroundShape.setDrawableFillColor(fillColor)
}

/**
 * Applies the ExtendedView attribute set on the specified view.
 * Note that the R.drawable.background_teardrop_card background HAS to be set on the view before calling this function
 */
fun View.applyExtendedViewAttr(attributes: AttributeSet) {

	val displayMetrics = resources.displayMetrics
	val backgroundShape = (background as LayerDrawable).findDrawableByLayerId(R.id.roundedCardShape) as GradientDrawable

	val roundedViewAttrs = context.obtainStyledAttributes(attributes, R.styleable.ExtendedView)

	// fill color (default transparent)
	val fillColor = roundedViewAttrs.getColor(R.styleable.ExtendedView_fillColor, ContextCompat.getColor(context, R.color.colorPrimary))
	backgroundShape.setDrawableFillColor(fillColor)

	// stroke (default white)
	val strokeWidth = roundedViewAttrs.getDimension(R.styleable.ExtendedView_strokeWidth, 0F)
	val strokeColor = roundedViewAttrs.getColor(R.styleable.ExtendedView_strokeColor, ContextCompat.getColor(context, R.color.colorPrimary))

	// dashes - check if the attributes exist or not
	if (roundedViewAttrs.hasValue(R.styleable.ExtendedView_strokeDashWidth)) {
		val dashWidth = roundedViewAttrs.getDimension(R.styleable.ExtendedView_strokeDashWidth, 1F)
		val dashGap = roundedViewAttrs.getDimension(R.styleable.ExtendedView_strokeDashGap, 1F)

		// actually apply the stroke
		backgroundShape.setDrawableStroke(strokeWidth, strokeColor, dashWidth, dashGap)

	} else {
		// actually apply the stroke (without dashes)
		backgroundShape.setDrawableStroke(strokeWidth, strokeColor)
	}

	// rippleColor
	val rippleColor = roundedViewAttrs.getColor(R.styleable.ExtendedView_rippleColor, ContextCompat.getColor(context, R.color.colorPrimary))
	(background as RippleDrawable).setColor(ColorStateList(
		arrayOf(intArrayOf()),
		intArrayOf(rippleColor)
	))

	roundedViewAttrs.recycle()
}

/**
 * Applies the Corners attribute set on the specified view.
 * Note that the R.drawable.background_teardrop_card background HAS to be set on the view before calling this function
 */
fun View.applyCornerAttr(attributes: AttributeSet) {

	val backgroundShape = (background as LayerDrawable).findDrawableByLayerId(R.id.roundedCardShape) as GradientDrawable

	val cornerAttrs = context.obtainStyledAttributes(attributes, R.styleable.ExtendedView)

	var cornerTopLeft: Float
	var cornerTopRight: Float
	var cornerBottomLeft: Float
	var cornerBottomRight: Float

	// if the cornerRadius attribute exists, use that instead of going through all corners
	if (cornerAttrs.hasValue(R.styleable.ExtendedView_cornerRadius)) {
		val corners = cornerAttrs.getDimension(R.styleable.ExtendedView_cornerRadius, 0F)

		cornerTopLeft = corners
		cornerTopRight = corners
		cornerBottomLeft = corners
		cornerBottomRight = corners

	} else {
		cornerTopLeft = cornerAttrs.getDimension(R.styleable.ExtendedView_cornerRadiusTopLeft, 0F)
		cornerTopRight = cornerAttrs.getDimension(R.styleable.ExtendedView_cornerRadiusTopRight, 0F)
		cornerBottomLeft = cornerAttrs.getDimension(R.styleable.ExtendedView_cornerRadiusBottomLeft, 0F)
		cornerBottomRight = cornerAttrs.getDimension(R.styleable.ExtendedView_cornerRadiusBottomRight, 0F)
	}

	// flip the corners for RTL
	if (isRtl()) {
		var temp = cornerTopLeft
		cornerTopLeft = cornerTopRight
		cornerTopRight = temp

		temp = cornerBottomLeft
		cornerBottomLeft = cornerBottomRight
		cornerBottomRight = temp
	}

	// corners
	val radii = floatArrayOf(
		cornerTopLeft, cornerTopLeft,
		cornerTopRight, cornerTopRight,
		cornerBottomLeft, cornerBottomLeft,
		cornerBottomRight, cornerBottomRight
	)

	backgroundShape.setDrawableCornerRadii(radii)

	cornerAttrs.recycle()
}

fun View.applyPaddingAttr(attributes: AttributeSet?) {

	if (attributes == null)
		return

	val paddingAttrTypedArray = context.obtainStyledAttributes(attributes, R.styleable.androidAttributes)

	var paddingLeft: Int
	val paddingTop: Int
	var paddingRight: Int
	val paddingBottom: Int

	if (paddingAttrTypedArray.hasValue(R.styleable.androidAttributes_android_padding)) {
		val padding = paddingAttrTypedArray.getDimensionPixelSize(R.styleable.androidAttributes_android_padding, 0)
		paddingLeft = padding
		paddingTop = padding
		paddingRight = padding
		paddingBottom = padding

	} else {

		paddingLeft = paddingAttrTypedArray.getDimensionPixelSize(R.styleable.androidAttributes_android_paddingStart, 0)
		paddingTop = paddingAttrTypedArray.getDimensionPixelSize(R.styleable.androidAttributes_android_paddingTop, 0)
		paddingRight = paddingAttrTypedArray.getDimensionPixelSize(R.styleable.androidAttributes_android_paddingEnd, 0)
		paddingBottom = paddingAttrTypedArray.getDimensionPixelSize(R.styleable.androidAttributes_android_paddingBottom, 0)

		// switch the left/right padding values for RTL
		if (isRtl()) {
			val temp = paddingLeft
			paddingLeft = paddingRight
			paddingRight = temp
		}

	}

	setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

	paddingAttrTypedArray.recycle()
}

private fun View.isRtl(): Boolean {
	return false
}
package mohsen.muhammad.minimalist.core.ext

import android.content.res.ColorStateList
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
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
		background.mutate() // mutate has to be called so that the following changes don't affect other instances of the drawable

		applyExtendedViewAttr(attributes)
		applyCornerAttr(attributes)
	}
}

// holy hell, a corner radius is WORK!!
fun AppCompatImageView.setRoundedBackground(attributes: AttributeSet?) {

	val cornerAttrs = context.obtainStyledAttributes(attributes, R.styleable.ExtendedView)
	if (!cornerAttrs.hasValue(R.styleable.ExtendedView_cornerRadius)) return
	val radius = cornerAttrs.getDimension(R.styleable.ExtendedView_cornerRadius, 0F)

	outlineProvider = object : ViewOutlineProvider() {
		override fun getOutline(view: View?, outline: Outline?) {
			outline?.setRoundRect(0, 0, view!!.width, view.height, radius)
		}
	}
	clipToOutline = true

	cornerAttrs.recycle()
}

/**
 * Applies the ExtendedView attribute set on the specified view.
 * Note that the R.drawable.background_teardrop_card background HAS to be set on the view before calling this function
 */
fun View.applyExtendedViewAttr(attributes: AttributeSet) {
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

	val cornerTopLeft: Float
	val cornerTopRight: Float
	val cornerBottomLeft: Float
	val cornerBottomRight: Float

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

	// corners
	val radii = floatArrayOf(
		cornerTopLeft, cornerTopLeft,
		cornerTopRight, cornerTopRight,
		cornerBottomRight, cornerBottomRight,
		cornerBottomLeft, cornerBottomLeft
	)

	backgroundShape.setDrawableCornerRadii(radii)

	cornerAttrs.recycle()
}

fun View.applyPaddingAttr(attributes: AttributeSet?) {

	if (attributes == null)
		return

	val paddingAttrTypedArray = context.obtainStyledAttributes(attributes, R.styleable.androidAttributes)

	val paddingLeft: Int
	val paddingTop: Int
	val paddingRight: Int
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
	}

	setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

	paddingAttrTypedArray.recycle()
}

fun ImageButton.setImageDrawable(@DrawableRes d: Int) {
	setImageDrawable(ContextCompat.getDrawable(context, d))
}

fun View.setHeight(height: Int) {
	val layoutParams = this.layoutParams
	layoutParams.height = height

	this.layoutParams = layoutParams
}
package mohsen.muhammad.minimalist.core

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import mohsen.muhammad.minimalist.core.ext.applyPaddingAttr
import mohsen.muhammad.minimalist.core.ext.setRoundedBackground

/**
 * Created by muhammad.mohsen on 11/4/2018.
 */

class ExtendedLinearLayout @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
	: LinearLayoutCompat(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}

class ExtendedFrameLayout @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
	: FrameLayout(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}

class ExtendedConstraintLayout @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
	: ConstraintLayout(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}

class ExtendedImageButton @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = androidx.appcompat.R.style.Widget_AppCompat_ImageButton)
	: AppCompatImageButton(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}

class ExtendedButton @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = androidx.appcompat.R.style.Widget_AppCompat_Button)
	: AppCompatButton(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}

class ExtendedView @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
	: View(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}

class ExtendedImageView @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null, defStyleAttr: Int = 0)
	: androidx.appcompat.widget.AppCompatImageView(context, attributes, defStyleAttr) {

	init {
		setRoundedBackground(attributes)
		applyPaddingAttr(attributes)
	}
}
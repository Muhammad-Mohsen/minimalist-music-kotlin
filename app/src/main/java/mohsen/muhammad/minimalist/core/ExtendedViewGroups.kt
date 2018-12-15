package mohsen.muhammad.minimalist.core

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout

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

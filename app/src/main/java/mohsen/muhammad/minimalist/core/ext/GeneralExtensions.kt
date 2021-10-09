package mohsen.muhammad.minimalist.core.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.viewbinding.ViewBinding
import java.util.*

/**
 * Created by muhammad.mohsen on 11/4/2018.
 * General extension functions and properties
 */

val String.Companion.EMPTY
	get() = ""

fun Context.unregisterReceiverSafe(receiver: BroadcastReceiver) {
	try {
		unregisterReceiver(receiver)

	} catch (e: Exception) {
		e.message?.let { Log.e("unregisterReceiverSafe", it) }
	}
}

fun Timer?.cancelSafe() {
	try {
		this?.cancel()

	} catch (e: Exception) {
		e.message?.let { Log.e("cancelSafe", it) }
	}
}

val View.isTransparent: Boolean
	get() = alpha == 0f

fun Float.toDip(context: Context): Float {
	return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
}
fun Context.convertToDip(value: Float): Float {
	return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}

fun View.setLayoutMargins(left: Number, top: Number, right: Number, bottom: Number) {
	val params = when (this.layoutParams) {
		is FrameLayout.LayoutParams -> FrameLayout.LayoutParams(this.layoutParams.width, this.layoutParams.height)
		is LinearLayoutCompat.LayoutParams -> LinearLayoutCompat.LayoutParams(this.layoutParams.width, this.layoutParams.height)
		else -> LinearLayout.LayoutParams(this.layoutParams.width, this.layoutParams.height) // default to LinearLayout
	}

	params.setMargins(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
	this.layoutParams = params
}

fun SharedPreferences.put(key: String, value: Any) {
	val editor = this.edit()

	when (value) {
		is String -> editor.putString(key, value)
		is Int -> editor.putInt(key, value)
		is Long -> editor.putLong(key, value)
		is Boolean -> editor.putBoolean(key, value)
	}

	editor.apply()
}

val ViewBinding.context: Context
	get() = this.root.context

val ViewBinding.resources: Resources
	get() = this.root.resources
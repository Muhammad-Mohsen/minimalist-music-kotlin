package mohsen.muhammad.minimalist.core.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
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

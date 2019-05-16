package mohsen.muhammad.minimalist.core.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log

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
		Log.e("unregisterReceiverSafe", e.message)
	}
}
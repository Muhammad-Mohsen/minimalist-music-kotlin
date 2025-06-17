package com.minimalist.music.foundation.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.NotificationCompat
import com.minimalist.music.foundation.Moirai
import java.util.Timer
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject


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

fun Int.toReadableTime(withSeconds: Boolean = false): String {
	fun pad(num: Int) = num.toString().padStart(2, '0')

	return if (withSeconds) "${pad(this / 60 / 60)}:${pad(this / 60 % 60)}:${pad(this % 60)}"
	else "${pad(this / 60 / 60)}:${pad((this / 60 % 60))}"
}

fun Number.toDip(context: Context): Float {
	return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics)
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
	this.edit {
		when (value) {
			is String -> putString(key, value)
			is Int -> putInt(key, value)
			is Long -> putLong(key, value)
			is Float -> putFloat(key, value)
			is Boolean -> putBoolean(key, value)
		}
	}
}

fun JSONObject.toMap(): Map<String, Any> {
	val map = mutableMapOf<String, Any>()
	val keysItr = this.keys()
	while (keysItr.hasNext()) {
		val key = keysItr.next()
		var value = this[key] ?: ""

		// Handle nested objects and arrays
		when (value) {
			is JSONObject -> value = value.toMap()
			is JSONArray -> value = value.toList()
		}
		map[key] = value
	}
	return map
}
fun JSONArray.toList(): List<Any> {
	val list = mutableListOf<Any>()
	for (i in 0 until this.length()) {
		var value = this[i]
		when (value) {
			is JSONObject -> value = value.toMap()
			is JSONArray -> value = value.toList()
		}
		list.add(value)
	}
	return list
}

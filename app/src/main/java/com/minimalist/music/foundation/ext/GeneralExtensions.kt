package com.minimalist.music.foundation.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer


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

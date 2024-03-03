package mohsen.muhammad.minimalist.core.ext

import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.viewbinding.ViewBinding
import mohsen.muhammad.minimalist.core.Moirai
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

// coroutines, nay!!
fun AppCompatImageView.setEncodedBitmapAsync(encodedBitmap: ByteArray?, handler: Handler = Moirai.BG) {
	if (encodedBitmap == null) {
		setImageResource(android.R.color.transparent)
		return
	}

	handler.post {
		val bitmap = BitmapFactory.decodeByteArray(encodedBitmap, 0, encodedBitmap.size)
		post {
			setImageBitmap(bitmap)
			val matrix = ColorMatrix().apply {
				setSaturation(0f)
			}
			colorFilter = ColorMatrixColorFilter(matrix)
		}
	}
}
fun MediaMetadataCompat.Builder.putEncodedBitmap(key: String, encodedBitmap: ByteArray?) {
	val bitmap = encodedBitmap?.let {
		BitmapFactory.decodeByteArray(encodedBitmap, 0, encodedBitmap.size)
	}
	this.putBitmap(key, bitmap)
}

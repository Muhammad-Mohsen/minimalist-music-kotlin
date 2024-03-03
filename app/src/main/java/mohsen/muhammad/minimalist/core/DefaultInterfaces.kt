package mohsen.muhammad.minimalist.core

import android.text.Editable
import android.text.TextWatcher
import android.widget.SeekBar

/**
 * Created by muhammad.mohsen on 3/12/2019.
 * contains default implementations for multi-function interfaces (e.g. TextWatcher)
 */

interface OnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
	override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
	override fun onStartTrackingTouch(p0: SeekBar?) {}
	override fun onStopTrackingTouch(p0: SeekBar?) {}
}

interface OnTextChangeListener : TextWatcher {
	override fun afterTextChanged(s: Editable?) {}
	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}
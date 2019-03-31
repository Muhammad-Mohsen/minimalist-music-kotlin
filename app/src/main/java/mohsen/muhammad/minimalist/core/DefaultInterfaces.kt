package mohsen.muhammad.minimalist.core

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
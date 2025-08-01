package com.minimalist.music.foundation.ext

import android.media.MediaPlayer
import android.media.audiofx.Equalizer

/**
 * Created by muhammad.mohsen on 2/17/2019.
 * Contains functions that encapsulate MediaPlayer's crappy API
 */

fun MediaPlayer.playPause(play: Boolean) {
	try {
		if (play) start()
		else pause()

	} catch (e: Exception) {
		e.printStackTrace()
	}
}

fun MediaPlayer.prepareSource(path: String) {
	try {
		reset()
		setDataSource(path)
		prepare()

	} catch (e: Exception) {
		e.printStackTrace()
	}
}

val MediaPlayer?.currentPositionSafe: Int
	get() {
		return try {
			this?.currentPosition ?: 0
		} catch (e: Exception) {
			e.printStackTrace()
			0
		}
	}

fun Equalizer?.ensureEnabled(): Int? {
	return if (this?.enabled == false) this.setEnabled(true)
	else 0
}
fun Equalizer.getInfo(preset: Short): Map<String, Any> {
	val bands = (0 until numberOfBands).map {
		return@map mapOf(
			"id" to it,
			"low" to bandLevelRange[0],
			"high" to bandLevelRange[1],
			"centerFrequency" to getCenterFreq(it.toShort()),
			"level" to getBandLevel(it.toShort())
		)
	}

	val presets = (0 until numberOfPresets).map {
		return@map mapOf(
			"id" to it,
			"name" to getPresetName(it.toShort())
		)
	}

	return mapOf(
		"bands" to bands,

		"presets" to presets,
		"currentPreset" to preset
	)
}

package com.minimalist.music.foundation.ext

import android.media.MediaPlayer

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

val MediaPlayer?.isPlayingSafe: Boolean
	get() {
		if (this == null) return false

		return try {
			isPlaying

		} catch (e: Exception) {
			e.printStackTrace()
			false
		}
	}

val MediaPlayer.currentPositionSafe: Int
	get() {
		return try {
			currentPosition
		} catch (e: Exception) {
			e.printStackTrace()
			0
		}
	}

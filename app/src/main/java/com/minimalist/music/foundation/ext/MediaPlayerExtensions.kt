package com.minimalist.music.foundation.ext

import android.media.MediaPlayer
import java.util.*
import java.util.concurrent.TimeUnit

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
			false
		}
	}

val MediaPlayer.currentPositionSafe: Int
	get() {
		return try {
			currentPosition
		} catch (e: Exception) {
			0
		}
	}

fun formatMillis(durationMillis: Long): String {
	val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
	val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
	val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

	return if (hours > 0) String.format(Locale("US"), "%02d:%02d:%02d", hours, minutes, seconds)
	else String.format(Locale("US"), "%02d:%02d", minutes, seconds)
}
package com.minimalist.music.data.state

import android.content.SharedPreferences
import com.minimalist.music.data.state.Playlist.RepeatMode
import com.minimalist.music.data.state.State.Key
import com.minimalist.music.data.state.State.playlist
import com.minimalist.music.foundation.ext.put

class Settings(private val preferences: SharedPreferences) {
	var seekJump: Int
		get() = preferences.getInt(Key.SEEK_JUMP, 60) // 1 minute default
		set(value) = preferences.put(Key.SEEK_JUMP, value)

	var playbackSpeed: Float
		get() = preferences.getFloat(Key.PLAYBACK_SPEED, 1F)
		set(value) = preferences.put(Key.PLAYBACK_SPEED, value)

	var nightMode: Int
		get() = preferences.getInt(Key.NIGHT_MODE, -1) // default is FOLLOW_SYSTEM
		set(value) = preferences.put(Key.NIGHT_MODE, value)

	var sleepTimer: Int
		get() = preferences.getInt(Key.SLEEP_TIMER, 60) // 1 hour default
		set(value) = preferences.put(Key.SLEEP_TIMER, value)

	var sort: Int
		get() = preferences.getInt(Key.SORT, 60) // 1 hour default
		set(value) = preferences.put(Key.SORT, value)

	var repeat: Int
		get() = preferences.getInt(Key.REPEAT, RepeatMode.INACTIVE)
		set(value) = preferences.put(Key.REPEAT, value)

	var shuffle: Boolean
		get() = preferences.getBoolean(Key.SHUFFLE, false)
		set(value) = preferences.put(Key.SHUFFLE, value)

	fun serialize(): Map<String, Any> {
		return mapOf(
			"seekJump" to seekJump,
			"playbackSpeed" to playbackSpeed,
			"nightMode" to nightMode,
			"sleepTimer" to sleepTimer,
			"sort" to sort,
			"repeat" to repeat,
			"shuffle" to shuffle,
		)
	}
}

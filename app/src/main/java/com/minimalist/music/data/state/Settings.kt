package com.minimalist.music.data.state

import android.content.SharedPreferences
import com.minimalist.music.data.state.Playlist.RepeatMode
import com.minimalist.music.data.state.State.Key
import com.minimalist.music.data.state.State.playlist
import com.minimalist.music.foundation.ext.put

class Settings(private val preferences: SharedPreferences) {
	private var _seekJump: Int? = null
	var seekJump: Int
		get() {
			if (_seekJump == null) _seekJump = preferences.getInt(Key.SEEK_JUMP, 60) // 1 minute default
			return _seekJump!!
		}
		set(value) {
			_seekJump = value
			preferences.put(Key.SEEK_JUMP, value)
		}

	private var _playbackSpeed: Float? = null
	var playbackSpeed: Float
		get() {
			if (_playbackSpeed == null) _playbackSpeed = preferences.getFloat(Key.PLAYBACK_SPEED, 1F)
			return _playbackSpeed!!
		}
		set(value) {
			_playbackSpeed = value
			preferences.put(Key.PLAYBACK_SPEED, value)
		}

	private var _nightMode: Int? = null
	var nightMode: Int
		get() {
			if (_nightMode == null) _nightMode = preferences.getInt(Key.NIGHT_MODE, -1) // default is FOLLOW_SYSTEM
			return _nightMode!!
		}
		set(value) {
			_nightMode = value
			preferences.put(Key.NIGHT_MODE, value)
		}

	private var _sleepTimer: Int? = null
	var sleepTimer: Int
		get() {
			if (_sleepTimer == null) _sleepTimer = preferences.getInt(Key.SLEEP_TIMER, 60) // 1 hour default
			return _sleepTimer!!
		}
		set(value) {
			_sleepTimer = value
			preferences.put(Key.SLEEP_TIMER, value)
		}

	private var _sort: Int? = null
	var sort: Int
		get() {
			if (_sort == null) _sort = preferences.getInt(Key.SORT, 0) // Assuming 0 or a specific default for sort
			return _sort!!
		}
		set(value) {
			_sort = value
			preferences.put(Key.SORT, value)
		}

	private var _repeat: Int? = null
	var repeat: Int
		get() {
			if (_repeat == null) _repeat = preferences.getInt(Key.REPEAT, RepeatMode.INACTIVE)
			return _repeat!!
		}
		set(value) {
			_repeat = value
			preferences.put(Key.REPEAT, value)
		}

	private var _shuffle: Boolean? = null
	var shuffle: Boolean
		get() {
			if (_shuffle == null) _shuffle = preferences.getBoolean(Key.SHUFFLE, false)
			return _shuffle!!
		}
		set(value) {
			_shuffle = value
			preferences.put(Key.SHUFFLE, value)
		}

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

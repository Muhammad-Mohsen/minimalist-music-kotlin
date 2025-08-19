package com.minimalist.music.data.state

import android.content.SharedPreferences
import com.minimalist.music.data.files.RepeatMode
import com.minimalist.music.data.files.SortBy
import com.minimalist.music.data.files.Theme
import com.minimalist.music.data.state.State.Key
import com.minimalist.music.foundation.ext.getStringSafe
import com.minimalist.music.foundation.ext.put

class Settings(private val preferences: SharedPreferences) {
	private var _seekJump: Int? = null
	var seekJump: Int
		get() {
			if (_seekJump == null) _seekJump = preferences.getInt(Key.SEEK_JUMP, 60 * 1000) // 1 minute default
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

	private var _theme: String? = null
	var theme: String
		get() {
			if (_theme == null) _theme = preferences.getStringSafe(Key.THEME, Theme.DARK)
			return _theme!!
		}
		set(value) {
			_theme = value
			preferences.put(Key.THEME, value)
		}

	private var _sleepTimer: Int? = null
	var sleepTimer: Int
		get() {
			if (_sleepTimer == null) _sleepTimer = preferences.getInt(Key.SLEEP_TIMER, 60 * 60 * 1000) // 1 hour default
			return _sleepTimer!!
		}
		set(value) {
			_sleepTimer = value
			preferences.put(Key.SLEEP_TIMER, value)
		}

	private var _sort: String? = null
	var sortBy: String
		get() {
			if (_sort == null) _sort = preferences.getString(Key.SORT, SortBy.AZ)
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

	private var _equalizerPreset: Short? = null
	var equalizerPreset: Short
		get() {
			if (_equalizerPreset == null) _equalizerPreset = preferences.getInt(Key.EQUALIZER_PRESET, 0).toShort()
			return _equalizerPreset!!
		}
		set(value) {
			_equalizerPreset = value
			preferences.put(Key.EQUALIZER_PRESET, value.toInt())
		}

	private var _equalizerBands: MutableMap<Int, Int>? = null
	var equalizerBands: MutableMap<Int, Int>
		get() {
			if (_equalizerBands == null) {
				val bands = preferences.getString(Key.EQUALIZER_BANDS, null)?.split(";")?.map { it.toInt() }
				_equalizerBands = if (bands == null) mutableMapOf() else (bands.indices zip bands).toMap().toMutableMap()
			}

			return _equalizerBands!!
		}
		set(value) {
			preferences.put(Key.EQUALIZER_BANDS, value.values.joinToString(";"))
		}

	private var _secondaryControls: String? = null
	var secondaryControls: String
		get() {
			if (_secondaryControls == null) _secondaryControls = preferences.getString(Key.SECONDARY_CONTROLS, "SEARCH;RW;PREV;NEXT;FF")
			return _secondaryControls!!
		}
		set(value) {
			_secondaryControls = value
			preferences.put(Key.SECONDARY_CONTROLS, value)
		}

	fun serialize(): Map<String, Any> {
		return mapOf(
			"seekJump" to seekJump,
			"playbackSpeed" to playbackSpeed,
			"theme" to theme,
			"sleepTimer" to sleepTimer,
			"sortBy" to sortBy,
			"repeat" to repeat,
			"shuffle" to shuffle,
			"equalizerPreset" to equalizerPreset,
			"equalizerBands" to equalizerBands,
			"secondaryControls" to secondaryControls
		)
	}
}

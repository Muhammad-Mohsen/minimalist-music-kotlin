package com.minimalist.music.data.state

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.minimalist.music.data.Const
import com.minimalist.music.player.PlaybackManager
import com.minimalist.music.foundation.ext.EMPTY
import com.minimalist.music.foundation.ext.formatMillis
import com.minimalist.music.foundation.ext.put
import com.minimalist.music.data.files.Chapter
import com.minimalist.music.data.files.ExplorerFile
import com.minimalist.music.data.files.FileMetadata
import com.minimalist.music.foundation.EventBus.Type
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * holds application-wide state (such as current directory, shuffle/repeat states, etc.)
 * it's also responsible for persisting those variables (in shared preferences)
 * State should never be registered with the EventBus...the async nature of EventBus would prevent the State from being 'trustworthy'
 */

object State {

	private lateinit var sharedPreferences: SharedPreferences
	lateinit var applicationContext: Context

	fun initialize(context: Context) {
		if (initialized) return

		applicationContext = context
		sharedPreferences = context.getSharedPreferences(Const.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)

		playlist = Playlist(sharedPreferences)
		track = Track(sharedPreferences)
		track.update()
	}

	private val initialized: Boolean
		get() = ::sharedPreferences.isInitialized

	var currentDirectory: File
		get() {
			val savedPath = sharedPreferences.getString(Key.DIRECTORY, null) ?: ExplorerFile.ROOT
			val savedFile = File(savedPath)
			return if (savedFile.exists()) savedFile else File(ExplorerFile.ROOT) // only return the saved file if it exists (it could've been removed, or that the SD card is unmounted!)
		}
		set(value) = sharedPreferences.put(Key.DIRECTORY, value.absolutePath)

	val isPlaying: Boolean
		get() = PlaybackManager.isPlaying

	val audioSessionId: Int
		get() = PlaybackManager.audioSessionId

	var seekJump: Int
		get() = sharedPreferences.getInt(Key.SEEK_JUMP, 60) // 1 minute default
		set(value) = sharedPreferences.put(Key.SEEK_JUMP, value)

	var playbackSpeed: Float
		get() = sharedPreferences.getFloat(Key.PLAYBACK_SPEED, 1F)
		set(value) = sharedPreferences.put(Key.PLAYBACK_SPEED, value)

	var nightMode: Int
		get() = sharedPreferences.getInt(Key.NIGHT_MODE, -1) // default is FOLLOW_SYSTEM
		set(value) = sharedPreferences.put(Key.NIGHT_MODE, value)

	val isSelectModeActive: Boolean
		get() = selectedTracks.isNotEmpty()

	val selectedTracks = ArrayList<String>()
	fun updateSelectedTracks(track: String): String {
		return if (selectedTracks.contains(track)) { // track already in the list, remove it
			selectedTracks.remove(track)
			if (selectedTracks.isEmpty()) Type.SELECT_MODE_CANCEL else Type.SELECT_MODE_SUB // if the list is empty, deactivate the select mode

		} else {
			selectedTracks.add(track)
			Type.SELECT_MODE_ADD
		}
	}

	var isSearchModeActive = false

	var isSleepTimerActive = false
	var sleepTimer: Int
		get() = sharedPreferences.getInt(Key.SLEEP_TIMER, 60) // 1 hour default
		set(value) = sharedPreferences.put(Key.SLEEP_TIMER, value)

	lateinit var track: Track
	lateinit var playlist: Playlist

	// the shared preferences keys
	internal object Key {
		const val DIRECTORY = "CurrentDirectory"
		const val PLAYLIST = "Playlist"
		const val PATH = "CurrentTrack"
		const val SEEK = "Seek"
		const val SEEK_JUMP = "SeekJump"
		const val PLAYBACK_SPEED = "PlaybackSpeed"
		const val SLEEP_TIMER = "SleepTimer"
		const val NIGHT_MODE = "NightMode"

		const val REPEAT = "Repeat"
		const val SHUFFLE = "Shuffle"
	}
}

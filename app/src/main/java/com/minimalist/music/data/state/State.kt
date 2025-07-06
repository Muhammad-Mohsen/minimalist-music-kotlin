package com.minimalist.music.data.state

import android.content.Context
import android.content.SharedPreferences
import com.minimalist.music.data.Const
import com.minimalist.music.data.files.ExplorerFile
import com.minimalist.music.data.files.FileCache
import com.minimalist.music.data.files.serializeFiles
import com.minimalist.music.foundation.ext.put
import com.minimalist.music.player.PlaybackManager
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

	private val initialized: Boolean
		get() = ::sharedPreferences.isInitialized

	var currentDirectory: File
		get() {
			val savedPath = sharedPreferences.getString(Key.DIRECTORY, null) ?: ExplorerFile.ROOT
			val savedFile = File(savedPath)
			return if (savedFile.exists()) savedFile else File(ExplorerFile.ROOT) // only return the saved file if it exists (it could've been removed, or that the SD card is unmounted!)
		}
		set(value) = sharedPreferences.put(Key.DIRECTORY, value.absolutePath)

	val files: ArrayList<ExplorerFile>
		get() = FileCache.getExplorerFilesByDirectory(currentDirectory)

	var mode = "normal"

	val isPlaying: Boolean
		get() = PlaybackManager.isPlaying

	val audioSessionId: Int
		get() = PlaybackManager.audioSessionId

	val selectedTracks = ArrayList<String>()

	var isSleepTimerActive = false

	lateinit var track: Track
	lateinit var playlist: Playlist
	lateinit var settings: Settings

	fun initialize(context: Context) {
		if (initialized) return

		applicationContext = context
		sharedPreferences = context.getSharedPreferences(Const.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)

		playlist = Playlist(sharedPreferences)
		track = Track(sharedPreferences)
		settings = Settings(sharedPreferences)
		
		track.update()
	}

	fun serialize(): Map<String, Any> {
		return mapOf(
			"currentDir" to currentDirectory,
			"files" to files.serializeFiles(),
			"selection" to selectedTracks,
			"isPlaying" to isPlaying,
			"isSleepTimerActive" to isSleepTimerActive,

			"track" to track.serialize(),
			"playlist" to playlist.serialize(),
			"settings" to settings.serialize()
		)
	}

	// shared preferences keys
	internal object Key {
		const val DIRECTORY = "CurrentDirectory"
		const val PLAYLIST = "Playlist"
		const val PATH = "CurrentTrack"
		const val SEEK = "Seek"
		const val SEEK_JUMP = "SeekJump"
		const val PLAYBACK_SPEED = "PlaybackSpeed"
		const val SLEEP_TIMER = "SleepTimer"
		const val NIGHT_MODE = "NightMode"
		const val SORT = "Sort"
		const val REPEAT = "Repeat"
		const val SHUFFLE = "Shuffle"
	}
}

package mohsen.muhammad.minimalist.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.core.ext.EMPTY
import mohsen.muhammad.minimalist.core.ext.formatMillis
import mohsen.muhammad.minimalist.core.ext.put
import mohsen.muhammad.minimalist.data.files.Chapter
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import mohsen.muhammad.minimalist.data.files.FileMetadata
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
		Track.update()
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

	var seekJump: Int
		get() = sharedPreferences.getInt(Key.SEEK_JUMP, 60) // 1 minute default
		set(value) = sharedPreferences.put(Key.SEEK_JUMP, value)

	var sleepTimerActive: Boolean = false
	var sleepTimer: Int
		get() = sharedPreferences.getInt(Key.SLEEP_TIMER, 60) // 1 hour default
		set(value) = sharedPreferences.put(Key.SLEEP_TIMER, value)

	var nightMode: Int
		get() = sharedPreferences.getInt(Key.NIGHT_MODE, -1) // default is FOLLOW_SYSTEM
		set(value) = sharedPreferences.put(Key.NIGHT_MODE, value)

	// current track state props
	// because getting the metadata is expensive, they're obtained once and stored here
	object Track {

		val exists
			get() = File(path).exists()

		var path: String
			get() = sharedPreferences.getString(Key.PATH, String.EMPTY) ?: String.EMPTY
			set(value) = sharedPreferences.put(Key.PATH, value)

		var title= String.EMPTY
		var album = String.EMPTY
		var artist = String.EMPTY
		var duration = 0L
		var albumArt: ByteArray? = null

		var chapters: ArrayList<Chapter> = ArrayList()
		val hasChapters: Boolean
			get() = chapters.size > 1

		var seek: Int
			get() = sharedPreferences.getInt(Key.SEEK, 0)
			set(value) = sharedPreferences.put(Key.SEEK, value)

		val readableSeek: String
			get() = formatMillis(seek.toLong())

		val readableDuration: String
			get() = formatMillis(duration)

		fun update(filePath: String = String.EMPTY) {
			path = filePath.ifBlank { path }

			val f = File(path)
			if (!ExplorerFile.isTrack(f)) return

			// Play Store keeps saying that the ffmpeg.setDataSource throws an illegalArgumentException...to me the above check should fix that, but it didn't
			// so I just try/catch the fucker
			try {
				val metadata = FileMetadata(f)
				title = metadata.title
				album = metadata.album
				artist = metadata.artist
				duration = metadata.duration
				chapters = metadata.chapters
				albumArt = metadata.albumArt

			} catch (ex: Exception) { Log.e("State", "State.Track.update", ex) }
		}
	}

	lateinit var playlist: Playlist

	val isSelectModeActive: Boolean
		get() = selectedTracks.isNotEmpty()

	val selectedTracks = ArrayList<String>()
	fun updateSelectedTracks(track: String): Int {
		return if (selectedTracks.contains(track)) { // track already in the list, remove it
			selectedTracks.remove(track)
			if (selectedTracks.isEmpty()) EventType.SELECT_MODE_INACTIVE else EventType.SELECT_MODE_SUB // if the list is empty, deactivate the select mode

		} else {
			selectedTracks.add(track)
			EventType.SELECT_MODE_ADD
		}
	}

	var isSearchModeActive = false

	// the shared preferences keys
	internal object Key {
		const val DIRECTORY = "CurrentDirectory"
		const val PLAYLIST = "Playlist"
		const val PATH = "CurrentTrack"
		const val SEEK = "Seek"
		const val SEEK_JUMP = "SeekJump"
		const val SLEEP_TIMER = "SleepTimer"
		const val NIGHT_MODE = "NightMode"

		const val REPEAT = "Repeat"
		const val SHUFFLE = "Shuffle"
	}
}

package mohsen.muhammad.minimalist.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.core.ext.EMPTY
import mohsen.muhammad.minimalist.core.ext.formatMillis
import mohsen.muhammad.minimalist.data.files.FileHelper
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * holds application-wide state variables (such as current directory, shuffle/repeat states, etc.)
 * it's also responsible for persisting those variables (in shared preferences)
 */

@SuppressLint("StaticFieldLeak")
object State {

	private lateinit var context: Context // holds the application context...don't worry
	private lateinit var sharedPreferences: SharedPreferences

	fun initialize(applicationContext: Context) {
		context = applicationContext
		sharedPreferences = context.getSharedPreferences(Key.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)
	}

	var currentDirectory: File
		get() {
			val savedPath = sharedPreferences.getString(Key.DIRECTORY, String.EMPTY)

			if (savedPath != String.EMPTY) {
				val savedFile = File(savedPath)

				// only return the saved file if it exists
				// it may not exist due to the file being removed, or the SD card being unmounted!
				if (savedFile.exists()) return savedFile
			}

			return File(FileHelper.ROOT)
		}
		set(value) {
			sharedPreferences.edit()
				.putString(Key.DIRECTORY, value.absolutePath)
				.apply()
		}

	val isPlaying: Boolean
		get() = PlaybackManager.isPlaying

	// current track state props
	// because getting the metadata is expensive, they're obtained once and stored here
	object Track {

		val isInitialized
			get() = path.isNotBlank()

		var path: String
			get() {
				return sharedPreferences.getString(Key.PATH, String.EMPTY) ?: String.EMPTY
			}
			set(value) {
				sharedPreferences.edit()
					.putString(Key.PATH, value)
					.apply()
			}

		var title: String
			get() {
				return sharedPreferences.getString(Key.TITLE, String.EMPTY) ?: String.EMPTY
			}
			set(value) {
				sharedPreferences.edit()
					.putString(Key.TITLE, value)
					.apply()
			}

		var album: String
			get() {
				return sharedPreferences.getString(Key.ALBUM, String.EMPTY) ?: String.EMPTY
			}
			set(value) {
				sharedPreferences.edit()
					.putString(Key.ALBUM, value)
					.apply()
			}

		var artist: String
			get() {
				return sharedPreferences.getString(Key.ARTIST, String.EMPTY) ?: String.EMPTY
			}
			set(value) {
				sharedPreferences.edit()
					.putString(Key.ARTIST, value)
					.apply()
			}

		var duration: Long
			get() {
				return sharedPreferences.getLong(Key.DURATION, 0L)
			}
			set(value) {
				sharedPreferences.edit()
					.putLong(Key.DURATION, value)
					.apply()
			}

		val readableDuration: String
			get() = formatMillis(duration)

		var seek: Int
			get() {
				return sharedPreferences.getInt(Key.SEEK, 0)
			}
			set(value) {
				sharedPreferences.edit()
					.putInt(Key.SEEK, value)
					.apply()
			}

		val readableSeek: String
			get() = formatMillis(seek.toLong())
	}

	// playlist state props
	object Playlist {
		var repeat: Int
			get() {
				return sharedPreferences.getInt(Key.REPEAT, RepeatMode.INACTIVE)
			}
			set(value) {
				sharedPreferences.edit()
					.putInt(Key.REPEAT, value)
					.apply()
			}

		var shuffle: Boolean
			get() {
				return sharedPreferences.getBoolean(Key.SHUFFLE, false)
			}
			set(value) {
				sharedPreferences.edit()
					.putBoolean(Key.SHUFFLE, value)
					.apply()
			}

		// we may need to store the playlist into SQLite. We'll see.
		var playlist: ArrayList<String>
			get() {
				// get a semi colon-separated string
				val savedPlaylist = sharedPreferences.getString(Key.PLAYLIST, String.EMPTY) ?: String.EMPTY
				return ArrayList(savedPlaylist.split(";").takeWhile { it.isNotEmpty() })
			}
			set(value) {
				sharedPreferences.edit()
					.putString(Key.PLAYLIST, value.joinToString(";"))
					.apply()
			}
	}

	// the shared preferences keys
	internal object Key {
		const val MINIMALIST_SHARED_PREFERENCES = "Minimalist"

		const val DIRECTORY = "CurrentDirectory"
		const val PLAYLIST = "Playlist"

		const val PATH = "CurrentTrack"
		const val TITLE = "Title"
		const val ALBUM = "Album"
		const val ARTIST = "Artist"
		const val DURATION = "Duration"

		const val REPEAT = "Repeat"
		const val SHUFFLE = "Shuffle"

		const val SEEK = "Seek"
	}
}

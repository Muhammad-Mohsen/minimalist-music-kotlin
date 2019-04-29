package mohsen.muhammad.minimalist.data

import android.annotation.SuppressLint
import android.content.Context
import mohsen.muhammad.minimalist.core.ext.EMPTY
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
	val sharedPreferences = context.getSharedPreferences(Key.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)

	fun initialize(app: Context) {
		context = app
	}

	var currentDirectory: File
		get() {
			val savedPath = sharedPreferences.getString(Key.DIRECTORY_PREFERENCE, String.EMPTY)

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
				.putString(Key.DIRECTORY_PREFERENCE, value.absolutePath)
				.apply()
		}

	var currentTrack: String
		get() {
			return sharedPreferences.getString(Key.TRACK_PREFERENCE, String.EMPTY) ?: String.EMPTY
		}
		set(value) {
			sharedPreferences.edit()
				.putString(Key.TRACK_PREFERENCE, value)
				.apply()
		}

	var currentSeek: Float
		get() {
			return sharedPreferences.getFloat(Key.SEEK, 0F)
		}
		set(value) {
			sharedPreferences.edit()
				.putFloat(Key.SEEK, value)
				.apply()
		}

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
			val savedPlaylist = sharedPreferences.getString(Key.PLAYLIST_PREFERENCE, String.EMPTY) ?: String.EMPTY
			return ArrayList(savedPlaylist.split(";").takeWhile { it.isNotEmpty() })
		}
		set(value) {
			sharedPreferences.edit()
				.putString(Key.TRACK_PREFERENCE, value.joinToString(";"))
				.apply()
		}

	// the shared preferences keys
	internal object Key {
		const val MINIMALIST_SHARED_PREFERENCES = "Minimalist"

		const val DIRECTORY_PREFERENCE = "CurrentDirectory"
		const val TRACK_PREFERENCE = "CurrentTrack"
		const val PLAYLIST_PREFERENCE = "Playlist"

		const val REPEAT = "Repeat"
		const val SHUFFLE = "Shuffle"

		const val SEEK = "Seek"
	}
}

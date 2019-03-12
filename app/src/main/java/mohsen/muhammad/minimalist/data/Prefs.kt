package mohsen.muhammad.minimalist.data

import android.content.Context
import mohsen.muhammad.minimalist.core.EMPTY
import mohsen.muhammad.minimalist.core.FileHelper
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * holds application-wide state variables (such as current directory, active context variables)
 * it's also responsible for persisting those variables (in shared preferences)
 */
object Prefs {
	private const val MINIMALIST_SHARED_PREFERENCE = "Minimalist"

	private const val DIRECTORY_SHARED_PREFERENCE = "CurrentDirectory"

	private const val TRACK_SHARED_PREFERENCE = "CurrentTrack"
	private const val PLAYLIST_SHARED_PREFERENCE = "Playlist"
	//
	// current directory
	//
	fun setCurrentDirectory(context: Context, directory: File) {
		context.getSharedPreferences(MINIMALIST_SHARED_PREFERENCE, Context.MODE_PRIVATE).edit()
			.putString(DIRECTORY_SHARED_PREFERENCE, directory.absolutePath)
			.apply()
	}

	fun getCurrentDirectory(context: Context): File {
		val sharedPreferences = context.getSharedPreferences(MINIMALIST_SHARED_PREFERENCE, Context.MODE_PRIVATE)
		val savedPath = sharedPreferences.getString(DIRECTORY_SHARED_PREFERENCE, String.EMPTY)

		if (savedPath != String.EMPTY) {
			val savedFile = File(savedPath)

			// only return the saved file if it exists
			// it may not exist due to the file being removed, or the SD card being unmounted!
			if (savedFile.exists()) return savedFile
		}

		return File(FileHelper.ROOT)
	}
	//
	// current track
	//
	var currentTrack: String? = null

	fun getSavedCurrentTrack(context: Context): String? {
		val sharedPreferences = context.getSharedPreferences(MINIMALIST_SHARED_PREFERENCE, Context.MODE_PRIVATE)
		val savedCurrentTrack = sharedPreferences.getString(
			TRACK_SHARED_PREFERENCE,
			String.EMPTY
		)

		return if (savedCurrentTrack != String.EMPTY) savedCurrentTrack else null

	}

	fun saveCurrentTrack(context: Context, currentTrack: String) {
		val sharedPreferences = context.getSharedPreferences(MINIMALIST_SHARED_PREFERENCE, Context.MODE_PRIVATE)
		val editor = sharedPreferences.edit()

		editor.putString(TRACK_SHARED_PREFERENCE, currentTrack)
		editor.apply()
	}
	//
	// playlist
	//
	// we may need to store the playlist into SQLite. We'll see.
	fun getSavedPlaylistItems(context: Context): ArrayList<String> {
		val sharedPreferences = context.getSharedPreferences(MINIMALIST_SHARED_PREFERENCE, Context.MODE_PRIVATE)

		// get a semi colon-separated string
		val savedPlaylist = sharedPreferences.getString(
			PLAYLIST_SHARED_PREFERENCE,
			String.EMPTY
		) ?: String.EMPTY

		// this'll most likely throw, but let's see!
		val playlistItems = ArrayList<String>()
		playlistItems.addAll(savedPlaylist.split(";").takeLastWhile { it.isNotBlank() })
		return playlistItems
	}

	fun savePlaylist(context: Context, playlist: ArrayList<String>) {
		val sharedPreferences = context.getSharedPreferences(MINIMALIST_SHARED_PREFERENCE, Context.MODE_PRIVATE)
		val editor = sharedPreferences.edit()

		val serialized = StringBuilder()
		for (s in playlist) {
			serialized
				.append(s)
				.append(";")
		}

		editor.putString(TRACK_SHARED_PREFERENCE, serialized.toString())
		editor.apply()
	}
}
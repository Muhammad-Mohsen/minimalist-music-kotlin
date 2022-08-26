package mohsen.muhammad.minimalist.data

import android.content.SharedPreferences
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.core.ext.EMPTY
import mohsen.muhammad.minimalist.core.ext.formatMillis
import mohsen.muhammad.minimalist.core.ext.put
import mohsen.muhammad.minimalist.data.files.Chapter
import mohsen.muhammad.minimalist.data.files.FileMetadata
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * holds application-wide state variables (such as current directory, shuffle/repeat states, etc.)
 * it's also responsible for persisting those variables (in shared preferences)
 * State should never be registered with the EventBus...the async nature of EventBus would prevent the State from being 'trustworthy'
 */

object State {

	private lateinit var sharedPreferences: SharedPreferences // holds the application context...don't worry

	fun initialize(prefs: SharedPreferences) {
		sharedPreferences = prefs
		playlist = Playlist(prefs)
		Track.update()
	}

	var currentDirectory: File
		get() {
			val savedPath = sharedPreferences.getString(Key.DIRECTORY, null) ?: FileMetadata.ROOT
			val savedFile = File(savedPath)
			return if (savedFile.exists()) savedFile else File(FileMetadata.ROOT) // only return the saved file if it exists (it could've been removed, or that the SD card is unmounted!)
		}
		set(value) = sharedPreferences.put(Key.DIRECTORY, value.absolutePath)

	val isPlaying: Boolean
		get() = PlaybackManager.isPlaying

	// current track state props
	// because getting the metadata is expensive, they're obtained once and stored here
	object Track {

		val isInitialized
			get() = path.isNotBlank()

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
			val p = filePath.ifBlank { path }
			if (p.isBlank()) return

			val metadata = FileMetadata(File(p))
			path = p

			title = metadata.title
			album = metadata.album
			artist = metadata.artist
			duration = metadata.duration
			chapters = metadata.chapters
			albumArt = metadata.albumArt
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

	// the shared preferences keys
	internal object Key {
		const val DIRECTORY = "CurrentDirectory"
		const val PLAYLIST = "Playlist"
		const val PATH = "CurrentTrack"
		const val SEEK = "Seek"

		const val REPEAT = "Repeat"
		const val SHUFFLE = "Shuffle"
	}
}

package mohsen.muhammad.minimalist.data

import android.annotation.SuppressLint
import android.content.Context
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

@SuppressLint("StaticFieldLeak")
object State {

	private lateinit var context: Context // holds the application context...don't worry
	private lateinit var sharedPreferences: SharedPreferences

	fun initialize(applicationContext: Context) {
		context = applicationContext
		sharedPreferences = context.getSharedPreferences(Key.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)

		playlist = Playlist(context)
	}

	var currentDirectory: File
		get() {
			val savedPath = sharedPreferences.getString(Key.DIRECTORY, null)

			if (savedPath != null) {
				val savedFile = File(savedPath)
				if (savedFile.exists()) return savedFile // only return the saved file if it exists (it could've been removed, or that the SD card is unmounted!)
			}

			return File(FileMetadata.ROOT)
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
			get() = sharedPreferences.getString(Key.PATH, String.EMPTY) ?: String.EMPTY
			set(value) = sharedPreferences.put(Key.PATH, value)

		var title: String
			get() = sharedPreferences.getString(Key.TITLE, String.EMPTY) ?: String.EMPTY
			set(value) = sharedPreferences.put(Key.TITLE, value)


		var album: String
			get() = sharedPreferences.getString(Key.ALBUM, String.EMPTY) ?: String.EMPTY
			set(value) = sharedPreferences.put(Key.ALBUM, value)

		var artist: String
			get() = sharedPreferences.getString(Key.ARTIST, String.EMPTY) ?: String.EMPTY
			set(value) = sharedPreferences.put(Key.ARTIST, value)

		var duration: Long
			get() = sharedPreferences.getLong(Key.DURATION, 0L)
			set(value) = sharedPreferences.put(Key.DURATION, value)

		private var chapterCount: Int
			get() = sharedPreferences.getInt(Key.CHAPTER_COUNT, 0)
			set(value) = sharedPreferences.put(Key.CHAPTER_COUNT, value)

		val hasChapters = chapterCount > 1

		private var _chapters: ArrayList<Chapter>? = null
		var chapters: ArrayList<Chapter>
			get() {
				_chapters?.let { return it }
				_chapters = Chapter.deserialize(sharedPreferences.getString(Key.CHAPTERS, String.EMPTY))
				return _chapters!!
			}
			set(value) {
				_chapters = value
				sharedPreferences.put(Key.CHAPTERS, Chapter.serialize(value))
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

		fun update(filePath: String) {
			val metadata = FileMetadata(File(filePath))
			path = filePath

			title = metadata.title
			album = metadata.album
			artist = metadata.artist
			duration = metadata.duration
			chapterCount = metadata.chapterCount
			chapters = metadata.chapters
		}
	}

	lateinit var playlist: Playlist

	val isSelectModeActive: Boolean
		get() = selectedTracks.count() > 0

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

		const val CHAPTER_COUNT = "ChapterCount"
		const val CHAPTERS = "Chapters"
	}
}

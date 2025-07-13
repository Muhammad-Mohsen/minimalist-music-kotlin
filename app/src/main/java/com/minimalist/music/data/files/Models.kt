package com.minimalist.music.data.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Base64
import androidx.annotation.RequiresApi
import com.minimalist.music.foundation.ext.EMPTY
import com.minimalist.music.data.Const
import com.minimalist.music.data.state.State
import org.json.JSONObject
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File
import java.io.FileFilter
import java.util.Arrays


/**
 * Created by muhammad.mohsen on 4/15/2017.
 */

/**
 * not really useful as a model class anymore, honestly...at the start of development, I used to display the track count for folders and the album/artist for tracks
 * now, only the statics are useful
 */
class ExplorerFile(pathname: String)
	: File(pathname) {

	val type = if (isDirectory) "dir" else "track"

	// FileFilter implementation that accepts directory/media files.
	private class ExplorerFileFilter : FileFilter {
		override fun accept(file: File): Boolean {
			return file.isDirectory || MEDIA_EXTENSIONS.contains(file.extension)
		}
	}

	companion object {

		val ROOT: String = Environment.getExternalStorageDirectory().path // root directory (actually the internal storage directory!)

		// these const's help work around the nuisances of Android storage APIs
		const val ACTUAL_ROOT = "/storage"
		private const val EMULATED = "/storage/emulated"
		private const val EMULATED_ZERO = "/storage/emulated/0"

		val MEDIA_EXTENSIONS = listOf("mp3", "wav", "m4b", "m4a", "flac", "midi", "ogg", "opus", "aac") // supported media extensions

		private val filter = ExplorerFileFilter()

		fun isAtRoot(path: String?): Boolean {
			return (path?.filter { it == '/' }?.length ?: 1) <= 1
		}

		fun isTrack(f: File): Boolean {
			return f.exists() && MEDIA_EXTENSIONS.contains(f.extension.lowercase())
		}

		fun listFiles(path: String, sortBy: String): ArrayList<ExplorerFile> {
			val fileModels = ArrayList<ExplorerFile>()

			var files = File(path).listFiles(filter)

			// just to make sure that we aren't trapped at the basement
			if (path == EMULATED && files == null) files = arrayOf(File(EMULATED_ZERO))
			else if (path == ACTUAL_ROOT && files == null) {
				files = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) listVolumes()
				else arrayOf(File(EMULATED))
			}

			if (files == null) return ArrayList()

			// this sucks!!
			when (sortBy) {
				SortBy.ZA -> {
					Arrays.sort(files) { o2, o1 ->
						if (o1.isDirectory && !o2.isDirectory) 1
						else if (o2.isDirectory && !o1.isDirectory) -1
						else o1.name.compareTo(o2.name, true)
					}
				}
				SortBy.NEWEST -> {
					Arrays.sort(files) { o2, o1 ->
						if (o1.isDirectory && !o2.isDirectory) 1
						else if (o2.isDirectory && !o1.isDirectory) -1
						else (o1.lastModified() - o2.lastModified()).toInt()
					}
				}
				SortBy.OLDEST -> {
					Arrays.sort(files) { o1, o2 ->
						if (o1.isDirectory && !o2.isDirectory) -1
						else if (o2.isDirectory && !o1.isDirectory) 1
						else (o1.lastModified() - o2.lastModified()).toInt()
					}
				}
				else -> { // default (A to Z)
					Arrays.sort(files) { o1, o2 ->
						if (o1.isDirectory && !o2.isDirectory) -1 // if the first is a directory, it's always first
						else if (o2.isDirectory && !o1.isDirectory) 1 // if the second is a directory, it's always first
						else o1.name.compareTo(o2.name, true) // if both are tracks, compare their names
					}
				}
			}

			for (f in files) fileModels.add(ExplorerFile(f.absolutePath))
			return fileModels
		}

		// After the scoped storage changes, we can't access the SD card from "/storage".listFiles() anymore :)
		// this little guy returns them nonetheless
		@RequiresApi(Build.VERSION_CODES.R)
		private fun listVolumes(): Array<File> {
			val context = State.applicationContext

			val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
			return ArrayList(storageManager.storageVolumes.mapNotNull {
				if (it.directory?.absolutePath == EMULATED_ZERO) File(EMULATED) // leaving this as is, causes a minor navigation problem...we end up with the breadcrumbs looking like storage/0/0 when navigating
				else it.directory

			}).toTypedArray()
		}
	}
}

/**
 * uses ffmpeg metadata retriever because it can list chapters
 */
class FileMetadata(file: File) {
	init {
		retriever.setDataSource(file.path)
	}

	val title: String = file.name
	val artist = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST) ?: String.EMPTY
	val album = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM) ?: file.parentFile?.name ?: String.EMPTY
	val duration = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
	val albumArtBitmap = SerializableBitmap(retriever.embeddedPicture ?: ByteArray(0))

	val lyrics = retriever.extractMetadata("UNSYNCEDLYRICS") ?: retriever.extractMetadata("lyrics-eng") ?: String.EMPTY

	private val chapterCount = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT)?.toInt() ?: 0
	val chapters = ArrayList((0 until chapterCount).map {
		Chapter(it,
			retriever.extractMetadataFromChapter(FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME, it)?.toLong() ?: 0,
			retriever.extractMetadataFromChapter(FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE, it),
		)
	})

	companion object {
		private val retriever = FFmpegMediaMetadataRetriever()
	}
}

/**
 * Holds a track's chapter info
 */
data class Chapter(val index: Int, val startTime: Long, val title: String)

// the ArrayList is guaranteed to be sorted
fun ArrayList<Chapter>.getNextChapter(currentSeek: Long): Chapter? {
	return this.firstOrNull {
		it.startTime > currentSeek
	}
}
fun ArrayList<Chapter>.getPrevChapter(currentSeek: Long): Chapter {
	return this.last {
		it.startTime < currentSeek - Const.PREV_THRESHOLD // less than 5 seconds since the chapter began
	}
}
fun ArrayList<Chapter>.serializeChapters(): List<JSONObject> {
	return this.map {
		val obj = JSONObject()
		obj.put("index", it.index)
		obj.put("startTime", it.startTime)
		obj.put("title", it.title)

		obj
	}
}

fun ArrayList<ExplorerFile>.serializeFiles(): List<JSONObject> {
	return this.map {
		val obj = JSONObject()
		obj.put("type", if (it.isDirectory) "dir" else "track")
		obj.put("path", it.absolutePath)
		obj.put("name", it.name)

		obj
	}
}

class SerializableBitmap(val data: ByteArray?) {
	val decoded: Bitmap? = BitmapFactory.decodeByteArray(data, 0, data?.size ?: 0)
	val encoded: String? = Base64.encodeToString(data, Base64.DEFAULT)
}

/**
 * ENUMS
 */
object RepeatMode {
	const val INACTIVE = 0 // inactive
	const val ACTIVE = 1 // active
	const val REPEAT_ONE = 2 // repeat-one

	val list = arrayOf(INACTIVE, ACTIVE, REPEAT_ONE)
}

object Theme {
	const val DARK = "dark"
	const val LIGHT = "light"
}

object SortBy {
	const val AZ = "az"
	const val ZA = "za"
	const val NEWEST = "newest"
	const val OLDEST = "oldest"
}
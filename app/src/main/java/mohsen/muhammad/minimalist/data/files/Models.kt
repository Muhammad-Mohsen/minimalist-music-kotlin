package mohsen.muhammad.minimalist.data.files

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import mohsen.muhammad.minimalist.core.ext.EMPTY
import mohsen.muhammad.minimalist.data.Const
import java.io.File
import java.io.FileFilter
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by muhammad.mohsen on 4/15/2017.
 */

/**
 * Holds a file's metadata.
 * Offers no distinction between a music file and a directory...just like java
 * Metadata is obtained asynchronously via the MetadataAsyncTask (no longer the case)
 */
class ExplorerFile(pathname: String, var album: String = String.EMPTY, var artist: String = String.EMPTY, var duration: String = String.EMPTY, var trackCount: Int = 0)
	: File(pathname) {

	// FileFilter implementation that accepts directory/media files.
	private class ExplorerFileFilter : FileFilter {
		override fun accept(file: File): Boolean {
			return file.isDirectory || MEDIA_EXTENSIONS.contains(file.extension)
		}
	}

	companion object {

		const val ROOT = "/storage" // root directory
		val MEDIA_EXTENSIONS = listOf("mp3", "wav", "m4b", "m4a", "flac", "midi", "ogg") // supported media extensions

		private val filter = ExplorerFileFilter()

		fun isTrack(f: File): Boolean {
			return MEDIA_EXTENSIONS.contains(f.extension.lowercase())
		}

		fun listExplorerFiles(path: String): ArrayList<ExplorerFile> {
			val fileModels = ArrayList<ExplorerFile>()

			var files = File(path).listFiles(filter)
			if (path == "/storage/emulated") files = arrayOf(File("/storage/emulated/0"))
			else if (files == null) return ArrayList()

			Arrays.sort(files) { o1, o2 ->
				if (o1.isDirectory && o2.isDirectory) o1.name.compareTo(o2.name, true) // if both are directories, compare their names
				else if (o1.isDirectory && !o2.isDirectory) -1 // if the first is a directory, it's always first
				else if (o2.isDirectory) 1 // if the second is a directory, it's always first
				else o1.name.compareTo(o2.name, true) // if both are tracks, compare their names
			}

			for (f in files) fileModels.add(ExplorerFile(f.absolutePath))
			return fileModels
		}
	}

}

/**
 * Holds a track's chapter info
 */
class Chapter(val index: Int, val startTime: Long) {
	companion object {
		fun serialize(chapters: ArrayList<Chapter>): String {
			return chapters.joinToString(separator = "&&", transform = {
				it.index.toString() + "," + it.startTime.toString()
			})
		}

		fun deserialize(s: String?): ArrayList<Chapter> {
			if (s.isNullOrBlank()) return ArrayList()
			val chapters = s.split("&&").map {
				val props = it.split(",")
				Chapter(props.first().toInt(), props.last().toLong())
			}

			return ArrayList(chapters)
		}
	}
}
// the ArrayList is guaranteed to be sorted
fun ArrayList<Chapter>.getNextChapter(currentSeek: Long): Chapter {
	return this.first {
		it.startTime > currentSeek
	}
}
fun ArrayList<Chapter>.getPrevChapter(currentSeek: Long): Chapter {
	return this.last {
		it.startTime < currentSeek - Const.PREV_THRESHOLD // less than 5 seconds since the chapter began
	}
}

class SerializableBitmap(val data: ByteArray?) {
	private val bitmapDecoded = BitmapFactory.decodeByteArray(data, 0, data?.size ?: 0)
	private val bitmapEncoded = Base64.encodeToString(data, Base64.DEFAULT)

	val bitmap: Bitmap? = bitmapDecoded
	val serialize: String = bitmapEncoded
}
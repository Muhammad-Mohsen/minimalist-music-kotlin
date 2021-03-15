package mohsen.muhammad.minimalist.data.files

import mohsen.muhammad.minimalist.core.ext.EMPTY
import mohsen.muhammad.minimalist.data.Const
import java.io.File


/**
 * Created by muhammad.mohsen on 4/15/2017.
 */

/**
 * Holds a file's metadata.
 * Offers no distinction between a music file and a directory...just like java
 * Metadata is obtained asynchronously via the MetadataAsyncTask (no longer the case)
 */
class ExplorerFile(pathname: String, var album: String = String.EMPTY, var artist: String = String.EMPTY, var duration: String = String.EMPTY, var trackCount: Int = 0)
	: File(pathname)

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
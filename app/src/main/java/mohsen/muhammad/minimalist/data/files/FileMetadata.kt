package mohsen.muhammad.minimalist.data.files

import mohsen.muhammad.minimalist.core.ext.EMPTY
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * contains methods to help with listing files, sorting, etc
 */

class FileMetadata(private val file: File) {

	private val retriever = FFmpegMediaMetadataRetriever()
	init {
		retriever.setDataSource(file.path)
	}

	val title: String
		get() {
			return file.nameWithoutExtension
		}

	val artist: String
		get() {
			val artist: String? = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST)
			return artist?: String.EMPTY
		}

	val album: String
		get() {
			val album: String? = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM)
			return album?: file.parentFile?.name ?: String.EMPTY
		}

	val duration: Long
		get() {
			val durationString: String? = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)
			return durationString?.toLong() ?: 0L
		}

	val trackCount: Int
		get() {
			if (file.isDirectory) {
				// FileFilter implementation that accepts media files defined by the media extensions string array
				val tracks = file.listFiles { file: File -> ExplorerFile.MEDIA_EXTENSIONS.contains(file.extension.lowercase()) }
				if (tracks != null) return tracks.size
			}

			return 0
		}

	private val chapterCount: Int
		get() = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT)?.toInt() ?: 0

	val albumArt: ByteArray?
		get() = retriever.embeddedPicture

	val chapters: ArrayList<Chapter>
		get() {
			return ArrayList((0 until chapterCount).map {
				Chapter(it, getChapterStartTime(it))
			})
		}

	private fun getChapterStartTime(i: Int): Long =
		retriever.extractMetadataFromChapter(FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME, i)?.toLong() ?: 0

}

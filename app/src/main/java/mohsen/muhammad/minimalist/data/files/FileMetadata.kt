package mohsen.muhammad.minimalist.data.files

import mohsen.muhammad.minimalist.core.ext.EMPTY
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * uses ffmpeg metadata retriever because it can list chapters
 */

class FileMetadata(private val file: File) {

	init {
		retriever.setDataSource(file.path)
	}

	val title: String
		get() = file.nameWithoutExtension

	val artist: String
		get() = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST) ?: String.EMPTY

	val album: String
		get() = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM) ?: file.parentFile?.name ?: String.EMPTY

	val duration: Long
		get() = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L

	private val chapterCount: Int
		get() = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT)?.toInt() ?: 0

	val albumArt: ByteArray?
		get() = retriever.embeddedPicture

	val chapters: ArrayList<Chapter>
		get() = ArrayList((0 until chapterCount).map {
			Chapter(it, getChapterStartTime(it))
		})

	private fun getChapterStartTime(i: Int): Long =
		retriever.extractMetadataFromChapter(FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME, i)?.toLong() ?: 0

	companion object {
		private val retriever = FFmpegMediaMetadataRetriever()
	}

}

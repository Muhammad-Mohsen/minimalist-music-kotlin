package mohsen.muhammad.minimalist.data.files

import mohsen.muhammad.minimalist.core.ext.EMPTY
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File
import java.io.FileFilter
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * contains methods to help with listing files, sorting, etc
 * instance APIs are no longer used!!
 */

class FileMetadata(private val file: File) {

	private val retriever = FFmpegMediaMetadataRetriever()

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
				val tracks = file.listFiles(MediaFileFilter())
				if (tracks != null) return tracks.size
			}

			return 0
		}

	val chapterCount: Int
		get() {
			return retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT)?.toInt() ?: 0
		}

	val hasChapters: Boolean
		get() = chapterCount > 0

	val chapters: ArrayList<Chapter>
		get() {
			val list = ArrayList<Chapter>()

			(0 until chapterCount).forEach {
				list.add(Chapter(it, getChapterStartTime(it)))
			}

			return list
		}

	private fun getChapterStartTime(i: Int): Long {
		return retriever.extractMetadataFromChapter(FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME, i)?.toLong() ?: 0
	}

	init {
		if (isTrack(file)) retriever.setDataSource(file.path)
	}

	// FileFilter implementation that accepts media files defined by the media extensions string array
	private class MediaFileFilter : FileFilter {

		override fun accept(file: File): Boolean {
			return MEDIA_EXTENSIONS.contains(file.extension)
		}
	}

	// FileFilter implementation that accepts directory/media files.
	private class ExplorerFileFilter : FileFilter {

		override fun accept(file: File): Boolean {
			return file.isDirectory || MEDIA_EXTENSIONS.contains(file.extension)
		}
	}

	private class FileComparator : Comparator<File> {
		override fun compare(o1: File, o2: File): Int {
			return if (o1.isDirectory && o2.isDirectory) o1.name.compareTo(o2.name, true) // if both are directories, compare their names
			else if (o1.isDirectory && !o2.isDirectory) -1 // if the first is a directory, it's always first
			else if (o2.isDirectory) 1 // if the second is a directory, it's always first
			else o1.name.compareTo(o2.name, true) // if both are tracks, compare their names
		}
	}

	companion object {

		const val ROOT = "/storage" // root directory
		private val MEDIA_EXTENSIONS = listOf("mp3", "MP3", "wav", "m4b", "mp4", "m4a", "ogg", "flac") // supported media extensions

		private val filter = ExplorerFileFilter()

		private fun isTrack(f: File): Boolean {
			return MEDIA_EXTENSIONS.contains(f.extension)
		}

		fun listExplorerFiles(path: String): ArrayList<ExplorerFile> {
			val fileModels = ArrayList<ExplorerFile>()

			var files = File(path).listFiles(filter)

			if (path == "/storage/emulated") files = arrayOf(File("/storage/emulated/0"))
			else if (files == null) return ArrayList()

			Arrays.sort(files, FileComparator())

			for (f in files) fileModels.add(ExplorerFile(f.absolutePath))

			return fileModels
		}
	}
}

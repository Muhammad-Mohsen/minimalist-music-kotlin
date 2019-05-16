package mohsen.muhammad.minimalist.data.files

import android.media.MediaMetadataRetriever
import mohsen.muhammad.minimalist.core.ext.EMPTY
import mohsen.muhammad.minimalist.core.ext.formatMillis
import java.io.File
import java.io.FileFilter
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * contains methods to help with listing files, sorting, etc
 */

class FileHelper(private val file: File) {

	private val retriever: MediaMetadataRetriever = MediaMetadataRetriever()

	val title: String
		get() {
			return file.nameWithoutExtension
		}

	// metadata getters
	val artist: String
		get() {
			val artist: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
			return artist?: String.EMPTY
		}

	val album: String
		get() {
			val album: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
			return album?: file.parentFile.name
		}

	val duration: Long
		get() {
			val durationString: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
			return durationString?.toLong() ?: 0L
		}

	// format the duration string
	val readableDuration: String?
		get() = formatMillis(duration)

	val trackCount: Int
		get() {
			if (file.isDirectory) {
				val tracks = file.listFiles(MediaFileFilter())
				if (tracks != null)
					return tracks.size
			}

			return 0
		}

	init {
		if (isTrack(file))
			retriever.setDataSource(file.path)
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
			return if (o1.isDirectory && o2.isDirectory)
			// if both are directories, compare their names
				o1.name.compareTo(o2.name, true)
			else if (o1.isDirectory && !o2.isDirectory)
			// if the first is a directory, it's always first
				-1
			else if (o2.isDirectory)
			// if the second is a directory, it's always first
				1
			else
				o1.name.compareTo(o2.name, true) // if both are tracks, compare their names
		}
	}

	companion object {

		const val ROOT = "/storage" // root directory
		private val MEDIA_EXTENSIONS = Arrays.asList("mp3", "wav") // supported media extensions

		private val filter = ExplorerFileFilter()

		private fun isTrack(f: File): Boolean {
			return MEDIA_EXTENSIONS.contains(f.extension)
		}

		fun listFileModels(path: String): ArrayList<ExplorerFile> {
			val fileModels = ArrayList<ExplorerFile>()

			var files = File(path).listFiles(filter)

			if (path == "/storage/emulated") files = arrayOf(File("/storage/emulated/0"))
			else if (files == null) return ArrayList()

			Arrays.sort(files, FileComparator())

			for (f in files)
				fileModels.add(ExplorerFile(f.absolutePath))

			return fileModels
		}
	}
}

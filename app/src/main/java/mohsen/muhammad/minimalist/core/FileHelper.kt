package mohsen.muhammad.minimalist.core

import android.media.MediaMetadataRetriever
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import java.io.File
import java.io.FileFilter
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * contains methods to help with listing files, sorting, etc
 */

class FileHelper (private val mFile: File) {

    private val mMetadataRetriever: MediaMetadataRetriever = MediaMetadataRetriever()

    // metadata getters
    // TODO extract string resource
    val artist: String
        get() {
            var artist: String? = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            if (artist == null || artist == "")
                artist = "Unknown"

            return artist
        }
    // TODO extract string resource
    val album: String
        get() {
            var album: String? = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

            if (album == null || album == "")
                album = "Unknown"

            return album
        }
    // format the duration string
    val duration: String?
        get() {
            var duration: String? = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

            if (duration != null) {
                val longDuration = java.lang.Long.parseLong(duration)
                duration = String.format(
                    Locale("US"), "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(longDuration),
                    TimeUnit.MILLISECONDS.toSeconds(longDuration) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            longDuration
                        )
                    )
                )
            }

            return duration
        }
    val trackCount: Int
        get() {
            if (mFile.isDirectory) {
                val tracks = mFile.listFiles(MediaFileFilter())
                if (tracks != null)
                    return tracks.size
            }

            return 0
        }

    init {

        if (isTrack(mFile))
            mMetadataRetriever.setDataSource(mFile.path)
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

        private fun isTrack(f: File): Boolean {
            return MEDIA_EXTENSIONS.contains(f.extension)
        }

        // sigh
        // http://stackoverflow.com/questions/1445233/is-it-possible-to-solve-the-a-generic-array-of-t-is-created-for-a-varargs-param
        fun listFileModels(path: String): ArrayList<ExplorerFile> {
            val fileModels = ArrayList<ExplorerFile>()

            val files = File(path).listFiles(ExplorerFileFilter()) ?: return ArrayList()

            Arrays.sort(files, FileComparator())

            for (f in files)
                fileModels.add(ExplorerFile(f.absolutePath))

            // MetadataAsyncTask().execute(fileModels)

            return fileModels
        }
    }
}

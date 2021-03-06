package mohsen.muhammad.minimalist.data.files

import java.io.File

/**
 * Created by muhammad.mohsen on 11/3/2018
 * The file explorer cache
 */
object FileCache {

    // cache
    private val fileCache = HashMap<String, ArrayList<ExplorerFile>>()
    private val lastModifiedCache = HashMap<String, Long>()

    // cache API
    fun getExplorerFilesByDirectory(f: File): ArrayList<ExplorerFile> {
        val path = f.absolutePath
        var files = fileCache[path]

        // if not cached, or directory was modified more recently than the cache
        if (files == null || f.lastModified() > lastModifiedCache[path] ?: 0L) {
            files = FileMetadata.listFileModels(path)
            fileCache[path] = files

            lastModifiedCache[path] = f.lastModified()
        }

        return files
    }

    fun getMediaPathsByPath(path: String): List<String> {
        val parentDir = File(path).parentFile
	    val parentDirPath = parentDir.absolutePath

        var files = fileCache[parentDir.absolutePath]

        // if not cached, or directory was modified more recently than the cache
        if (files == null || parentDir.lastModified() > lastModifiedCache[parentDirPath] ?: 0L) {
            files = FileMetadata.listFileModels(parentDirPath)
            fileCache[parentDirPath] = files

            lastModifiedCache[parentDirPath] = parentDir.lastModified()
        }

        return files.filter { file -> !file.isDirectory }.map { file -> file.absolutePath }
    }
}

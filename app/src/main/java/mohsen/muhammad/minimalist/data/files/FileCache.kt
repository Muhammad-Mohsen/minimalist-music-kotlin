package mohsen.muhammad.minimalist.data.files

import mohsen.muhammad.minimalist.core.FileHelper
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
        return getExplorerFilesByPath(f.absolutePath) // such design, much abstraction
    }

    private fun getExplorerFilesByPath(path: String): ArrayList<ExplorerFile> {
        var files = fileCache[path]

        val f = File(path)

        // if not cached, or directory was modified more recently than the cache
        if (files == null || f.lastModified() > lastModifiedCache[path] ?: 0L) {
            files = FileHelper.listFileModels(path)
            fileCache[path] = files

            lastModifiedCache[path] = f.lastModified()
        }

        return files
    }

}
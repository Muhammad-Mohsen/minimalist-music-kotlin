package com.minimalist.music.data.files

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
	fun listFiles(dir: File, sortBy: String = SortBy.AZ): ArrayList<ExplorerFile> {
		val path = dir.absolutePath
		var files = fileCache["$sortBy/$path"]

		// if not cached, or directory was modified more recently than the cache
		if (files == null || dir.lastModified() > (lastModifiedCache["$sortBy/$path"] ?: 0L)) {
			files = ExplorerFile.listFiles(path, sortBy)
			fileCache["$sortBy/$path"] = files

			lastModifiedCache["$sortBy/$path"] = dir.lastModified()
		}

		return files
	}

	fun listTracks(path: String, sortBy: String = SortBy.AZ): List<String> {
		val parentDir = File(path).parentFile ?: return emptyList()
		val parentDirPath = parentDir.absolutePath

		var files = fileCache["$sortBy/$parentDirPath"]

		// if not cached, or directory was modified more recently than the cache
		if (files == null || parentDir.lastModified() > (lastModifiedCache["$sortBy/$parentDirPath"] ?: 0L)) {
			files = ExplorerFile.listFiles(parentDirPath, sortBy)
			fileCache["$sortBy/$parentDirPath"] = files

			lastModifiedCache["$sortBy/$parentDirPath"] = parentDir.lastModified()
		}

		return files.filter { file -> !file.isDirectory }.map { file -> file.absolutePath }
	}
}

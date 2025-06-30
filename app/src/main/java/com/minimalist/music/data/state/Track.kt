package com.minimalist.music.data.state

import android.content.SharedPreferences
import android.util.Log
import com.minimalist.music.data.files.Chapter
import com.minimalist.music.data.files.ExplorerFile
import com.minimalist.music.data.files.FileMetadata
import com.minimalist.music.data.files.SerializableBitmap
import com.minimalist.music.data.files.serializeChapters
import com.minimalist.music.data.state.State.Key
import com.minimalist.music.data.state.State.track
import com.minimalist.music.foundation.ext.EMPTY
import com.minimalist.music.foundation.ext.formatMillis
import com.minimalist.music.foundation.ext.put
import java.io.File

class Track(private val preferences: SharedPreferences) {
	var path: String
		get() = preferences.getString(Key.PATH, String.EMPTY) ?: String.EMPTY
		set(value) = preferences.put(Key.PATH, value)

	val exists
		get() = File(path).exists()

	var name = String.EMPTY
	var album = String.EMPTY
	var artist = String.EMPTY
	var duration = 0L
	var albumArt: SerializableBitmap? = null

	var seek: Int
		get() = preferences.getInt(Key.SEEK, 0)
		set(value) = preferences.put(Key.SEEK, value)

	var chapters: ArrayList<Chapter> = ArrayList()
	val hasChapters: Boolean
		get() = chapters.size > 1

	var lyrics = String.EMPTY

	val readableSeek: String
		get() = formatMillis(seek.toLong())

	val readableDuration: String
		get() = formatMillis(duration)

	fun update(filePath: String = String.EMPTY) {
		path = filePath.ifBlank { path }

		val f = File(path)
		if (!ExplorerFile.isTrack(f)) return

		// Play Store keeps saying that the ffmpeg.setDataSource throws an illegalArgumentException...to me the above check should fix that, but it didn't
		// so I just try/catch the fucker
		try {
			val metadata = FileMetadata(f)
			name = metadata.title
			album = metadata.album
			artist = metadata.artist
			duration = metadata.duration
			chapters = metadata.chapters
			albumArt = metadata.albumArtBitmap

		} catch (ex: Exception) { Log.e("State", "State.Track.update", ex) }
	}

	fun serialize(): Map<String, Any> {
		return mapOf(
			"path" to path,
			"album" to album,
			"name" to name,
			"artist" to artist,
			"seek" to seek,
			"duration" to duration,
			"albumArt" to (albumArt?.encoded ?: ""),
			"chapters" to chapters.serializeChapters(),
			"lyrics" to lyrics
		)
	}
}
package com.minimalist.music.data.state

import android.content.SharedPreferences
import android.util.Log
import com.minimalist.music.data.files.Chapter
import com.minimalist.music.data.files.SerializableBitmap
import com.minimalist.music.data.files.Verse
import com.minimalist.music.data.files.isTrack
import com.minimalist.music.data.files.serializeChapters
import com.minimalist.music.data.files.serializeLyrics
import com.minimalist.music.data.state.State.Key
import com.minimalist.music.foundation.ext.EMPTY
import com.minimalist.music.data.files.extractChapters
import com.minimalist.music.data.files.extractSyncedLyrics
import com.minimalist.music.data.files.extractUnsyncedLyrics
import com.minimalist.music.foundation.ext.put
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.File

class Track(private val preferences: SharedPreferences) {
	private var _path: String? = null
	var path: String
		get() {
			if (_path == null) _path = preferences.getString(Key.PATH, String.EMPTY) ?: String.EMPTY
			return _path!!
		}
		set(value) {
			_path = value
			preferences.put(Key.PATH, value)
		}

	val exists
		get() = File(path).exists()

	var name = String.EMPTY
	var album = String.EMPTY
	var artist = String.EMPTY
	var duration = 0L
	var albumArt: SerializableBitmap? = null

	private var _seek: Int? = null
	var seek: Int
		get() {
			if (_seek == null) _seek = preferences.getInt(Key.SEEK, 0)
			return _seek!!
		}
		set(value) {
			_seek = value
			preferences.put(Key.SEEK, value)
		}

	var chapters: ArrayList<Chapter> = ArrayList()
	val hasChapters: Boolean
		get() = chapters.size > 1

	var unsyncedLyrics = String.EMPTY
	var syncedLyrics: ArrayList<Verse> = ArrayList()

	fun update(filePath: String = String.EMPTY) {
		path = filePath.ifBlank { path }

		val f = File(path)
		if (!f.isTrack()) return

		// from the field: ffmpeg.setDataSource throws an illegalArgumentException...to me the above check should fix that, but it didn't
		// so I just try/catch the fucker
		try {
			val retriever = FFmpegMediaMetadataRetriever().apply { setDataSource(path) }

			name = f.nameWithoutExtension
			album = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM) ?: f.parentFile?.name ?: String.EMPTY
			artist = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST) ?: String.EMPTY
			duration = retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
			unsyncedLyrics = retriever.extractUnsyncedLyrics()
			syncedLyrics = retriever.extractSyncedLyrics(f)
			chapters = retriever.extractChapters()
			albumArt = SerializableBitmap(retriever.embeddedPicture ?: ByteArray(0))

			retriever.release()

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
			"unsyncedLyrics" to unsyncedLyrics,
			"syncedLyrics" to syncedLyrics.serializeLyrics()
		)
	}
}

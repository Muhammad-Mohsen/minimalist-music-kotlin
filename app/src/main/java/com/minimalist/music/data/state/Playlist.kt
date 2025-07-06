package com.minimalist.music.data.state

import android.content.SharedPreferences
import com.minimalist.music.data.files.FileCache
import com.minimalist.music.foundation.ext.put
import java.util.Collections
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Holds playlist items, information about the playlist (shuffle, repeat modes, current index...
 */

class Playlist(private val preferences: SharedPreferences) {

	val tracks: ArrayList<String> = preferences.getString(State.Key.PLAYLIST, null)?.split(";")
		?.let { ArrayList(it) }
		?: ArrayList()

	var index: Int = 0 // current index

	fun update(trackPath: String) {
		val tracks = FileCache.getMediaPathsByPath(trackPath)
		val start = tracks.indexOf(trackPath)
		Collections.rotate(tracks, -start) // start where the user clicked

		update(tracks)
	}
	fun update(trackList: List<String>, append: Boolean = false) {
		if (!append) tracks.clear()
		tracks.addAll(trackList)

		preferences.put(State.Key.PLAYLIST, trackList.joinToString(";"))
	}

	// sets the index of the current track (may return -1 if the track was deleted for example)
	fun updateIndex(currentTrackPath: String) {
		index = tracks.indexOf(currentTrackPath)
	}

	fun contains(track: String): Boolean {
		return tracks.contains(track)
	}

	fun getPreviousTrack(): String? {
		if (tracks.isEmpty()) return null

		when {
			State.settings.shuffle -> index = ThreadLocalRandom.current().nextInt(0, tracks.size) // first, check the shuffle state
			index > 0 -> index-- // then, if we're not at the first track of the playlist, decrement by one!
			else -> index = tracks.size - 1 // otherwise, rotate the index to the end of the list
		}

		return tracks.getOrNull(index)
	}

	// the onComplete param indicates whether we're requesting the next track upon completion of playing the current track,
	// or by clicking the "Next" button
	fun getNextTrack(onComplete: Boolean): String? {
		if (tracks.isEmpty()) return null

		when {
			// first, check the shuffle state
			State.settings.shuffle -> index = ThreadLocalRandom.current().nextInt(0, tracks.size) // nextInt is exclusive.

			// then the repeat stuff
			State.settings.repeat == RepeatMode.REPEAT_ONE -> return tracks.getOrNull(index) // if the index was out-of-bounds, return null
			State.settings.repeat == RepeatMode.ACTIVE -> index = (index + 1) % tracks.size
			State.settings.repeat == RepeatMode.INACTIVE -> {
				index = (index + 1) % tracks.size
				if (index == 0 && onComplete) return null // at the end of the playlist
			}
		}

		return tracks.getOrNull(index)
	}

	fun getTrackByIndex(index: Int): String? {
		return tracks.getOrNull(index)
	}

	fun toggleShuffle() {
		State.settings.shuffle = !State.settings.shuffle
	}
	fun cycleRepeatMode() {
		State.settings.repeat = RepeatMode.list[(State.settings.repeat + 1) % RepeatMode.list.size]
	}

	fun isEmpty() = tracks.isEmpty()

	fun serialize(): Map<String, Any> {
		return mapOf(
			"tracks" to tracks,
			"index" to index
		)
	}

	object RepeatMode {
		const val INACTIVE = 0 // inactive
		const val ACTIVE = 1 // active
		const val REPEAT_ONE = 2 // repeat-one

		val list = arrayOf(INACTIVE, ACTIVE, REPEAT_ONE)
	}
}

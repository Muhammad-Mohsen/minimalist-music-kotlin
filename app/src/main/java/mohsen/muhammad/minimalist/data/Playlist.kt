package mohsen.muhammad.minimalist.data

import android.content.SharedPreferences
import mohsen.muhammad.minimalist.core.ext.put
import mohsen.muhammad.minimalist.data.files.FileCache
import java.util.Collections
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Holds playlist items, information about the playlist (shuffle, repeat modes, current index...
 */

class Playlist(private val sharedPreferences: SharedPreferences) {

	private val tracks: ArrayList<String> = ArrayList()
	/* get() {
		val savedPlaylist = sharedPreferences.getString(Key.PLAYLIST, String.EMPTY) ?: String.EMPTY // get a semi colon-separated string
		return ArrayList(savedPlaylist.split(";"))
	}
	set(value) = sharedPreferences.put(Key.PLAYLIST, value.joinToString(";")) */

	private var index: Int = 0 // current index

	// stored attributes
	var repeat: Int
		get() = sharedPreferences.getInt(State.Key.REPEAT, RepeatMode.INACTIVE)
		set(value) = sharedPreferences.put(State.Key.REPEAT, value)

	var shuffle: Boolean
		get() = sharedPreferences.getBoolean(State.Key.SHUFFLE, false)
		set(value) = sharedPreferences.put(State.Key.SHUFFLE, value)

	fun updateItems(trackPath: String) {
		val tracks = FileCache.getMediaPathsByPath(trackPath)
		val start = tracks.indexOf(trackPath)
		Collections.rotate(tracks, -start) // start where the user clicked

		updateItems(tracks)
	}

	fun updateItems(items: List<String>, append: Boolean = false) {
		if (!append) tracks.clear()
		tracks.addAll(items)
	}

	fun contains(track: String): Boolean {
		return tracks.contains(track)
	}

	fun getPreviousTrack(): String? {
		if (tracks.size == 0) return null

		when {
			shuffle -> index = ThreadLocalRandom.current().nextInt(0, tracks.size) // first, check the shuffle state
			index > 0 -> index-- // then, if we're not at the first track of the playlist, decrement by one!
			else -> index = tracks.size - 1 // otherwise, rotate the index to the end of the list
		}

		return tracks.getOrNull(index)
	}

	// the onComplete param indicates whether we're requesting the next track upon completion of playing the current track,
	// or by clicking the "Next" button
	fun getNextTrack(onComplete: Boolean): String? {
		if (tracks.size == 0) return null

		when {
			// first, check the shuffle state
			shuffle -> index = ThreadLocalRandom.current().nextInt(0, tracks.size) // nextInt is exclusive.

			// then the repeat stuff
			repeat == RepeatMode.REPEAT_ONE -> return tracks.getOrNull(index) // if the index was out-of-bounds, return null
			repeat == RepeatMode.ACTIVE -> index = (index + 1) % tracks.size
			repeat == RepeatMode.INACTIVE -> {
				index = (index + 1) % tracks.size
				if (index == 0 && onComplete) return null // at the end of the playlist
			}
		}

		return tracks.getOrNull(index)
	}

	fun getTrackByIndex(index: Int): String? {
		return tracks.getOrNull(index)
	}

	// sets the index of the current track (may return -1 if the track was deleted for example)
	fun setTrack(currentTrackPath: String) {
		index = tracks.indexOf(currentTrackPath)
	}

	fun toggleShuffle() {
		shuffle = !shuffle
	}
	fun cycleRepeatMode() {
		repeat = RepeatMode.list[(repeat + 1) % RepeatMode.list.size]
	}

	object RepeatMode {
		const val INACTIVE = 0 // inactive
		const val ACTIVE = 1 // active
		const val REPEAT_ONE = 2 // repeat-one

		val list = arrayOf(INACTIVE, ACTIVE, REPEAT_ONE)
	}
}

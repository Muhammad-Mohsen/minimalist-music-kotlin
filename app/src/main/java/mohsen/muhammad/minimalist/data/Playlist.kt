package mohsen.muhammad.minimalist.data

import android.content.SharedPreferences
import mohsen.muhammad.minimalist.core.ext.put
import mohsen.muhammad.minimalist.data.files.FileCache
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Holds playlist items, information about the playlist (shuffle, repeat modes, current index, current directory...
 */

class Playlist(private val sharedPreferences: SharedPreferences) {

	private val tracks: ArrayList<String> = ArrayList()
		/* get() {
			val savedPlaylist = sharedPreferences.getString(Key.PLAYLIST, String.EMPTY) ?: String.EMPTY // get a semi colon-separated string
			return ArrayList(savedPlaylist.split(";"))
		}
		set(value) = sharedPreferences.put(Key.PLAYLIST, value.joinToString(";")) */

	private var index: Int = 0 // current index
	private var start: Int = 0 // starting index - to try and do circular playlist

	// stored attributes
	var repeat: Int
		get() = sharedPreferences.getInt(State.Key.REPEAT, RepeatMode.INACTIVE)
		set(value) = sharedPreferences.put(State.Key.REPEAT, value)

	var shuffle: Boolean
		get() = sharedPreferences.getBoolean(State.Key.SHUFFLE, false)
		set(value) = sharedPreferences.put(State.Key.SHUFFLE, value)

	fun updateItems(trackPath: String) {
		val tracks = FileCache.getMediaPathsByPath(trackPath)
		updateItems(tracks)
	}

	fun updateItems(items: List<String>, addToExisting: Boolean = false) {
		if (!addToExisting) tracks.clear()
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

		return if (index != -1) tracks[index] else null
	}

	// the onComplete param indicates whether we're requesting the next track upon completion of playing the current track,
	// or by clicking the "Next" button
	fun getNextTrack(onComplete: Boolean): String? {
		if (tracks.size == 0) return null

		// first, check the shuffle state
		if (shuffle) index = ThreadLocalRandom.current().nextInt(0, tracks.size) // nextInt is exclusive.

		// if we hit the end, and we're repeating, go back to the start.
		// next up, if we're yet to hit the end of the playlist AND we're not repeating the same track, simply increment the index.
		else if (index < tracks.size - 1 && repeat != RepeatMode.REPEAT_ONE) index++

		else if (repeat == RepeatMode.ACTIVE) index = 0

		// if we did hit the end, and we're not repeating, we look into the onComplete argument:
		// if it is true, meaning that we're looking for the next track after finishing playing the current one, we'll stop.
		// otherwise, it means that the user clicked the Next button, so, we'll return the first track index.
		else if (repeat == RepeatMode.INACTIVE) {
			index = if (onComplete) -1 else 0
		}

		// finally, if the index isn't invalid, we return the track.
		return if (index != -1) tracks[index] else null
	}

	fun getTrackByIndex(index: Int): String? {
		return if (index < tracks.size) tracks[index] else null
	}

	// sets the index of the current track - isStart indicates whether the current track should be treated as the starting index in the current playlist
	fun setTrack(currentTrackPath: String, isStart: Boolean = true) {
		index = tracks.indexOf(currentTrackPath)
		if (isStart) start = index
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

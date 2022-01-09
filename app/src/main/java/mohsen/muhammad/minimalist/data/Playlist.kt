package mohsen.muhammad.minimalist.data

import android.content.Context
import mohsen.muhammad.minimalist.data.files.FileCache
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Holds playlist items, information about the playlist (shuffle, repeat modes, current index, current directory...
 */

class Playlist(context: Context) {

	private val sharedPreferences = context.getSharedPreferences(State.Key.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)

	private val tracks: ArrayList<String> = ArrayList()
		/* get() {
			// get a semi colon-separated string
			val savedPlaylist = sharedPreferences.getString(Key.PLAYLIST, String.EMPTY) ?: String.EMPTY
			return ArrayList(savedPlaylist.split(";"))
		}
		set(value) {
			sharedPreferences.edit()
				.putString(Key.PLAYLIST, value.joinToString(";"))
				.apply()
		} */
	private var index: Int = 0 // current index
	private var start: Int = 0 // starting index - to try and do circular playlist

	// stored attributes
	var repeat: Int
		get() {
			return sharedPreferences.getInt(State.Key.REPEAT, RepeatMode.INACTIVE)
		}
		set(value) {
			sharedPreferences.edit()
				.putInt(State.Key.REPEAT, value)
				.apply()
		}

	var shuffle: Boolean
		get() {
			return sharedPreferences.getBoolean(State.Key.SHUFFLE, false)
		}
		set(value) {
			sharedPreferences.edit()
				.putBoolean(State.Key.SHUFFLE, value)
				.apply()
		}

	fun updateItems(trackPath: String) {
		val tracks = FileCache.getMediaPathsByPath(trackPath)
		updateItems(tracks)
	}

	fun updateItems(items: List<String>) {
		tracks.clear()
		tracks.addAll(items)

		// State.playlist.tracks.clear()
		// State.playlist.tracks.addAll(items)
	}

	fun contains(track: String): Boolean {
		return tracks.contains(track)
	}

	// first, check the shuffle state
	// then, if we're not at the first track of the playlist, decrement by one!
	// otherwise, rotate the index to the end of the list
	fun getPreviousTrack(): String? {
		when {
			shuffle -> index = ThreadLocalRandom.current().nextInt(0, tracks.size) // nextInt is exclusive.
			index > 0 -> index--
			else -> index = tracks.size - 1
		}

		return if (index != -1) tracks[index] else null
	}

	// the onComplete param indicates whether we're requesting the next track upon completion of playing the current track,
	// or by clicking the "Next" button
	fun getNextTrack(onComplete: Boolean): String? {

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
}

package mohsen.muhammad.minimalist.app.player

import mohsen.muhammad.minimalist.data.RepeatMode
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Holds playlist items, information about the playlist (shuffle, repeat modes, current index, current directory...
 */

class Playlist {

	private val trackList: ArrayList<String>

	private var index: Int = 0
	private var start: Int = 0 // starting index - to try and do circular playlist

	// attributes
	private var isShuffle: Boolean = false
	private var repeatMode: Int = RepeatMode.NONE

	constructor(items: ArrayList<String>) {
		trackList = items
	}

	constructor(trackPath: String) {
		trackList = ArrayList() // TODO get base path + get tracks in the base path
	}

	// first, check the shuffle state
	// then, if we're not at the first track of the playlist, decrement by one!
	// otherwise, rotate the index to the end of the list
	fun getPreviousTrack(): String? {
		when {
			isShuffle -> index = ThreadLocalRandom.current().nextInt(0, trackList.size) // nextInt is exclusive.
			index > 0 -> index--
			else -> index = trackList.size - 1
		}

		return null
	}

	// the onComplete param indicates whether we're requesting the next track upon completion of playing the current track,
	// or by clicking the "Next" button
	fun getNextTrack(onComplete: Boolean): String? {

		// first, check the shuffle state
		if (isShuffle)
			index = ThreadLocalRandom.current().nextInt(0, trackList.size) // nextInt is exclusive.

		else if (index < trackList.size - 1 && repeatMode != RepeatMode.ONE)
			index++

		else if (repeatMode == RepeatMode.REPEAT)
			index = 0

		// if we hit the end, and we're not repeating, we look into the onComplete argument:
		// if it is true, meaning that we're looking for the next track after finishing playing the current one, we'll stop.
		// otherwise, it means that the user clicked the Next button, so, we'll return the first track index.
		// if we did hit the end, and we're repeating, go back to the start.
		// next up, if we're yet to hit the end of the playlist AND we're not repeating the same track, simply increment the index.
		else if (repeatMode == RepeatMode.NONE) {
			index = if (onComplete) -1 else 0
		}

		// finally, if the index isn't invalid, we return the track.
		return if (index != -1) trackList[index] else null

	}

	fun getTrackByIndex(index: Int): String? {
		return if (index < trackList.size) trackList[index] else null
	}

	// sets the index of the current track - isStart indicates whether the current track should be treated as the starting index in the current playlist
	fun setTrack(currentTrackPath: String, isStart: Boolean = true) {
		index = trackList.indexOf(currentTrackPath)

		if (isStart)
			start = index
	}

	fun toggleShuffle() {
		isShuffle = !isShuffle
	}

	fun cycleRepeatMode() {
		when (repeatMode) {
			RepeatMode.NONE -> repeatMode = RepeatMode.REPEAT
			RepeatMode.REPEAT -> repeatMode = RepeatMode.ONE
			RepeatMode.ONE -> repeatMode = RepeatMode.NONE
		}
	}
}

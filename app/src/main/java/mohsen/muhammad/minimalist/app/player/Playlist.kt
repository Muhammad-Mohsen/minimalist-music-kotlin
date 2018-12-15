package mohsen.muhammad.minimalist.app.player

import java.util.concurrent.ThreadLocalRandom

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Holds playlist items, information about the playlist (shuffle, repeat modes, current index, current directory...
 */

class Playlist {

    private var mIndex: Int = 0
    private val mStart: Int // starting index - to try and do circular playlist

    var isShuffle: Boolean = false
        private set
    var repeatMode: RepeatMode? = null
        private set

    var playlistItems: ArrayList<String>? = null

    // first, check the shuffle state
    // nextInt max is exclusive.
    // then, if we're not at the first track of the playlist, decrement by one!
    // otherwise, rotate the index to the end of the list
    val previousItem: String?
        get() {
            if (playlistItems == null)
                return null

            val playlistSize = playlistItems!!.size
            when {
                isShuffle -> mIndex = ThreadLocalRandom.current().nextInt(0, playlistSize)
                mIndex > 0 -> mIndex--
                else -> mIndex = playlistSize - 1
            }

            return null
        }

    init {
        mIndex = 0
        mStart = 0

        isShuffle = false
    }

    fun toggleShuffle() {
        isShuffle = !isShuffle
    }

    fun updateRepeatMode() {
        when (repeatMode) {
            Playlist.RepeatMode.NONE -> repeatMode = RepeatMode.REPEAT

            Playlist.RepeatMode.REPEAT -> repeatMode = RepeatMode.ONE

            Playlist.RepeatMode.ONE -> repeatMode = RepeatMode.NONE
        }
    }

    // the onComplete param indicates whether we're requesting the next track upon completion of playing the current track,
    // or by clicking the "Next" button
    fun getNextItem(onComplete: Boolean): String? {
        // first things first, check that the playlist is initialized
        if (playlistItems == null)
            return null

        val playlistSize = playlistItems!!.size

        // first, check the shuffle state
        if (isShuffle)
            mIndex = ThreadLocalRandom.current().nextInt(0, playlistSize) // nextInt max is exclusive.
        else if (mIndex < playlistSize - 1 && repeatMode != RepeatMode.ONE)
            mIndex++
        else if (repeatMode == RepeatMode.REPEAT)
            mIndex = 0
        else if (repeatMode == RepeatMode.NONE) {
            mIndex = if (onComplete)
                -1
            else
                0
        }// if we hit the end, and we're not repeating, we look into the onComplete argument:
        // if it is true, meaning that we're looking for the next track after finishing playing the current one, we'll stop.
        // otherwise, it means that the user clicked the Next button, so, we'll return the first track index.
        // if we did hit the end, and we're repeating, go back to the start.
        // next up, if we're yet to hit the end of the playlist AND we're not repeating the same track, simply increment the index.

        // finally, if the index isn't invalid, we return the track.
        return if (mIndex != -1) playlistItems!![mIndex] else null

    }

    fun getItem(index: Int): String? {
        return if (index < playlistItems!!.size) playlistItems!![index] else null

    }

    enum class RepeatMode {
        NONE,
        ONE,
        REPEAT
    }
}

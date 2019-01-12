package mohsen.muhammad.minimalist.data

/**
 * Created by muhammad.mohsen on 11/3/2018.
 */

// defines the explorer recycler view adapter view types
// also used to determine the interaction (click, long click) source
object Type {
	const val DIRECTORY = 0
	const val TRACK = 1
	const val CRUMB = 2
}

object SelectionState {
	const val NONE = 0
	const val SELECTED = 1
	const val PLAYLIST = 2
	const val PLAYING = 3
}

object RepeatMode {
	const val NONE = 0
	const val ONE = 1
	const val REPEAT = 2
}

object PlaylistFlag {
	const val CYCLE_SHUFFLE = 2
	const val CYCLE_REPEAT = 4
	const val DIRECTORY = 8
}
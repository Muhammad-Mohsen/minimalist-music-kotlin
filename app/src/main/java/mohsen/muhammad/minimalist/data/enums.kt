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
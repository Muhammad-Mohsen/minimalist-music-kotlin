package mohsen.muhammad.minimalist.data

/**
 * Created by muhammad.mohsen on 11/3/2018.
 */

// defines the explorer recycler view adapter view types
// also used to determine the interaction (click, long click) source
object ItemType {
	const val DIRECTORY = 0
	const val TRACK = 1
	const val CRUMB = 2
}

object SelectionState {
	const val NONE = 0
	const val SELECTED_PLAYING = 1
	const val SELECTED_STOPPED = 11
	const val PLAYLIST = 2
	const val PLAYING = 3
}

object RepeatMode {
	const val NONE = 0
	const val ONE = 1
	const val REPEAT = 2
}

// event types (used in the EventBus's PlaybackEvent)
object PlaybackEventType {
	const val PLAY_ITEM = 0
	const val PLAY_NEXT = 1
	const val PLAY_PREVIOUS = 2

	const val PLAY = 10
	const val PAUSE = 11

	const val UPDATE_SEEK = 12

	const val CYCLE_SHUFFLE = 20
	const val CYCLE_REPEAT = 21

	const val UPDATE_METADATA = 30 // event to update the metadata (album|artist|total duration)

	const val INIT = 40
}

// event source (used in the EventBus's PlaybackEvent)
object PlaybackEventSource {
	const val EXPLORER = 1
	const val CONTROLS = 2
	const val SERVICE = 3
}
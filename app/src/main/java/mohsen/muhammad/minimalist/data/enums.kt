package mohsen.muhammad.minimalist.data

import android.view.ViewConfiguration

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
	const val INACTIVE = 0 // inactive
	const val ACTIVE = 1 // active
	const val REPEAT_ONE = 2 // repeat-one

	val list = arrayOf(INACTIVE, ACTIVE, REPEAT_ONE)
}

// event types (used in the EventBus's SystemEvent)
object EventType {
	const val PLAY_ITEM = 0
	const val PLAY_NEXT = 1
	const val PLAY_PREVIOUS = 2

	const val PLAY = 10
	const val PAUSE = 11

	const val SEEK_UPDATE = 12

	const val CYCLE_SHUFFLE = 20
	const val CYCLE_REPEAT = 21

	const val METADATA_UPDATE = 30 // event to update the metadata (album|artist|total duration)

	const val DIR_CHANGE = 40
}

// event source (used in the EventBus's SystemEvent)
object EventSource {
	const val EXPLORER = 1
	const val CONTROLS = 2
	const val SERVICE = 3
	const val NOTIFICATION = 4
	const val BREADCRUMB = 5
	const val FRAGMENT = 6
}

object FabMenu {
	val DELAY = ViewConfiguration.getTapTimeout().toLong() * 2
	const val DURATION = 200L

	const val ANGLE_BRACKET_ARC =35F
	const val ANGLE_OFFSET = 5F
	const val GESTURE_MIN_DISTANCE = 230

	const val BUTTON_NEXT = 0
	const val BUTTON_REPEAT = 1
	const val BUTTON_SHUFFLE = 2
	const val BUTTON_PREV = 3
}

object NotificationAction {
	const val PLAY_PAUSE = 0
	const val PREV = 1
	const val NEXT = 2

	const val EXTRA = "Action"
}
package mohsen.muhammad.minimalist.data

import android.view.ViewConfiguration

/**
 * Created by muhammad.mohsen on 11/3/2018.
 */

object Const {
	object Alpha {
		const val OPAQUE = 1F
		const val TRANSPARENT = 0F
	}

	object Margin {
		const val FIRST_ITEM = 80F
		const val LAST_ITEM = 20F
	}
}

// defines the explorer recycler view adapter view types
// also used to determine the interaction (click, long click) source
object ItemType {
	const val DIRECTORY = 0
	const val TRACK = 1
	const val CRUMB = 2
}

object SelectionState {
	const val NONE = 0
	const val SELECTED = 1
	const val SELECTED_PENDING = 11
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
	const val PLAY_SELECTED = 3 // play the selected items (from breadcrumb bar)

	const val PLAY = 10
	const val PAUSE = 11
	const val FF = 12
	const val RW = 13

	const val SEEK_UPDATE = 14

	const val CYCLE_SHUFFLE = 20
	const val CYCLE_REPEAT = 21

	const val METADATA_UPDATE = 30 // event to update the metadata (album|artist|total duration)

	const val DIR_CHANGE = 40

	const val SELECT_MODE_ADD = 50 // add a track to the selected list (activate the mode if none were selected before)
	const val SELECT_MODE_SUB = 51 // remove a track from the selected list (deactivate the mode if none are selected now)

	const val SELECT_MODE_INACTIVE = 52 // deactivate select mode (press cancel from the breadcrumb bar)
	const val SELECT_MODE_APPEND = 53 // append selected tracks to the current playlist (press add from the breadcrumb bar)
}

// event source (used in the EventBus's SystemEvent)
object EventSource {
	const val EXPLORER = 1
	const val CONTROLS = 2
	const val SERVICE = 3
	const val NOTIFICATION = 4
	const val BREADCRUMB = 5
	const val FRAGMENT = 6
	const val SESSION = 7
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

object PlaybackNotification {
	const val PREV = 0
	const val PLAY_PAUSE = 1
	const val NEXT = 2

	const val EXTRA = "Action"
}
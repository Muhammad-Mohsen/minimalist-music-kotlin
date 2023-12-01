package mohsen.muhammad.minimalist.data

import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.EMPTY

/**
 * Created by muhammad.mohsen on 2/11/2019.
 * event args for all playback events dispatched from different parts of the app.
 */

class SystemEvent(val source: Int, val type: Int, val extras: String = String.EMPTY) : EventBus.EventData()

object EventSource {
    const val EXPLORER = 1
    const val CONTROLS = 2
    const val SERVICE = 3
    const val NOTIFICATION = 4
    const val BREADCRUMB = 5
    const val FRAGMENT = 6
    const val SESSION = 7
}

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

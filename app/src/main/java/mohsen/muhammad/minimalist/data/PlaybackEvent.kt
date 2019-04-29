package mohsen.muhammad.minimalist.data

import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.EMPTY

/**
 * Created by muhammad.mohsen on 2/11/2019.
 * event args for all playback events dispatched from different parts of the app.
 */

class PlaybackEvent(val source: Int, val type: Int, val extras: String = String.EMPTY) : EventBus.EventData()

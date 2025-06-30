package com.minimalist.music.foundation

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.minimalist.music.foundation.ext.toMap
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * Created by muhammad.mohsen on 2/10/2019.
 * Inspired by the EventBus library, this is designed to send messages to the app components (service/UI/notification)
 * that need to be updated when various events occur
 */
object EventBus {

	private val subscribers = ArrayList<WeakReference<Subscriber>>()
	private lateinit var ipc: WeakReference<WebView>

	fun subscribe(subscriber: Subscriber) {
		val ref = WeakReference(subscriber)
		subscribers.add(ref)
	}
	fun subscribe(subscriber: WebView) {
		ipc = WeakReference(subscriber)
	}

	fun dispatch(event: Event) {
		subscribers.forEach { sub -> sub.get()?.handle(event) }

		val event = JSONObject(mapOf<String, Any>(
			"type" to event.type,
			"target" to event.target,
			"data" to event.data
		)).toString()

		Moirai.MAIN.post {
			ipc.get()?.evaluateJavascript("EventBus.dispatch($event, 'fromNative')", null)
		}
	}

	@JavascriptInterface
	fun dispatch(serializedEvent: String) {
		val jsonEvent = JSONObject(serializedEvent)
		val type = jsonEvent.get("type").toString()
		val target = jsonEvent.get("target").toString()
		val data = if (jsonEvent.has("data")) jsonEvent.get("data") as JSONObject else null

		subscribers.forEach { sub -> sub.get()?.handle(Event(type, target, data?.toMap() ?: emptyMap())) }
	}

	interface Subscriber {
		fun handle(event: Event)
	}

	class Event(val type: String, val target: String, val data: Map<String, Any> = emptyMap())

	object Type {
		const val RESTORE_STATE = "restoreState"
		const val INSETS = "insets"

		const val PLAY_TRACK_REQUEST = "playTrackRequest"
		const val PLAY_TRACK = "playTrack"
		const val PLAY_NEXT = "playNext"
		const val PLAY_PREVIOUS = "playPrev"
		const val PLAY_SELECTED = "playSelected" // play the selected items (from breadcrumb bar)

		const val PLAY = "play"
		const val PAUSE = "pause"
		const val FF = "ff"
		const val RW = "rw"

		const val DIR_CHANGE_REQUEST = "dirChangeRequest"
		const val DIR_CHANGE = "dirChange"
		const val METADATA_UPDATE = "metadataUpdate" // event to update the metadata (album|artist|total duration)

		const val SEEK_UPDATE = "seekUpdate"
		const val SEEK_UPDATE_USER = "seekUpdateUser" // a seek change that was initiated by the user (used to update the session playback state)

		const val SELECT_MODE_ADD = "selectModeAdd" // add a track to the selected list (activate the mode if none were selected before)
		const val SELECT_MODE_SUB = "selectModeSub" // remove a track from the selected list (deactivate the mode if none are selected now)
		const val SELECT_MODE_CANCEL = "selectModeCancel" // deactivate select mode (press cancel from the breadcrumb bar)
		const val SEARCH_QUERY = "searchQuery" // search the explorer with a query

		const val SLEEP_TIMER_TICK = "sleepTimerTick"
		const val SLEEP_TIMER_FINISH = "sleepTimerFinish"

		const val HIDE_SETTINGS = "hideSettings"

		const val PLAYBACK_SPEED = "playbackSpeed"
		const val EQ = "eq"
		const val PRIVACY_POLICY = "privacyPolicy"
		const val CYCLE_SHUFFLE = "toggleShuffle"
		const val CYCLE_REPEAT = "toggleRepeat"

		const val MODE_CHANGE = "modeChange"
		const val MODE_NORMAL = "modeNormal"
		const val PERMISSION_REQUEST = "permissionRequest"
		const val PERMISSION_RESPONSE = "permissionResponse"

		const val APP_FOREGROUNDED = "appForegrounded"
	}

	object Target {
		const val SERVICE = "service"
		const val NOTIFICATION = "notification"
		const val ACTIVITY = "activity"
		const val SESSION = "session"
		const val TIMER = "timer"
	}
}

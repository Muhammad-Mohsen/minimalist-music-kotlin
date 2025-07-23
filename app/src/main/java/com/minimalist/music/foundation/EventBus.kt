package com.minimalist.music.foundation

import android.util.Log
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
	private var ipc: WeakReference<WebView>? = null

	fun subscribe(subscriber: Subscriber) {
		val ref = WeakReference(subscriber)
		subscribers.add(ref)
	}
	fun subscribe(subscriber: WebView) {
		ipc = WeakReference(subscriber)
	}
	fun unsubscribe(subscriber: Subscriber) {
		subscribers.removeIf { it.get() == null || it.get() == subscriber }
	}

	fun dispatch(event: Event) {
		subscribers.forEach { sub -> sub.get()?.handle(event) }

		val event = JSONObject(mapOf(
			"type" to event.type,
			"target" to event.target,
			"data" to event.data
		)).toString()

		Log.d("EventBus", "dispatch: $event")

		Moirai.MAIN.post {
			ipc?.get()?.evaluateJavascript("""
				try { EventBus.dispatch($event, 'fromNative') }
				catch (e) { console.log(e.stack, JSON.stringify($event)); }
			""".trimIndent(), null)
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
		const val PERMISSION_REQUEST = "permissionRequest"
		const val PERMISSION_RESPONSE = "permissionResponse"

		const val INSETS = "insets"
		const val RESTORE_STATE = "restoreState"

		const val MODE_CHANGE = "modeChange"
		const val MODE_NORMAL = "modeNormal"

		const val PLAYLIST_UPDATE = "playlistUpdate"
		const val QUEUE_PLAY_SELECTED = "queuePlaySelected" // play the selected items (from breadcrumb bar)
		const val QUEUE_ADD_SELECTED = "queueAddSelected" // play the selected items (from breadcrumb bar)

		const val DIR_CHANGE = "dirChange"
		const val DIR_UPDATE = "dirUpdate"

		const val METADATA_UPDATE = "metadataUpdate" // event to update the metadata (album|artist|total duration)
		const val PLAY_TRACK = "playTrack"
		const val PLAY_NEXT = "playNext"
		const val PLAY_PREV = "playPrev"
		const val PLAY = "play"
		const val PAUSE = "pause"
		const val FF = "ff"
		const val RW = "rw"
		const val SEEK_TICK = "seekTick"
		const val SEEK_UPDATE = "seekUpdate"

		const val SLEEP_TIMER_TOGGLE = "sleepTimerToggle"
		const val SLEEP_TIMER_CHANGE = "sleepTimerChange"
		const val SLEEP_TIMER_TICK = "sleepTimerTick"
		const val SLEEP_TIMER_FINISH = "sleepTimerFinish"

		const val THEME_CHANGE = "themeChange"
		const val PLAYBACK_SPEED_CHANGE = "playbackSpeedChange"
		const val SEEK_JUMP_CHANGE = "seekJumpChange"
		const val SORT_BY_CHANGE = "sortByChange"
		const val TOGGLE_SHUFFLE = "toggleShuffle"
		const val TOGGLE_REPEAT = "toggleRepeat"

		const val EQUALIZER_INFO = "equalizerInfo"
		const val EQUALIZER_PRESET_CHANGE = "equalizerPresetChange"
		const val EQUALIZER_BAND_CHANGE = "equalizerBandChange"

		const val SECONDARY_CONTROLS_CHANGE = "secondaryControlsChange"

		const val PRIVACY_POLICY = "privacyPolicy"
		const val APP_FOREGROUNDED = "appForegrounded" // used in foreground service
	}

	object Target {
		const val SERVICE = "service"
		const val NOTIFICATION = "notification"
		const val ACTIVITY = "activity"
		const val SESSION = "session"
	}
}

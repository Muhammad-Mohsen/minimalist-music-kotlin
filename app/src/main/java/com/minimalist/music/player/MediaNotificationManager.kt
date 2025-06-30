package com.minimalist.music.player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.minimalist.music.R
import com.minimalist.music.MainActivity
import com.minimalist.music.foundation.Moirai
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Target
import com.minimalist.music.data.state.State

/**
 * Created by muhammad.mohsen on 5/3/2019.
 * Handles the display/control of the notification
 * P.S. the "NotificationManager" name is already taken!
 */

class MediaNotificationManager(private val context: Context, sessionToken: MediaSessionCompat.Token) : EventBus.Subscriber {

	// media notification style
	private val style: androidx.media.app.NotificationCompat.MediaStyle by lazy {
		androidx.media.app.NotificationCompat.MediaStyle()
			.setShowCancelButton(false)
			.setShowActionsInCompactView(Action.PREV, Action.PLAY_PAUSE, Action.NEXT)
			.setMediaSession(sessionToken)
	}

	init {
		EventBus.subscribe(this)
		createChannel()
	}

	// creates the notification, and displays it
	// the MissingPermission suppression is because this notification should be exempt from this requirement according to:
	// https://developer.android.com/develop/ui/views/notifications/notification-permission#exemptions
	@SuppressLint("MissingPermission", "NotificationPermission")
	fun createNotification(): Notification {

		// click the notification, get the main activity!
		val contentIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), INTENT_FLAGS)

		val notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
			setContentIntent(contentIntent)
			setContentTitle(State.track.name)
			setContentText(State.track.album)
			setSmallIcon(R.drawable.ic_notification)
			setLargeIcon(State.track.albumArt?.decoded)
			setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			priority = NotificationCompat.PRIORITY_HIGH // for versions prior to Oreo

			setColorized(true)
			color = ContextCompat.getColor(context, R.color.black)
			setStyle(style)

			addAction(createAction(R.drawable.ic_notification_rw, Action.RW))
			addAction(createAction(R.drawable.ic_notification_prev, Action.PREV))
			addAction(createAction(getPlayPauseIcon(), Action.PLAY_PAUSE))
			addAction(createAction(R.drawable.ic_notification_next, Action.NEXT))
			addAction(createAction(R.drawable.ic_notification_ff, Action.FF))

		}.build()

		NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification) // display the notification
		return notification
	}

	// creates notification actions
	private fun createAction(iconResId: Int, actionIndex: Int): NotificationCompat.Action {
		val intent = Intent(context, NotificationActionHandler::class.java)
		intent.putExtra(EXTRA, actionIndex)

		val pendingIntent = PendingIntent.getBroadcast(context, actionIndex, intent, INTENT_FLAGS)

		return NotificationCompat.Action(iconResId, actionIndex.toString(), pendingIntent)
	}

	// gets the correct play/pause icon based on current state
	private fun getPlayPauseIcon(): Int {
		return if (State.isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
	}

	// creates a notification channel to play nice with Oreo and above
	private fun createChannel() {
		val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.notificationChannelName), NotificationManager.IMPORTANCE_LOW)
		channel.description = context.getString(R.string.notificationChannelDescription)
		channel.setShowBadge(false)

		val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.createNotificationChannel(channel)
	}

	override fun handle(event: Event) {
		Moirai.MAIN.post { // post-ing here just to delay the notification creation long enough for the playback state to settle (without it, State.isPlaying reported the previous state!)
			when (event.type) {
				EventBus.Type.PLAY,
				EventBus.Type.PAUSE,
				EventBus.Type.PLAY_TRACK,
				EventBus.Type.PLAY_NEXT,
				EventBus.Type.PLAY_PREVIOUS,
				EventBus.Type.METADATA_UPDATE -> {
					Moirai.BG.post {
						createNotification() // simply recreating the notification updates it
					}
				}
			}
		}
	}

	// basically the notification action click handler (registered statically in the manifest)
	class NotificationActionHandler : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {

			val event = when (intent?.getIntExtra(EXTRA, Action.PLAY_PAUSE) ?: Action.PLAY_PAUSE) {
				Action.NEXT -> EventBus.Type.PLAY_NEXT
				Action.PLAY_PAUSE -> if (State.isPlaying) EventBus.Type.PAUSE else EventBus.Type.PLAY
				else -> EventBus.Type.PLAY_PREVIOUS
			}

			EventBus.dispatch(Event(event, Target.NOTIFICATION))
		}
	}

	companion object {
		private const val CHANNEL_ID = "PLAYBACK_NOTIFICATION" // Android Oreo notification channel name
		const val NOTIFICATION_ID = 124816
		const val EXTRA = "Action"
		const val INTENT_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT

		object Action {
			const val PREV = 0
			const val PLAY_PAUSE = 1
			const val NEXT = 2
			const val RW = 3
			const val FF = 4
		}
	}
}

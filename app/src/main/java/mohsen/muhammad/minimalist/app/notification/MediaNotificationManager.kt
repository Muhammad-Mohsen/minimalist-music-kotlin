package mohsen.muhammad.minimalist.app.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.NotificationAction
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent

/**
 * Created by muhammad.mohsen on 5/3/2019.
 * Handles the display/control of the notification
 * P.S. the "NotificationManager" name is already taken!
 */

@SuppressLint("StaticFieldLeak")
object MediaNotificationManager :
	EventBus.Subscriber,
	BroadcastReceiver() // receive notification clicks
{

	private const val CHANNEL_ID = "PLAYBACK_NOTIFICATION"
	private const val ACTION_FILTER = "mohsen.muhammad.minimalist.NOTIFICATION_ACTION"
	private const val NOTIFICATION_ID = 124816

	private lateinit var context: Context // still holding the same application context from State.kt :D

	private val style: androidx.media.app.NotificationCompat.MediaStyle by lazy {
		androidx.media.app.NotificationCompat.MediaStyle()
			.setShowCancelButton(false)
			.setShowActionsInCompactView(NotificationAction.PLAY_PAUSE)
	}

	fun initialize(applicationContext: Context) {
		context = applicationContext
		context.registerReceiver(this, IntentFilter())
		EventBus.subscribe(this)

		createChannel()
		createNotification()
	}

	private fun createNotification() {

		val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
			setContentTitle(State.Track.title)
			setContentText(State.Track.artist)
			setSmallIcon(R.drawable.ic_notification)
			setShowWhen(false)
			priority = NotificationCompat.PRIORITY_DEFAULT // for versions prior to Oreo

			setColorized(true)
			color = ContextCompat.getColor(context, R.color.colorOnBackgroundDark)

			setStyle(style)
			addAction(createAction(getPlayPauseIcon(), NotificationAction.PLAY_PAUSE))
		}

		with(NotificationManagerCompat.from(context)) {
			notify(NOTIFICATION_ID, builder.build())
		}
	}

	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent) {
			when (data.type) {
				EventType.PLAY,
				EventType.PAUSE,
				EventType.PLAY_ITEM,
				EventType.PLAY_NEXT,
				EventType.PLAY_PREVIOUS,
				EventType.METADATA_UPDATE -> createNotification() // simply recreating the notification updates it
			}
		}
	}

	// broadcast receiver handler
	override fun onReceive(context: Context?, intent: Intent?) {
		val notificationAction = intent?.getIntExtra(NotificationAction.EXTRA, NotificationAction.PLAY_PAUSE) ?: NotificationAction.PLAY_PAUSE
	}

	// creates a notification channel to play nice with Oreo and above
	private fun createChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = context.getString(R.string.notificationChannelName)
			val descriptionText = context.getString(R.string.notificationChannelDescription)

			val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
			channel.description = descriptionText
			channel.setShowBadge(false)

			val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}

	private fun createAction(iconResId: Int, actionIndex: Int): NotificationCompat.Action {
		val intent = Intent(context, MediaNotificationManager.javaClass).apply {
			action = ACTION_FILTER
			putExtra(NotificationAction.EXTRA, actionIndex)
		}

		val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

		return NotificationCompat.Action(iconResId, actionIndex.toString(), pendingIntent)
	}

	private fun getPlayPauseIcon(): Int {
		return if (State.isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play
	}
}

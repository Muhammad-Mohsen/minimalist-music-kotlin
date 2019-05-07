package mohsen.muhammad.minimalist.app.main

import android.app.Application
import mohsen.muhammad.minimalist.app.notification.MediaNotificationManager
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.data.State

/**
 * Created by muhammad.mohsen on 1/4/2019.
 * Used to start the player service on app start
 */
class MinimalistApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		PlaybackManager.start(this)
		State.initialize(applicationContext)
		MediaNotificationManager.initialize(applicationContext)
	}

}

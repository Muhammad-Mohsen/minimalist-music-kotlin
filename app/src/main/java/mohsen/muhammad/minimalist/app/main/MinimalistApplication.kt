package mohsen.muhammad.minimalist.app.main

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.data.State

/**
 * Created by muhammad.mohsen on 1/4/2019.
 * Used to start the player service on app start
 */
class MinimalistApplication : Application() {

	override fun onCreate() {
		super.onCreate()

		val playerIntent = Intent(applicationContext, PlaybackManager::class.java)
		ContextCompat.startForegroundService(this, playerIntent)

		State.initialize(applicationContext)
	}

}

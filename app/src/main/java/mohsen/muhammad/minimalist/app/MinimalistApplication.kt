package mohsen.muhammad.minimalist.app

import android.app.Application
import mohsen.muhammad.minimalist.app.player.PlayerService

/**
 * Created by muhammad.mohsen on 1/4/2019.
 * Used to start the player service on app start
 */
class MinimalistApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		PlayerService.start()
	}
}
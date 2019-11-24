package mohsen.muhammad.minimalist.app.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.data.State

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {

		setTheme(R.style.AppTheme) // replace the splash screen

		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		val playerIntent = Intent(applicationContext, PlaybackManager::class.java)
		ContextCompat.startForegroundService(this, playerIntent)

		State.initialize(applicationContext)

		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
					.replace(R.id.container, MainFragment.newInstance())
					.commitNow()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		PlaybackManager.stopSelf()
	}

	override fun onBackPressed() {
		// call onBackPressed on the MainFragment...if it fails, call the super
		if ((supportFragmentManager.fragments.last() as? MainFragment)?.onBackPressed() == false)
			super.onBackPressed()
	}

}

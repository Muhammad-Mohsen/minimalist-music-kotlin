package mohsen.muhammad.minimalist.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.app.player.PlaybackManager

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {

		setTheme(R.style.AppTheme) // replace the splash screen

		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

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

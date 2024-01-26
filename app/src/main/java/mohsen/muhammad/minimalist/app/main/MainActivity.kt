package mohsen.muhammad.minimalist.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.data.State

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {

		// night mode + theme
		State.initialize(applicationContext) // initialize the state while we're at it
		AppCompatDelegate.setDefaultNightMode(State.nightMode)
		setTheme(R.style.AppTheme) // replace the splash screen

		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
					.replace(R.id.container, MainFragment.newInstance())
					.commitNow()
		}
	}

	// Used for uiMode changes, because otherwise, the transition animations are ignored
	override fun recreate() {
		finish()
		application.startActivity(this.intent)
	}
}

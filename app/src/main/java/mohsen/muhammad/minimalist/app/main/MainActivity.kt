package mohsen.muhammad.minimalist.app.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.data.Const
import mohsen.muhammad.minimalist.data.State

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {

		// night mode + theme
		val preferences = getSharedPreferences(Const.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)
		AppCompatDelegate.setDefaultNightMode(preferences.getInt(State.Key.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
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

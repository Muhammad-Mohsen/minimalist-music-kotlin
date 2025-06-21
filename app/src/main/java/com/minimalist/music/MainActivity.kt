package com.minimalist.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.Insets
import com.minimalist.music.player.PlaybackManager
import com.minimalist.music.data.Const
import com.minimalist.music.data.state.State
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Target
import com.minimalist.music.foundation.EventBus.Type
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity(), EventBus.Subscriber {

	private lateinit var permissionRequest: ActivityResultLauncher<String>
	private lateinit var webView: WebView

	private var windowInsets: Insets? = null
	private var isWebViewReady = false

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.AppTheme) // change from splash screen (android 11-)

		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false) // edge-to-edge (android 14-)
		setContentView(R.layout.main_activity)

		webView = findViewById<WebView>(R.id.webview)

		// storage permission...must be in onCreate
		permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
			onPermissionResult(isGranted)
		}

		// back button
		onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				onBackPress()
			}
		})

		// insets (edge to edge)
		ViewCompat.setOnApplyWindowInsetsListener(webView) { v, insets ->
			windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			sendInsetsToWebView()

			insets
		}

		EventBus.subscribe(this)
		initWebView()
		initNative()
	}

	private fun initNative() {
		when {
			checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {

				State.initialize(applicationContext)

				// service
				val playerIntent = Intent(this, PlaybackManager::class.java)
				startForegroundService(playerIntent)

				// restore the state, now that everything is initialized (except the service...which is why restoreState is manually called over there)
				if (State.track.exists) EventBus.dispatch(Event(Type.METADATA_UPDATE, Target.ACTIVITY))
			}
			else -> {
				EventBus.dispatch(Event(Type.UI_MODE_CHANGE, Target.ACTIVITY, mapOf("mode" to "permission")))
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Suppress("DEPRECATION")
	private fun initWebView() {
		WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

		webView.apply {
			webViewClient = object : WebViewClient() {
				override fun onPageFinished(view: WebView?, url: String?) {
					super.onPageFinished(view, url)
					isWebViewReady = true
					sendInsetsToWebView()
				}
			}

			settings.apply {
				javaScriptEnabled = true
				domStorageEnabled = true
				allowFileAccess = true
				allowFileAccessFromFileURLs = true
				allowUniversalAccessFromFileURLs = true
			}

			addJavascriptInterface(EventBus, "IPC")

			val mode = if (checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED) "normal" else "permission"
			loadUrl("file:///android_asset/index.html?mode=$mode") // here we go!

			EventBus.subscribe(this)
		}
	}

	// TODO handle webview dialogs
	private fun onBackPress() {
		when {
//			State.isSettingsSheetVisible -> EventBus.dispatch(Event(Type.HIDE_SETTINGS, Target.ACTIVITY))
//			State.isSelectModeActive or State.isSearchModeActive -> {
//				State.selectedTracks.clear() // update the state
//				State.isSearchModeActive = false
//				EventBus.dispatch(Event(Type.SELECT_MODE_CANCEL, Target.ACTIVITY))
//			}
//			ExplorerFile.isAtRoot(State.currentDirectory.absolutePath) -> {
//				this.moveTaskToBack(true)
//			}
			else -> {
				State.currentDirectory = State.currentDirectory.parentFile!! // don't worry about it
				EventBus.dispatch(Event(Type.DIR_CHANGE, Target.ACTIVITY, mapOf("currentDirectory" to State.currentDirectory.absolutePath)))
			}
		}
	}

	@SuppressLint("QueryPermissionsNeeded")
	private fun eq() {
		val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
			putExtra(AudioEffect.EXTRA_AUDIO_SESSION, State.audioSessionId)
			putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
		}

		if (intent.resolveActivity(packageManager) != null) ActivityCompat.startActivityForResult(this, intent, 0, null)
		else Toast.makeText(this,resources.getString(R.string.noEqualizer),Toast.LENGTH_SHORT).show()
	}
	@SuppressLint("QueryPermissionsNeeded")
	private fun privacyPolicy() {
		val browserIntent = Intent(Intent.ACTION_VIEW, Const.PRIVACY_POLICY_URL.toUri())
		if (intent.resolveActivity(packageManager) != null) startActivity(browserIntent, null)
		else Toast.makeText(this,resources.getString(R.string.noBrowser),Toast.LENGTH_SHORT).show()
	}

	private fun requestPermission() {
		if (shouldShowRequestPermissionRationale(DISK_PERMISSION)) {
			val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
				data = Uri.fromParts("package", packageName, null)
			}
			startActivity(intent)

		} else permissionRequest.launch(DISK_PERMISSION)
	}
	private fun onPermissionResult(isGranted: Boolean) {
		if (isGranted) {
			initNative()
			EventBus.dispatch(Event(Type.UI_MODE_CHANGE, Target.ACTIVITY, mapOf("mode" to "normal")))

		} else {
			EventBus.dispatch(Event(Type.UI_MODE_CHANGE, Target.ACTIVITY, mapOf("mode" to "permission")))
		}
	}

	private fun sendInsetsToWebView() {
		if (windowInsets != null && isWebViewReady) {
			val top = windowInsets!!.top
			val bottom = windowInsets!!.bottom

			EventBus.dispatch(Event(Type.INSETS, Target.ACTIVITY, mapOf("top" to top, "bottom" to bottom)))
		}
	}

	override fun handle(event: Event) {
		if (event.target == Target.ACTIVITY) return

		when (event.type) {
			Type.PERMISSION_REQUEST -> requestPermission()
			Type.EQ -> eq()
			Type.PRIVACY_POLICY -> privacyPolicy()
		}
	}

	companion object {
		val DISK_PERMISSION = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
		else Manifest.permission.READ_EXTERNAL_STORAGE
	}
}

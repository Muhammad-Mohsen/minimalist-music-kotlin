package com.minimalist.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.graphics.Insets
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.minimalist.music.data.Const
import com.minimalist.music.data.files.ExplorerFile
import com.minimalist.music.data.files.serializeFiles
import com.minimalist.music.data.state.State
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Target
import com.minimalist.music.foundation.EventBus.Type
import com.minimalist.music.player.PlaybackManager
import java.io.File

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
	override fun onResume() {
		super.onResume()
		EventBus.dispatch(Event(Type.PERMISSION_RESPONSE, Target.ACTIVITY, mapOf("mode" to
				if (checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED) "normal" else "permission"
		)))
	}

	private fun initNative() {
		when {
			checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
				State.initialize(applicationContext)

				// service
				val playerIntent = Intent(this, PlaybackManager::class.java)
				startForegroundService(playerIntent)

				// restore the state (the service manually calls its own updateState function because it isn't ready at this point)
				if (State.track.exists) EventBus.dispatch(Event(Type.METADATA_UPDATE, Target.ACTIVITY))
			}
			else -> {
				EventBus.dispatch(Event(Type.MODE_CHANGE, Target.ACTIVITY, mapOf("mode" to "permission")))
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Suppress("DEPRECATION")
	private fun initWebView() {
		State.initialize(applicationContext)

		WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

		webView.apply {
			webViewClient = object : WebViewClient() {
				override fun onPageFinished(view: WebView?, url: String?) {
					super.onPageFinished(view, url)
					isWebViewReady = true
					sendInsetsToWebView()
					EventBus.dispatch(Event(Type.RESTORE_STATE, Target.ACTIVITY, State.serialize()))
				}
			}

			settings.apply {
				javaScriptEnabled = true
				allowFileAccessFromFileURLs = true
			}

			addJavascriptInterface(EventBus, "IPC")

			val mode = if (checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED) "normal" else "permission"
			loadUrl("file:///android_asset/index.html?mode=$mode") // here we go!

			EventBus.subscribe(this) // this is the webview (IPC) subscription
		}
	}

	private fun onBackPress() {
		when {
			State.mode != "normal" -> {
				EventBus.dispatch(Event(Type.MODE_NORMAL, Target.ACTIVITY)) // simulates "cancel" click from the header
			}
			ExplorerFile.isAtRoot(State.currentDirectory.absolutePath) -> {
				this.moveTaskToBack(true)
			}
			else -> {
				State.currentDirectory = State.currentDirectory.parentFile!! // don't worry about it
				EventBus.dispatch(Event(Type.DIR_UPDATE, Target.ACTIVITY, mapOf(
					"currentDir" to State.currentDirectory.absolutePath,
					"files" to State.files.serializeFiles()
				)))
			}
		}
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
			EventBus.dispatch(Event(Type.MODE_CHANGE, Target.ACTIVITY, mapOf("mode" to "normal")))

		} else {
			EventBus.dispatch(Event(Type.MODE_CHANGE, Target.ACTIVITY, mapOf("mode" to "permission")))
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
			Type.PRIVACY_POLICY -> privacyPolicy()

			Type.MODE_CHANGE -> State.mode = event.data["mode"].toString() // only used for system back navigation logic

			Type.DIR_CHANGE -> {
				State.currentDirectory = File(event.data["dir"].toString())
				EventBus.dispatch(Event(Type.DIR_UPDATE, Target.ACTIVITY, mapOf("files" to State.files.serializeFiles())))
			}

			Type.THEME_CHANGE -> State.settings.theme = event.data["value"].toString()
			Type.SLEEP_TIMER_CHANGE -> State.settings.sleepTimer = event.data["value"].toString().toInt()
			Type.SEEK_JUMP_CHANGE -> State.settings.seekJump = event.data["value"].toString().toInt()
			Type.PLAYBACK_SPEED_CHANGE -> State.settings.playbackSpeed = event.data["value"].toString().toFloat()
			Type.SORT_BY_CHANGE -> {
				State.settings.sortBy = event.data["value"].toString()
				EventBus.dispatch(Event(Type.DIR_UPDATE, Target.ACTIVITY, mapOf("files" to State.files.serializeFiles())))
			}
		}
	}

	companion object {
		val DISK_PERMISSION = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
		else Manifest.permission.READ_EXTERNAL_STORAGE
	}
}

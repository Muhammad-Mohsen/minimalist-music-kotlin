package com.minimalist.music

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.minimalist.music.data.Const
import com.minimalist.music.data.files.Theme
import com.minimalist.music.data.files.isRoot
import com.minimalist.music.data.files.isTrack
import com.minimalist.music.data.files.serializeFiles
import com.minimalist.music.data.state.State
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Target
import com.minimalist.music.foundation.EventBus.Type
import com.minimalist.music.foundation.Moirai
import com.minimalist.music.foundation.ext.captureScreenshot
import com.minimalist.music.player.PlaybackManager
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity(), EventBus.Subscriber {

	private lateinit var mask: ImageView
	private var webView: WebView? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.AppTheme) // change from splash screen (android 11-)

		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false) // edge-to-edge (android 14-)
		setContentView(R.layout.main_activity)

		webView = findViewById(R.id.webview)
		mask = findViewById(R.id.mask)

		// back button
		onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				onBackPress()
			}
		})

		// insets (edge to edge)
		ViewCompat.setOnApplyWindowInsetsListener(webView!!) { _, insets ->
			State.windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			sendInsetsToWebView()

			insets
		}

		EventBus.subscribe(this)
		initWebView()
		initNative()

		handleOpenWithIntent(intent)
	}
	override fun onResume() {
		super.onResume()
		EventBus.dispatch(Event(Type.PERMISSION_RESPONSE, Target.ACTIVITY, mapOf("mode" to
				if (checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED) "normal" else "permission")))

		initNative()

		Moirai.MAIN.postDelayed({
			mask.visibility = View.GONE
		}, 200)
	}

	override fun onPause() {
		super.onPause()
		webView?.captureScreenshot(this.window) {
			mask.setImageBitmap(it)
			mask.visibility = View.VISIBLE
		}
	}

	// release the webview
	override fun onDestroy() {
		super.onDestroy()

		webView?.apply {
			val parent = this.parent
			(parent as FrameLayout).removeView(this)
			this.clearCache(true)
			this.destroy()
		}
		webView = null
	}

	private fun initNative() {
		when {
			State.playbackManagerReady -> return

			checkSelfPermission(DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
				State.initialize(applicationContext)

				// service
				val playerIntent = Intent(this, PlaybackManager::class.java)
				startForegroundService(playerIntent)
				State.playbackManagerReady = true
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

		webView?.apply {
			webViewClient = object : WebViewClient() {
				override fun onPageFinished(view: WebView?, url: String?) {
					super.onPageFinished(view, url)
					State.webviewReady = true
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
			val lang = Locale.getDefault().language // en, pt, etc
			loadUrl("file:///android_asset/index.html?mode=$mode&lang=$lang") // here we go!

			EventBus.subscribe(this) // this is the webview (IPC) subscription
		}

		handleThemeChange()
	}

	private fun onBackPress() {
		when {
			State.mode != "normal" -> {
				EventBus.dispatch(Event(Type.MODE_NORMAL, Target.ACTIVITY)) // simulates "cancel" click from the header
			}
			State.currentDirectory.isRoot() -> {
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

	private fun sendInsetsToWebView() {
		if (State.windowInsets != null && State.webviewReady) {
			EventBus.dispatch(Event(Type.INSETS, Target.ACTIVITY, mapOf(
				"top" to State.windowInsets!!.top,
				"bottom" to State.windowInsets!!.bottom)))
		}
	}

	private fun handleThemeChange() {
		val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
		Moirai.MAIN.post {
			windowInsetsController.isAppearanceLightStatusBars = State.settings.theme == Theme.LIGHT
		}
	}

	// PERMISSIONS
	private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
		onPermissionResult(isGranted)
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

	// CUSTOM FONT
	private fun handleCustomFont() {
		val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
			addCategory(Intent.CATEGORY_OPENABLE)
			type = "font/*"
		}

		customFontRequest.launch(intent)
	}
	private val customFontRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode != RESULT_OK) return@registerForActivityResult
		val uri = result.data?.data ?: return@registerForActivityResult

		try {
			Moirai.BG.post {
				contentResolver.openInputStream(uri)?.use { stream ->
					State.settings.customFont = Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
					EventBus.dispatch(Event(Type.CUSTOM_FONT, Target.ACTIVITY, mapOf("font" to State.settings.customFont)))
				}
			}
		}
		catch (e: Exception) { e.printStackTrace() }
	}

	// OPEN WITH
	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		handleOpenWithIntent(intent)
	}
	private fun handleOpenWithIntent(intent: Intent?) {
		val action = intent?.action ?: return
		val uri = intent.data ?: return
		if (action != Intent.ACTION_VIEW) return

		val path = when (uri.scheme) {
			"file" -> uri.path
			"content" -> getPathFromContentUri(uri)
			else -> return
		}

		path ?: return
		val file = File(path)

		if (file.isTrack()) {
			State.currentDirectory = file.parentFile ?: return
			State.track.update(file.absolutePath)
			State.autoplay = true

			EventBus.dispatch(Event(Type.DIR_UPDATE, Target.ACTIVITY, mapOf(
				"files" to State.files.serializeFiles(),
				"currentDir" to State.currentDirectory.absolutePath
			)))
			EventBus.dispatch(Event(Type.PLAY_TRACK, Target.ACTIVITY, mapOf("path" to file.absolutePath)))
		}
	}
	private fun getPathFromContentUri(contentUri: Uri): String? {
		var path: String? = null
		val projection = arrayOf(MediaStore.Audio.Media.DATA) // The absolute path column

		val cursor = contentResolver.query(contentUri, projection, null, null, null)
		cursor?.use {
			if (it.moveToFirst()) {
				val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
				path = it.getString(columnIndex)
			}
		}
		return path
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

			Type.THEME_CHANGE -> {
				State.settings.theme = event.data["value"].toString()
				handleThemeChange()
			}

			Type.SLEEP_TIMER_CHANGE -> State.settings.sleepTimer = event.data["value"].toString().toInt()
			Type.SEEK_JUMP_CHANGE -> State.settings.seekJump = event.data["value"].toString().toInt()
			Type.PLAYBACK_SPEED_CHANGE -> State.settings.playbackSpeed = event.data["value"].toString().toFloat()
			Type.SORT_BY_CHANGE -> {
				State.settings.sortBy = event.data["value"].toString()
				EventBus.dispatch(Event(Type.DIR_UPDATE, Target.ACTIVITY, mapOf("files" to State.files.serializeFiles())))
			}
			Type.SECONDARY_CONTROLS_CHANGE -> State.settings.secondaryControls = event.data["value"].toString()
			Type.TOGGLE_TEXT_WRAP -> State.settings.textWrap = event.data["value"].toString().toBoolean()
			Type.FONT_SIZE_CHANGE -> State.settings.fontSize = event.data["value"].toString().toInt()
			Type.TOGGLE_ALBUM_ART -> State.settings.albumArt = event.data["value"].toString().toBoolean()
			Type.CUSTOM_FONT -> handleCustomFont()
		}
	}

	companion object {
		val DISK_PERMISSION = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
		else Manifest.permission.READ_EXTERNAL_STORAGE
	}
}

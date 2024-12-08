package mohsen.muhammad.minimalist.app.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import mohsen.muhammad.minimalist.app.appbar.AppBarManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.app.player.PlayerControlsManager2
import mohsen.muhammad.minimalist.app.settings.SettingsManager
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding
import java.lang.ref.WeakReference


class MainFragment : Fragment() {

	private lateinit var binding: MainFragmentBinding
	private lateinit var permissionRequest: ActivityResultLauncher<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// storage permission...must be in onCreate
		permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
			if (isGranted) {
				initialize()

			} else {
				binding.layoutPermission.root.visibility = View.VISIBLE
				binding.layoutPermission.buttonGrantPermission.setOnClickListener {
					initialize()
				}
			}
		}

		// back button
		requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				onBackPressed()
			}
		})
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = MainFragmentBinding.inflate(inflater, container, false)
		initialize()

		return binding.root
	}

	override fun onStart() {
		super.onStart()
		EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.APP_FOREGROUNDED))
	}

	// initializes everything except the service!
	private fun initialize() {
		when {
			ContextCompat.checkSelfPermission(requireContext(), DISK_PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
				binding.layoutPermission.root.visibility = View.GONE

				// breadcrumbs
				val appBarManager = AppBarManager(binding)
				appBarManager.initialize()

				// explorer
				val explorerManager = ExplorerManager(binding.recyclerViewExplorer)
				explorerManager.initialize()

				// controls
				val playerControlsManager = PlayerControlsManager2(binding)
				playerControlsManager.initialize()

				// settings
				val settingsManager = SettingsManager(binding)
				settingsManager.initialize()

				// service
				val playerIntent = Intent(requireActivity(), PlaybackManager::class.java)
				ContextCompat.startForegroundService(requireActivity(), playerIntent)

				// restore the state, now that everything is initialized (except the service...which is why restoreState is manually called over there)
				if (State.Track.exists) EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.METADATA_UPDATE))

				State.activity = WeakReference(requireActivity())
			}
			shouldShowRequestPermissionRationale(DISK_PERMISSION) -> {
				permissionRequest.launch(DISK_PERMISSION)
			}
			else -> {
				permissionRequest.launch(DISK_PERMISSION)
			}
		}
	}

	private fun onBackPressed() {
		when {
			State.isSettingsSheetVisible -> EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.HIDE_SETTINGS))
			State.isSelectModeActive or State.isSearchModeActive -> {
				State.selectedTracks.clear() // update the state
				State.isSearchModeActive = false
				EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.SELECT_MODE_INACTIVE))
			}
			ExplorerFile.isAtRoot(State.currentDirectory.absolutePath) -> {
				requireActivity().moveTaskToBack(true)
			}
			else -> {
				State.currentDirectory = State.currentDirectory.parentFile!! // don't worry about it
				EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.DIR_CHANGE, State.currentDirectory.absolutePath))
			}
		}
	}

	companion object {
		fun newInstance() = MainFragment()

		val DISK_PERMISSION = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
		else Manifest.permission.READ_EXTERNAL_STORAGE
	}
}

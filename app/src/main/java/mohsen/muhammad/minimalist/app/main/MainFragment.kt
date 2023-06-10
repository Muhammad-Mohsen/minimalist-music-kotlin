package mohsen.muhammad.minimalist.app.main

import android.Manifest
import android.content.Context
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
import mohsen.muhammad.minimalist.app.breadcrumb.BreadcrumbManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.app.player.PlayerControlsManager2
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.*
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding


class MainFragment : Fragment() {

	private lateinit var binding: MainFragmentBinding
	private lateinit var permissionRequest: ActivityResultLauncher<String>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// state - initializing the state in the permission callback sometimes threw "lateinit property sharedPreferences has not been initialized" exception!
		val preferences = requireActivity().getSharedPreferences(Const.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)
		State.initialize(preferences)

		// storage permission
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

	private fun initialize() {
		when {
			ContextCompat.checkSelfPermission(requireContext(), PERMISSION) == PackageManager.PERMISSION_GRANTED -> {
				binding.layoutPermission.root.visibility = View.GONE

				// service
				val playerIntent = Intent(requireActivity(), PlaybackManager::class.java)
				ContextCompat.startForegroundService(requireActivity(), playerIntent)

				// breadcrumbs
				val breadcrumbManager = BreadcrumbManager(binding)
				breadcrumbManager.initialize()

				// explorer
				val explorerManager = ExplorerManager(binding.recyclerViewExplorer)
				explorerManager.initialize()

				// controls
				val playerControlsManager = PlayerControlsManager2(binding)
				playerControlsManager.initialize()

				// after initializing everything, restore the state - at this point, the Playback service isn't started yet, so it hasn't yet registered to the event bus!
				if (State.Track.isInitialized) EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.METADATA_UPDATE))
			}
			shouldShowRequestPermissionRationale(PERMISSION) -> {
				permissionRequest.launch(PERMISSION)
			}
			else -> {
				permissionRequest.launch(PERMISSION)
			}
		}
	}

	private fun onBackPressed() {
		when {
			State.isSelectModeActive -> {
				State.selectedTracks.clear() // update the state
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

	override fun onDestroy() {
		super.onDestroy()
		PlaybackManager.stopSelf()
	}

	companion object {
		fun newInstance() = MainFragment()

		val PERMISSION = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
	}
}

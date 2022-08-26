package mohsen.muhammad.minimalist.app.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import mohsen.muhammad.minimalist.app.breadcrumb.BreadcrumbManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.app.player.PlaybackManager
import mohsen.muhammad.minimalist.app.player.PlayerControlsManager2
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.*
import mohsen.muhammad.minimalist.data.files.FileMetadata
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding


class MainFragment : Fragment() {

	private lateinit var binding: MainFragmentBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = MainFragmentBinding.inflate(inflater, container, false)
		initialize()
		return binding.root
	}

	private fun initialize() {

		// state - initializing the state in the permission callback sometimes threw "lateinit property sharedPreferences has not been initialized" exception!
		val preferences = requireActivity().getSharedPreferences(Const.MINIMALIST_SHARED_PREFERENCES, Context.MODE_PRIVATE)
		State.initialize(preferences)

		// ask for permission
		Dexter.withContext(requireActivity())
			.withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			.withListener(object : PermissionListener {

				override fun onPermissionGranted(response: PermissionGrantedResponse?) {

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

				override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) { token?.continuePermissionRequest() }

				// show permission layout
				override fun onPermissionDenied(response: PermissionDeniedResponse?) {
					binding.layoutPermission.root.visibility = View.VISIBLE

					binding.layoutPermission.buttonGrantPermission.setOnClickListener {
						initialize()
					}
				}

			}).check()
	}

	fun onBackPressed(): Boolean {
		return when {
			State.isSelectModeActive -> {
				State.selectedTracks.clear() // update the state
				EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.SELECT_MODE_INACTIVE))
				true
			}
			State.currentDirectory.absolutePath == FileMetadata.ROOT -> false
			else -> {
				State.currentDirectory = State.currentDirectory.parentFile!! // don't worry about it
				EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.DIR_CHANGE, State.currentDirectory.absolutePath))
				true
			}
		}
	}

	companion object {
		fun newInstance() = MainFragment()
	}
}

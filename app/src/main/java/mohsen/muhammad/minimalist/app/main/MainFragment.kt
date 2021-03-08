package mohsen.muhammad.minimalist.app.main

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import mohsen.muhammad.minimalist.app.breadcrumb.BreadcrumbManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.app.player.PlayerControlsManager2
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.data.files.FileMetadata
import mohsen.muhammad.minimalist.databinding.BreadcrumbBarBinding
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding
import mohsen.muhammad.minimalist.databinding.MediaControls2Binding
import mohsen.muhammad.minimalist.databinding.PermissionRequestBinding


class MainFragment : Fragment() {

	private lateinit var mainBinding: MainFragmentBinding
	private lateinit var permissionBinding: PermissionRequestBinding
	private lateinit var breadcrumbBinding: BreadcrumbBarBinding
	private lateinit var controlsBinding: MediaControls2Binding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		mainBinding = MainFragmentBinding.inflate(inflater, container, false)
		permissionBinding = PermissionRequestBinding.bind(mainBinding.layoutPermission.root)
		breadcrumbBinding = BreadcrumbBarBinding.bind(mainBinding.layoutBreadcrumbs.root)
		controlsBinding = MediaControls2Binding.bind(mainBinding.layoutControls2.root)

		return mainBinding.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		initialize()
	}

	private fun initialize() {
		// ask for permission
		Dexter.withContext(requireActivity())
			.withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			.withListener(object : PermissionListener {

				override fun onPermissionGranted(response: PermissionGrantedResponse?) {

					mainBinding.layoutPermission.root.visibility = View.GONE

					// breadcrumbs
					val breadcrumbManager = BreadcrumbManager(breadcrumbBinding.breadcrumbBarContainer, breadcrumbBinding.multiSelectBarContainer)
					breadcrumbManager.initialize()

					// explorer
					val explorerManager = ExplorerManager(mainBinding.recyclerViewExplorer)
					explorerManager.initialize()

					// controls
					val playerControlsManager = PlayerControlsManager2(controlsBinding.root)
					playerControlsManager.initialize()

					// after initializing everything, restore the state - at this point, the Playback service isn't started yet, so it hasn't yet registered to the event bus!
					if (State.Track.isInitialized) EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.METADATA_UPDATE))
				}

				override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) { token?.continuePermissionRequest() }

				// show permission layout
				override fun onPermissionDenied(response: PermissionDeniedResponse?) {
					mainBinding.layoutPermission.root.visibility = View.VISIBLE

					permissionBinding.buttonGrantPermission.setOnClickListener {
						initialize()
					}
				}

			}).check()
	}

	fun onBackPressed(): Boolean {
		return when {
			State.isSelectModeActive -> {
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

	private fun togglePermissionLayout(show: Boolean) {
		breadcrumbBinding.root.visibility = if (show) View.GONE else View.VISIBLE // this is due to the elevation of the breadcrumbs
		mainBinding.layoutPermission.root.visibility = if (show) View.VISIBLE else View.GONE
	}

	companion object {
		fun newInstance() = MainFragment()
	}
}

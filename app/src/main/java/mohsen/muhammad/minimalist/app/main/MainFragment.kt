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
import kotlinx.android.synthetic.main.breadcrumb_bar.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.media_controls_2.*
import kotlinx.android.synthetic.main.permission_request.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.app.breadcrumb.BreadcrumbManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.app.player.PlayerControlsManager2
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.data.files.FileMetadata


class MainFragment : Fragment() {

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.main_fragment, container, false)
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

					layoutPermission.visibility = View.GONE

					// breadcrumbs
					val breadcrumbManager = BreadcrumbManager(breadcrumbBarContainer, multiSelectBarContainer)
					breadcrumbManager.initialize()

					// explorer
					val explorerManager = ExplorerManager(recyclerViewExplorer)
					explorerManager.initialize()

					// controls
					val playerControlsManager = PlayerControlsManager2(controls_2)
					playerControlsManager.initialize()

					// after initializing everything, restore the state - at this point, the Playback service isn't started yet, so it hasn't yet registered to the event bus!
					if (State.Track.isInitialized) EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.METADATA_UPDATE))
				}

				override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) { token?.continuePermissionRequest() }

				// show permission layout
				override fun onPermissionDenied(response: PermissionDeniedResponse?) {
					layoutPermission.visibility = View.VISIBLE

					buttonGrantPermission.setOnClickListener {
						initialize()
					}
				}

			}).check()
	}

	fun onBackPressed(): Boolean {
		val currentDirectory = State.currentDirectory

		// TODO check if select mode is active, and deactivate it if so

		return if (currentDirectory.absolutePath == FileMetadata.ROOT) false
		else {
			val parentDir = currentDirectory.parentFile
			State.currentDirectory = parentDir
			EventBus.send(SystemEvent(EventSource.FRAGMENT, EventType.DIR_CHANGE, parentDir.absolutePath))

			true
		}
	}

	private fun togglePermissionLayout(show: Boolean) {
		breadcrumbs.visibility = if (show) View.GONE else View.VISIBLE // this is due to the elevation of the breadcrumbs
		layoutPermission.visibility = if (show) View.VISIBLE else View.GONE
	}

	companion object {
		fun newInstance() = MainFragment()
	}
}

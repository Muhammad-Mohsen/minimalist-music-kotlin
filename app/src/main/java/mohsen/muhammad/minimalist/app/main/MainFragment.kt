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
import kotlinx.android.synthetic.main.media_controls.*
import kotlinx.android.synthetic.main.permission_request.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.app.breadcrumb.BreadcrumbManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.app.player.PlayerControlsManager
import mohsen.muhammad.minimalist.app.player.PlayerService
import mohsen.muhammad.minimalist.core.FileHelper
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.data.Prefs
import mohsen.muhammad.minimalist.data.Type
import java.io.File


class MainFragment : Fragment(), OnListItemInteractionListener<File> {

	private var breadcrumbManager: BreadcrumbManager? = null
	private var explorerManager: ExplorerManager? = null
	private var playerControlsManager: PlayerControlsManager? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.main_fragment, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		initialize()
	}

	private fun initialize() {
		// ask for permission
		Dexter.withActivity(requireActivity())
			.withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			.withListener(object : PermissionListener {

				override fun onPermissionGranted(response: PermissionGrantedResponse?) {

					togglePermissionLayout(false) // hide permission layout

					val currentDirectory = Prefs.getCurrentDirectory(requireContext())

					// breadcrumbs
					breadcrumbManager = BreadcrumbManager(recyclerViewBreadcrumbs, buttonBack, this@MainFragment, currentDirectory)
					breadcrumbManager?.initialize()

					// explorer
					explorerManager = ExplorerManager(recyclerViewExplorer, this@MainFragment, currentDirectory)
					explorerManager?.initialize()

					// controls
					playerControlsManager = PlayerControlsManager(controls)
					playerControlsManager?.initialize()
				}

				override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
					token?.continuePermissionRequest()
				}

				// show permission layout
				override fun onPermissionDenied(response: PermissionDeniedResponse?) {
					togglePermissionLayout(true)

					buttonGrantPermission.setOnClickListener {
						initialize()
					}
				}

			}).check()
	}

	fun onBackPressed(): Boolean {
		val currentDirectory = Prefs.getCurrentDirectory(requireContext())

		return if (currentDirectory.absolutePath == FileHelper.ROOT) false
		else {
			onListItemClick(currentDirectory.parentFile, Type.CRUMB)
			true
		}
	}

	override fun onListItemClick(data: File?, source: Int) {
		if (data == null) return

		val currentDirectory = Prefs.getCurrentDirectory(requireContext())

		if (source == Type.CRUMB || source == Type.DIRECTORY) { // breadcrumb, and directory item clicks

			if (data.absolutePath == currentDirectory.absolutePath) return // clicking the same directory should do nothing

			Prefs.setCurrentDirectory(requireContext(), data)

			breadcrumbManager?.onDirectoryChange(data) // repopulate the breadcrumb bar
			explorerManager?.onDirectoryChange(data) // repopulate the recycler views

		} else { // track item clicks
			PlayerService.instance?.playTrack(data.absolutePath)
		}
	}

	override fun onListItemLongClick(data: File?, source: Int) {
		// eventually
	}

	private fun togglePermissionLayout(show: Boolean) {
		breadcrumbs.visibility = if (show) View.GONE else View.VISIBLE // this is due to the elevation of the breadcrumbs
		layoutPermission.visibility = if (show) View.VISIBLE else View.GONE
	}

	companion object {
		fun newInstance() = MainFragment()
	}
}
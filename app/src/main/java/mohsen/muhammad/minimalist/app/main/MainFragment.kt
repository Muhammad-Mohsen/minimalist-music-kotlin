package mohsen.muhammad.minimalist.app.main

import android.Manifest
import android.os.Bundle
import android.util.Log
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
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.app.breadcrumb.BreadcrumbManager
import mohsen.muhammad.minimalist.app.explorer.ExplorerManager
import mohsen.muhammad.minimalist.core.FileHelper
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.data.Prefs
import mohsen.muhammad.minimalist.data.Type
import java.io.File


class MainFragment : Fragment(), OnListItemInteractionListener<File> {

	private var breadcrumbManager: BreadcrumbManager? = null
	private var explorerManager: ExplorerManager? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.main_fragment, container, false)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)

		// ask for permission
		Dexter.withActivity(requireActivity())
			.withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			.withListener(object : PermissionListener {

				override fun onPermissionGranted(response: PermissionGrantedResponse?) {
					// TODO do it better
					val currentDirectory = Prefs.getSavedCurrentDirectory(requireContext())
					Prefs.currentDirectory = currentDirectory

					breadcrumbManager = BreadcrumbManager(recyclerViewBreadcrumbs, buttonBack, this@MainFragment, currentDirectory)
					breadcrumbManager?.initialize()

					explorerManager = ExplorerManager(recyclerViewExplorer, this@MainFragment, currentDirectory)
					explorerManager?.initialize()

					// back button click listener
					buttonBack.setOnClickListener {
						if (Prefs.currentDirectory.absolutePath != FileHelper.ROOT)
							onListItemClick(Prefs.currentDirectory.parentFile, Type.CRUMB)
					}
				}

				override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {}

				// TODO show permission layout
				override fun onPermissionDenied(response: PermissionDeniedResponse?) {
					// ExplorerManager.showHidePermissionLayout(false)
					Log.d("main", "whoops!!")
				}

			}).check()
	}

	fun onBackPressed(): Boolean {
		return if (Prefs.currentDirectory.absolutePath == FileHelper.ROOT) false
		else {
			onListItemClick(Prefs.currentDirectory.parentFile, Type.CRUMB)
			true
		}
	}

	override fun onListItemClick(data: File?, source: Int) {
		if (data == null) return

		if (source == Type.CRUMB || source == Type.DIRECTORY) { // breadcrumb, and directory item clicks

			if (data.absolutePath == Prefs.currentDirectory.absolutePath) return // clicking the same directory should do nothing

			Prefs.currentDirectory = data

			breadcrumbManager?.onDirectoryChange(data) // repopulate the breadcrumb bar
			explorerManager?.onDirectoryChange(data) // repopulate the recycler views

		} else { // track item clicks

		}
	}

	override fun onListItemLongClick(data: File?, source: Int) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	companion object {
		fun newInstance() = MainFragment()
	}
}

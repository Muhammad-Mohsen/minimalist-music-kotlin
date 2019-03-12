package mohsen.muhammad.minimalist.app.explorer

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.data.Prefs
import mohsen.muhammad.minimalist.data.files.FileCache
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * controls layout properties (e.g. scroll position for each directory, display of permission request layout)
 * for the explorer view
 */

class ExplorerManager(
	private val recyclerViewExplorer: RecyclerView,
	private val interactionHandler: OnListItemInteractionListener<File>,
	private val linearLayoutPermission: LinearLayout? = null,
	private val linearLayoutEmptyDir: LinearLayout? = null
) {

	private val explorerAdapter: ExplorerAdapter
        get() = recyclerViewExplorer.adapter as ExplorerAdapter

	fun initialize() {

		val currentDirectory = Prefs.getCurrentDirectory(recyclerViewExplorer.context)
		val selectedTrack = Prefs.getCurrentTrack(recyclerViewExplorer.context)

		val explorerAdapter = ExplorerAdapter(FileCache.getExplorerFilesByDirectory(currentDirectory), selectedTrack, interactionHandler)
		recyclerViewExplorer.adapter = explorerAdapter
		recyclerViewExplorer.itemAnimator = SlideInLeftAnimator()
	}

    // called when the current directory is changed
    fun onDirectoryChange(dir: File) {
	    val files = FileCache.getExplorerFilesByDirectory(dir)

	    if (files.isNotEmpty()) {
			toggleEmptyDirLayout(false)
		    explorerAdapter.update(files)

	    } else {
		    toggleEmptyDirLayout(true)
	    }
    }

	fun onSelectionChange(path: String) {
		val context = recyclerViewExplorer.context

		val oldSelection = Prefs.getCurrentTrack(context)
		Prefs.setCurrentTrack(context, path)

		explorerAdapter.updateSelection(path, oldSelection)
	}

	private fun toggleEmptyDirLayout(show: Boolean) {
		recyclerViewExplorer.visibility = if (show) View.GONE else View.VISIBLE
		linearLayoutEmptyDir?.visibility = if (show) View.VISIBLE else View.GONE
	}
}

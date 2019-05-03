package mohsen.muhammad.minimalist.app.explorer

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.*
import mohsen.muhammad.minimalist.data.files.FileCache
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * controls layout properties (e.g. scroll position for each directory, display of permission request layout)
 * for the explorer view
 */

class ExplorerManager(
	private val recyclerViewExplorer: RecyclerView,
	private val linearLayoutPermission: LinearLayout? = null,
	private val linearLayoutEmptyDir: LinearLayout? = null
) : EventBus.Subscriber, OnListItemInteractionListener<File> {

	private val explorerAdapter: ExplorerAdapter
		get() = recyclerViewExplorer.adapter as ExplorerAdapter

	fun initialize() {

		EventBus.subscribe(this)

		val currentDirectory = State.currentDirectory
		val selectedTrack = State.Track.path

		val explorerAdapter = ExplorerAdapter(FileCache.getExplorerFilesByDirectory(currentDirectory), selectedTrack, this)
		recyclerViewExplorer.adapter = explorerAdapter
		recyclerViewExplorer.itemAnimator = SlideInLeftAnimator()
	}

	// explorer item click
	override fun onListItemClick(data: File?, source: Int) {
		if (data == null) return

		val currentDirectory = State.currentDirectory

		if (source == ItemType.DIRECTORY) { // breadcrumb, and directory item clicks

			if (data.absolutePath == currentDirectory.absolutePath) return // clicking the same directory should do nothing

			State.currentDirectory = data
			onDirectoryChange(data) // repopulate the recycler views

			// breadcrumbManager?.onDirectoryChange(data) // repopulate the breadcrumb bar
			EventBus.send(SystemEvent(EventSource.EXPLORER, EventType.DIR_CHANGE))

		} else { // track item clicks
			EventBus.send(SystemEvent(EventSource.EXPLORER, EventType.PLAY_ITEM, data.absolutePath))
			onSelectionChange(data.absolutePath)
		}
	}

	override fun onListItemLongClick(data: File?, source: Int) {
		// eventually
	}

	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent && data.source != EventSource.EXPLORER) {
			Handler(Looper.getMainLooper()).post {

				when (data.type) {
					EventType.METADATA_UPDATE -> explorerAdapter.updateSelection(State.Track.path)
					EventType.DIR_CHANGE -> onDirectoryChange(State.currentDirectory)
				}

			}
		}
	}

	// called when the current directory is changed
	private fun onDirectoryChange(dir: File) {
		val files = FileCache.getExplorerFilesByDirectory(dir)

		if (files.isNotEmpty()) {
			toggleEmptyDirLayout(false)
			explorerAdapter.update(files)

		} else {
			toggleEmptyDirLayout(true)
		}
	}

	private fun onSelectionChange(path: String) {
		State.Track.path = path
		explorerAdapter.updateSelection(path)
	}

	private fun toggleEmptyDirLayout(show: Boolean) {
		recyclerViewExplorer.visibility = if (show) View.GONE else View.VISIBLE
		linearLayoutEmptyDir?.visibility = if (show) View.VISIBLE else View.GONE
	}
}

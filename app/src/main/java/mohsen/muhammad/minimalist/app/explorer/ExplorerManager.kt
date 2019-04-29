package mohsen.muhammad.minimalist.app.explorer

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.PlaybackEvent
import mohsen.muhammad.minimalist.data.PlaybackEventSource
import mohsen.muhammad.minimalist.data.PlaybackEventType
import mohsen.muhammad.minimalist.data.State
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
) : EventBus.Subscriber {

	private val explorerAdapter: ExplorerAdapter
        get() = recyclerViewExplorer.adapter as ExplorerAdapter

	fun initialize() {

		EventBus.subscribe(this)

		val currentDirectory = State.currentDirectory
		val selectedTrack = State.currentTrack

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
		State.currentTrack = path
		explorerAdapter.updateSelection(path)
	}

	private fun toggleEmptyDirLayout(show: Boolean) {
		recyclerViewExplorer.visibility = if (show) View.GONE else View.VISIBLE
		linearLayoutEmptyDir?.visibility = if (show) View.VISIBLE else View.GONE
	}

	override fun receive(data: EventBus.EventData) {
		if (data is PlaybackEvent && data.source != PlaybackEventSource.EXPLORER) {
			when (data.type) {
				PlaybackEventType.UPDATE_METADATA -> Handler(Looper.getMainLooper()).post {
					explorerAdapter.updateSelection(data.extras.split(";").last())
				}
			}
		}
	}
}

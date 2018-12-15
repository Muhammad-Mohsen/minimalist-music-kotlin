package mohsen.muhammad.minimalist.app.explorer

import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
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
	private val currentDirectory: File,
	private val linearLayoutPermission: LinearLayout? = null,
	private val linearLayoutEmptyDir: LinearLayout? = null
) {

	private val explorerAdapter: ExplorerAdapter
        get() = recyclerViewExplorer.adapter as ExplorerAdapter

	fun initialize() {
		val explorerAdapter = ExplorerAdapter(FileCache.getExplorerFilesByDirectory(currentDirectory), interactionHandler)
		recyclerViewExplorer.adapter = explorerAdapter
		recyclerViewExplorer.itemAnimator = SlideInLeftAnimator()
	}

    // called when the current directory is changed
    fun onDirectoryChange(dir: File) {
        explorerAdapter.update(FileCache.getExplorerFilesByDirectory(dir))
    }

    // called when the current track changes (playback completes, or another track is selected)
    // if the directory was changed between track changes, the oldPosition will be -1 (check is made in PlaybackManager)
    fun onCurrentTrackChange(newPosition: Int, oldPosition: Int) {
        explorerAdapter.updateCurrentItem(newPosition, oldPosition)
    }

    // called when the current playlist is changed
    // if the directory was changed between playlist changes, the oldPositionList will be an empty list (check is made in PlaybackManager)
    fun onPlaylistChange(newPositionList: List<Int>, oldPositionList: List<Int>) {
        explorerAdapter.updatePlaylist(newPositionList, oldPositionList)
    }
}

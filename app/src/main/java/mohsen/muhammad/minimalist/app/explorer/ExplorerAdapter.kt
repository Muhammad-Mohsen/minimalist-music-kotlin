package mohsen.muhammad.minimalist.app.explorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.ExtendedFrameLayout
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.data.ItemType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import mohsen.muhammad.minimalist.databinding.ExplorerItemBinding
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Adapter class for the explorer RecyclerView
 */

class ExplorerAdapter(
	explorerFiles: ArrayList<ExplorerFile>,
	private var selection: String,
	private val interactionListener: OnListItemInteractionListener<File>

) : RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder>() {

	// ArrayList has to be copied in order to separate the cache reference from the Adapter's data set reference
	// otherwise, whatever cached list that was used to initialize the adapter will be changed whenever the data set is changed
	private val files: ArrayList<ExplorerFile> = ArrayList(explorerFiles)
	private var originals = ArrayList(files)

	override fun getItemCount(): Int {
		return files.size
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExplorerViewHolder {
		return ExplorerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.explorer_item, parent, false))
	}

	// binds the ViewHolder with the content
	override fun onBindViewHolder(holder: ExplorerViewHolder, position: Int) {

		val file = files[position]
		with(holder) {

			icon.setColorFilter(R.color.mainBackground)
			icon.setImageResource(if (file.isDirectory) R.mipmap.ic_directory else R.mipmap.ic_track)
			title.text = file.name

			// text/icon color
			var itemColor = itemView.context.getColor(R.color.mainForeground)
			if (!file.isDirectory && !State.playlist.contains(file.absolutePath)) itemColor = itemView.context.getColor(R.color.explorerForegroundLight)
			icon.setColorFilter(itemColor)
			title.setTextColor(itemColor)

			// selection states
			currentlyPlayingView.alpha = if (isSelected(file)) OPAQUE else TRANSPARENT

			selectedView.alpha = if (State.selectedTracks.contains(file.absolutePath)) OPAQUE else TRANSPARENT

			// listeners
			val itemType = if (file.isDirectory) ItemType.DIRECTORY else ItemType.TRACK
			itemView.setOnClickListener {
				interactionListener.onListItemClick(file, itemType)
			}
			itemView.setOnLongClickListener {
				if (file.isDirectory) return@setOnLongClickListener true // no long click for you!

				interactionListener.onListItemLongClick(file, itemType)
				return@setOnLongClickListener true
			}
		}
	}

	// updates the entire list (animated)
	internal fun update(files: ArrayList<ExplorerFile>) {
		val initialSize = this.files.size

		this.files.retainAll(ArrayList<ExplorerFile>().toSet()) // remove everything
		notifyItemRangeRemoved(0, initialSize)

		for (file in files) this.files.add(file)
		notifyItemRangeInserted(0, this.files.size)

		originals = ArrayList(files)
	}

	// updates the selected item
	internal fun updateSelection(newSelection: String) {
		selection = newSelection
		notifyItemRangeChanged(0, files.size)
	}

	internal fun updateMultiSelection(path: String) {
		val position = getPositionByPath(path)
		notifyItemChanged(position)
	}

	internal fun filter(q: String) {
		for (i in files.indices.reversed()) {
			if (files.elementAt(i).name.contains(q, true)) continue

			files.removeAt(i)
			notifyItemRemoved(i)
		}

		val toAdd = originals.filter { f -> q.isBlank() || f.name.contains(q, true) } // this collapses the indices so that...
		for ((i, file) in toAdd.withIndex()) {
			if (files.elementAtOrNull(i)?.name == file.name) continue

			files.add(i, file) // ...these don't blow up with an IndexOutOfBounds...
			notifyItemInserted(i) // ...for example "files" only has 3 elements, and there's a match on index 20 in "originals"
		}
	}

	// gets item position by absolutePath
	internal fun getPositionByPath(path: String): Int {
		return files.indexOfFirst { f -> f.absolutePath == path }
	}

	// indicates whether the file should be marked as selected or not
	private fun isSelected(file: ExplorerFile): Boolean {
		return selection == file.absolutePath
	}

	// ViewHolder class
	class ExplorerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val binding = ExplorerItemBinding.bind(itemView)

		val icon: ImageView = binding.imageViewIcon
		val title: TextView = binding.textViewTitle

		val currentlyPlayingView: ExtendedFrameLayout = binding.frameLayoutCurrent
		val selectedView: ExtendedFrameLayout = binding.imageViewSelected

		val duration: TextView = binding.textViewDuration
	}

	companion object {
		const val OPAQUE = 1F
		const val TRANSPARENT = 0F
	}
}

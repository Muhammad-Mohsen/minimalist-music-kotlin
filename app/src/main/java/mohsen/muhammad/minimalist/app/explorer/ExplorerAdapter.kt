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
import mohsen.muhammad.minimalist.core.ext.setLayoutMargins
import mohsen.muhammad.minimalist.core.ext.toDip
import mohsen.muhammad.minimalist.data.Const
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

			// set the margins on the first and last items in the list (they are different than the rest)
			val topMargin = if (position == 0) Const.Dimen.FIRST_ITEM.toDip(itemView.context) else 0f
			val bottomMargin = if (position == files.lastIndex) Const.Dimen.LAST_ITEM.toDip(itemView.context) else 0f
			itemView.setLayoutMargins(0, topMargin.toInt(), 0, bottomMargin.toInt()) // TODO possible performance issue

			icon.setColorFilter(R.color.colorBackground)
			icon.setImageResource(if (file.isDirectory) R.mipmap.ic_directory else R.mipmap.ic_track)
			title.text = file.name

			// text/icon color
			var itemColor = itemView.context.getColor(R.color.colorOnBackgroundDark)
			if (!file.isDirectory && !State.playlist.contains(file.absolutePath)) itemColor = itemView.context.getColor(R.color.colorSecondary)
			icon.setColorFilter(itemColor)
			title.setTextColor(itemColor)

			// selection states
			if (isSelected(file)) currentlyPlayingView.alpha = Const.Alpha.OPAQUE
			else currentlyPlayingView.alpha = Const.Alpha.TRANSPARENT

			selectedView.alpha = if (State.selectedTracks.contains(file.absolutePath)) Const.Alpha.OPAQUE else Const.Alpha.TRANSPARENT

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

	// gets item position by absolutePath
	private fun getPositionByPath(path: String): Int {
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

		// val subtitle: TextView = binding.textViewSubtitle
		val duration: TextView = binding.textViewDuration
	}
}

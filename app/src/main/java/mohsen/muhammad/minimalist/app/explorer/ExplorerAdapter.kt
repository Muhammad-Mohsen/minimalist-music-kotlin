package mohsen.muhammad.minimalist.app.explorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.explorer_list_item.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.ExtendedFrameLayout
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.core.ext.toDip
import mohsen.muhammad.minimalist.data.Const
import mohsen.muhammad.minimalist.data.ItemType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.files.ExplorerFile
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
		return ExplorerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.explorer_list_item, parent, false))
	}

	// binds the ViewHolder with the content
	override fun onBindViewHolder(holder: ExplorerViewHolder, position: Int) {

		val file = files[position]
		with(holder) {

			// set the margins on the first and last items in the list (they are different than the rest)
			val topMargin = if (position == 0) Const.Margin.FIRST_ITEM.toDip(itemView.context) else 0f
			val bottomMargin = if (position == files.size - 1) Const.Margin.LAST_ITEM.toDip(itemView.context) else 0f
			val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
			params.setMargins(0, topMargin.toInt(), 0, bottomMargin.toInt())

			itemView.layoutParams = params

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

			// click listener
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

		this.files.retainAll(ArrayList()) // remove everything
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
		val icon: ImageView = itemView.imageViewIcon
		val title: TextView = itemView.textViewTitle

		val currentlyPlayingView: ExtendedFrameLayout = itemView.frameLayoutCurrent
		val selectedView: ExtendedFrameLayout = itemView.imageViewSelected

		val subtitle: TextView = itemView.textViewSubtitle
		val duration: TextView = itemView.textViewDuration
	}
}

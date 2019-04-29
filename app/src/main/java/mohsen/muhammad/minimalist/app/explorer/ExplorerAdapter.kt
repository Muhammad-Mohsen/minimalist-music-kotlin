package mohsen.muhammad.minimalist.app.explorer

import android.content.Context
import android.util.TypedValue
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
import mohsen.muhammad.minimalist.data.ItemType
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Adapter class for the explorer RecyclerView
 */

class ExplorerAdapter(
	explorerFiles: ArrayList<ExplorerFile>,
	selectedTrackPath: String,
	private val interactionListener: OnListItemInteractionListener<File>

) : RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder>() {

	// ArrayList has to be copied in order to separate the cache reference from the Adapter's data set reference
	// otherwise, whatever cached list that was used to initialize the adapter will be changed whenever the data set is changed
	private val files: ArrayList<ExplorerFile> = ArrayList(explorerFiles)
	private var selection: String = selectedTrackPath

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

			val resources = itemView.context.resources
			val topMargin = if (position == 0) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics) else 0f

			val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
			params.setMargins(0, topMargin.toInt(), 0, 0)

			itemView.layoutParams = params

			icon.setImageResource(if (file.isDirectory) R.mipmap.ic_directory else R.mipmap.ic_track)
			title.text = file.name

			// selection states
			selectionView.visibility = if (isSelected(file)) View.VISIBLE else View.GONE

			// click listener
			val itemType = if (file.isDirectory) ItemType.DIRECTORY else ItemType.TRACK
			itemView.setOnClickListener {
				interactionListener.onListItemClick(file, itemType)
			}
		}
	}

	// updates the entire list (animated)
	internal fun update(files: ArrayList<ExplorerFile>) {
		val initialSize = this.files.size

		this.files.retainAll(ArrayList()) // remove everything
		notifyItemRangeRemoved(0, initialSize)

		for (file in files)
			this.files.add(file)

		notifyItemRangeInserted(0, this.files.size)
	}

	// updates the selected item
	internal fun updateSelection(newSelection: String) {
		// update the selected path
		val oldSelection = selection
		selection = newSelection

		// remove the old selection (if possible)
		val oldSelectedPosition = getPositionByPath(oldSelection)
		notifyItemChanged(oldSelectedPosition)

		// update the selection
		val newSelectedPosition = getPositionByPath(newSelection)
		notifyItemChanged(newSelectedPosition)
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

		val context: Context = itemView.context

		val icon: ImageView = itemView.imageViewIcon

		val selectionView: ExtendedFrameLayout = itemView.frameLayoutSelected

		val title: TextView = itemView.textViewTitle
		val subtitle: TextView = itemView.textViewSubtitle

		val duration: TextView = itemView.textViewDuration
	}
}

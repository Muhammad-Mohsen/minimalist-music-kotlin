package mohsen.muhammad.minimalist.app.explorer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.explorer_list_item.view.*
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.data.Type
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Adapter class for the explorer RecyclerView
 */

class ExplorerAdapter(explorerFiles: ArrayList<ExplorerFile>, private val interactionListener: OnListItemInteractionListener<File>) : RecyclerView.Adapter<ExplorerAdapter.ExplorerViewHolder>() {

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
			icon.setImageResource(if (file.isDirectory) R.mipmap.ic_directory else R.mipmap.ic_track)
			title.text = file.name

			// TODO add icon states

			// click listener
		    val itemType = if (file.isDirectory) Type.DIRECTORY else Type.TRACK
		    itemView.setOnClickListener {
			    interactionListener.onListItemClick(file, itemType)
		    }
	    }
    }

    internal fun update(files: ArrayList<ExplorerFile>) {
        val initialSize = this.files.size

        this.files.retainAll(ArrayList()) // remove everything
        notifyItemRangeRemoved(0, initialSize)

        for (file in files)
            this.files.add(file)

        notifyItemRangeInserted(0, this.files.size)
    }

    internal fun updateCurrentItem(newPosition: Int, oldPosition: Int) {
        if (oldPosition != -1)
            notifyItemChanged(oldPosition)

        notifyItemChanged(newPosition)
    }

    internal fun updatePlaylist(newPositionList: List<Int>, oldPositionList: List<Int>) {
        for (position in oldPositionList)
            notifyItemChanged(position)

        for (position in newPositionList)
            notifyItemChanged(position)
    }

	class ExplorerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

		val context: Context = itemView.context

		val icon: ImageView = itemView.imageViewIcon

		val title: TextView = itemView.textViewTitle
		val subtitle: TextView = itemView.textViewSubtitle

		val duration: TextView = itemView.textViewDuration
	}
}

package mohsen.muhammad.minimalist.app.breadcrumb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnListItemClickListener
import mohsen.muhammad.minimalist.data.ItemType
import mohsen.muhammad.minimalist.databinding.BreadcrumbBarItemBinding
import java.io.File
import java.util.*


/**
 * Created by muhammad.mohsen on 11/3/2018.
 */


class BreadcrumbAdapter(file: File, private val interactionListener: OnListItemClickListener<File>) : RecyclerView.Adapter<BreadcrumbAdapter.CrumbViewHolder>() {

	private val crumbs: ArrayList<String> = ArrayList(file.absolutePath.split("/")).apply {
		removeAt(0) // remove the empty crumb
	}

	// where the views are inflated
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrumbViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return CrumbViewHolder(inflater.inflate(R.layout.breadcrumb_bar_item, parent, false))
	}

	override fun onBindViewHolder(holder: CrumbViewHolder, position: Int) {
		val file = File(crumbs.subList(0, position + 1).joinToString("/"))

		with(holder) {
			crumbText.text = file.name

			crumbButton.setOnClickListener {
				interactionListener.onListItemClick(file, ItemType.CRUMB)
			}
		}
	}

	override fun getItemCount(): Int {
		return crumbs.size
	}

	internal fun update(file: File) {
		val crumbList = ArrayList(file.absolutePath.split("/"))
		crumbList.removeAt(0)

		// if you're at the same directory, don't do anything
		if (crumbList[crumbList.size - 1] == crumbs[crumbs.size - 1] && crumbList.size == crumbs.size) return

		// +ve means that we're going deeper
		// -ve means that we're going back
		val navigationSteps = crumbList.size - crumbs.size

		// only one can be added at a time
		if (navigationSteps > 0) {
			crumbs.add(crumbList[crumbList.size - 1]) // add the new crumb
			notifyItemInserted(crumbs.size - 1)

		} else { // one or more crumbs can be removed
			val initialSize = crumbs.size
			for (i in initialSize - 1 downTo initialSize + navigationSteps) crumbs.removeAt(i)

			// starting position will be the final size (last index of the updated list + 1)
			notifyItemRangeRemoved(crumbs.size, -navigationSteps)
		}
	}

	class CrumbViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val binding = BreadcrumbBarItemBinding.bind(itemView)

		val crumbText = binding.crumbText
		val crumbButton = binding.crumbButton
	}

}

package mohsen.muhammad.minimalist.app.breadcrumb

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.FileHelper
import mohsen.muhammad.minimalist.core.OnListItemInteractionListener
import mohsen.muhammad.minimalist.core.animateDrawable
import mohsen.muhammad.minimalist.data.Prefs
import mohsen.muhammad.minimalist.data.Type
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Controls layout properties (scroll position, back button) for the breadcrumb bar layout
 */

class BreadcrumbManager(
	private val recyclerViewBreadcrumb: RecyclerView,
	private val buttonBack: ImageView,
	private val interactionHandler: OnListItemInteractionListener<File>,
	private val currentDirectory: File
) {

	private val breadcrumbAdapter: BreadcrumbAdapter
		get() = recyclerViewBreadcrumb.adapter as BreadcrumbAdapter

	fun initialize() {

		val breadcrumbAdapter = BreadcrumbAdapter(currentDirectory, interactionHandler)
		recyclerViewBreadcrumb.adapter = breadcrumbAdapter

		// set back button icon.
		// a lot of work for such a simple proposition
		val animationResourceId: Int
		val tag: Int
		if (currentDirectory.absolutePath == FileHelper.ROOT) {
			animationResourceId = R.drawable.anim_root_back
			tag = R.drawable.anim_back_root

		} else {
			animationResourceId = R.drawable.anim_back_root
			tag = R.drawable.anim_root_back
		}

		val drawable = ContextCompat.getDrawable(buttonBack.context, animationResourceId)
		buttonBack.setImageDrawable(drawable)

		// because the animation is not started,
		// the tag should be the opposite of the animation drawable being set on the button
		buttonBack.tag = tag

		// scroll to end
		recyclerViewBreadcrumb.scrollToPosition(currentDirectory.absolutePath.split("/").size - 2)

		// back button click listener
		buttonBack.setOnClickListener {
			val dir = Prefs.getCurrentDirectory(buttonBack.context)
			if (dir.absolutePath != FileHelper.ROOT)
				interactionHandler.onListItemClick(dir.parentFile, Type.CRUMB)
		}
	}

    fun onDirectoryChange(currentDirectory: File) {
	    breadcrumbAdapter.update(currentDirectory)

	    recyclerViewBreadcrumb.scrollToPosition(currentDirectory.absolutePath.split("/").size - 2)

        // if currently at the root, animate to the root icon
        if (currentDirectory.absolutePath == FileHelper.ROOT)
            animateBackButton(false)

        else if (currentDirectory.absolutePath != FileHelper.ROOT && buttonBack.tag as Int != R.drawable.anim_root_back)
            animateBackButton(true) // if not at the root AND not displaying the back icon, animate to it
    }

    // forward means that we're going deeper into the directory hierarchy
    private fun animateBackButton(forward: Boolean) {
        buttonBack.animateDrawable(if (forward) R.drawable.anim_root_back else R.drawable.anim_back_root)
    }
}

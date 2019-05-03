package mohsen.muhammad.minimalist.app.breadcrumb

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnListItemClickListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.animateDrawable
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.data.files.FileHelper
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Controls layout properties (scroll position, back button) for the breadcrumb bar layout
 */

class BreadcrumbManager(
	private val recyclerViewBreadcrumb: RecyclerView,
	private val buttonBack: ImageView
) : EventBus.Subscriber, OnListItemClickListener<File> {

	private val breadcrumbAdapter: BreadcrumbAdapter
		get() = recyclerViewBreadcrumb.adapter as BreadcrumbAdapter

	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		val currentDirectory = State.currentDirectory

		val breadcrumbAdapter = BreadcrumbAdapter(currentDirectory, this)
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

			if (State.currentDirectory.absolutePath != FileHelper.ROOT) {
				val dir = State.currentDirectory.parentFile
				State.currentDirectory = dir

				onDirectoryChange(dir) // repopulate the breadcrumb bar
				EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.DIR_CHANGE))
			}
		}
	}

	// crumb click handler
	override fun onListItemClick(data: File?, source: Int) {
		if (data == null) return

		val currentDirectory = State.currentDirectory

		if (data.absolutePath == currentDirectory.absolutePath) return // clicking the same directory should do nothing

		State.currentDirectory = data
		onDirectoryChange(data) // repopulate the recycler views

		// breadcrumbManager?.onDirectoryChange(data) // repopulate the breadcrumb bar
		EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.DIR_CHANGE, data.absolutePath))
	}

	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent && data.source != EventSource.BREADCRUMB) {
			Handler(Looper.getMainLooper()).post {

				when (data.type) {
					EventType.DIR_CHANGE -> onDirectoryChange(State.currentDirectory)
				}

			}
		}
	}

	private fun onDirectoryChange(currentDirectory: File) {
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

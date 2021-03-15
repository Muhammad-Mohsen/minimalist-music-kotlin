package mohsen.muhammad.minimalist.app.breadcrumb

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.OnListItemClickListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.*
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.data.files.FileMetadata
import mohsen.muhammad.minimalist.databinding.BreadcrumbBarBinding
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Controls layout properties (scroll position, back button) for the breadcrumb bar layout
 */

class BreadcrumbManager(mainBinding: MainFragmentBinding) : EventBus.Subscriber, OnListItemClickListener<File> {

	private val binding = BreadcrumbBarBinding.bind(mainBinding.layoutBreadcrumbs.root as View)

	private val recyclerViewBreadcrumbs = binding.recyclerViewBreadcrumbs
	private val buttonBack = binding.buttonBack

	private val buttonCancel = binding.buttonCancel
	private val buttonAppendToPlaylist = binding.buttonAddSelection
	private val buttonPlaySelected = binding.buttonPlaySelected
	private val textViewSelectionCount = binding.textViewSelectionCount

	private val breadcrumbAdapter: BreadcrumbAdapter
		get() = recyclerViewBreadcrumbs.adapter as BreadcrumbAdapter

	fun initialize() {

		// event bus subscription
		EventBus.subscribe(this)

		val currentDirectory = State.currentDirectory

		val breadcrumbAdapter = BreadcrumbAdapter(currentDirectory, this)
		recyclerViewBreadcrumbs.adapter = breadcrumbAdapter

		// set back button icon
		val animationResourceId = if (currentDirectory.absolutePath == FileMetadata.ROOT) R.drawable.anim_root_back else R.drawable.anim_back_root
		val drawable = ContextCompat.getDrawable(buttonBack.context, animationResourceId)
		buttonBack.setImageDrawable(drawable)

		// scroll to end
		recyclerViewBreadcrumbs.scrollToPosition(currentDirectory.absolutePath.split("/").size - 2)

		// back button click listener
		buttonBack.setOnClickListener {

			if (State.currentDirectory.absolutePath != FileMetadata.ROOT) {
				val dir = State.currentDirectory.parentFile ?: return@setOnClickListener

				State.currentDirectory = dir
				onDirectoryChange(dir) // repopulate the breadcrumb bar
				EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.DIR_CHANGE))
			}
		}

		// set the cancel multi-select button listener
		buttonCancel.setOnClickListener {
			State.selectedTracks.clear() // update the state
			onSelectModeChange()
			EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.SELECT_MODE_INACTIVE))
		}
		// set the add to playlist button listener
		buttonAppendToPlaylist.setOnClickListener {
			EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.SELECT_MODE_APPEND))
		}
		// set the play playlist button listener
		buttonPlaySelected.setOnClickListener {
			// update state
			State.playlist.updateItems(State.selectedTracks)
			State.selectedTracks.clear()

			EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.PLAY_SELECTED))
			onSelectModeChange()
		}
	}

	// crumb click handler
	override fun onListItemClick(data: File?, source: Int) {
		if (data == null) return
		if (data.absolutePath == State.currentDirectory.absolutePath) return // clicking the same directory should do nothing

		State.currentDirectory = data
		onDirectoryChange(data) // repopulate the recycler views

		EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.DIR_CHANGE, data.absolutePath))
	}

	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent && data.source != EventSource.BREADCRUMB) {
			Handler(Looper.getMainLooper()).post {

				when (data.type) {
					EventType.DIR_CHANGE -> onDirectoryChange(State.currentDirectory)
					EventType.SELECT_MODE_ADD,
					EventType.SELECT_MODE_SUB,
					EventType.SELECT_MODE_APPEND,
					EventType.SELECT_MODE_INACTIVE -> onSelectModeChange()
				}
			}
		}
	}

	private fun onDirectoryChange(currentDirectory: File) {
		breadcrumbAdapter.update(currentDirectory)
		recyclerViewBreadcrumbs.scrollToPosition(currentDirectory.absolutePath.split("/").size - 2)

		// if currently at the root, animate to the root icon
		if (currentDirectory.absolutePath == FileMetadata.ROOT) animateBackButton(false)
		// if not at the root AND not displaying the back icon, animate to it
		else if (currentDirectory.absolutePath != FileMetadata.ROOT && currentDirectory.parent == FileMetadata.ROOT) animateBackButton(true)
	}

	private fun onSelectModeChange() {
		val isCurrentlyActive = binding.breadcrumbBarContainer.translationY != 0F
		val selectionCount = State.selectedTracks.count()
		val isActive = selectionCount > 0

		// only play the animations when changing states
		if (isActive && !isCurrentlyActive) {
			binding.breadcrumbBarContainer.slideY(-10F, 150L, 50L)
			binding.breadcrumbBarContainer.fadeOut(150L, 50L)

			binding.multiSelectBarContainer.slideY(0F, 200L)
			binding.multiSelectBarContainer.fadeIn(200L)

		} else if (!isActive && isCurrentlyActive) {
			binding.breadcrumbBarContainer.slideY(0F, 200L)
			binding.breadcrumbBarContainer.fadeIn(200L)

			binding.multiSelectBarContainer.slideY(10F, 150L, 50L)
			binding.multiSelectBarContainer.fadeOut(150L, 50L)
		}

		textViewSelectionCount.setText(binding.resources.getQuantityString(R.plurals.selectedCount, selectionCount, selectionCount))
	}

	// forward means that we're going deeper into the directory hierarchy
	private fun animateBackButton(forward: Boolean) {
		buttonBack.animateDrawable(if (forward) R.drawable.anim_root_back else R.drawable.anim_back_root)
	}
}

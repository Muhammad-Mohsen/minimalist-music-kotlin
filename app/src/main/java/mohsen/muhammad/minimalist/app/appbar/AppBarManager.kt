package mohsen.muhammad.minimalist.app.appbar

import android.content.Context
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import mohsen.muhammad.minimalist.R
import mohsen.muhammad.minimalist.core.Moirai
import mohsen.muhammad.minimalist.core.OnListItemClickListener
import mohsen.muhammad.minimalist.core.OnTextChangeListener
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.core.ext.*
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.State
import mohsen.muhammad.minimalist.data.SystemEvent
import mohsen.muhammad.minimalist.data.files.ExplorerFile
import mohsen.muhammad.minimalist.databinding.MainFragmentBinding
import java.io.File


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Controls layout properties (scroll position, back button) for the breadcrumb bar layout
 */

class AppBarManager(mainBinding: MainFragmentBinding) : EventBus.Subscriber, OnListItemClickListener<File> {

	private val binding = mainBinding.layoutBreadcrumbs

	private val breadcrumbAdapter: BreadcrumbAdapter
		get() = binding.recyclerViewBreadcrumbs.adapter as BreadcrumbAdapter

	private val inputMethodManager = binding.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

	fun initialize() {
		// event bus subscription
		EventBus.subscribe(this)

		val currentDirectory = State.currentDirectory

		val breadcrumbAdapter = BreadcrumbAdapter(currentDirectory, this)
		binding.recyclerViewBreadcrumbs.adapter = breadcrumbAdapter

		// set back button icon and tag
		val atRoot = ExplorerFile.isAtRoot(currentDirectory.absolutePath)
		val animationResourceId = if (atRoot) R.drawable.anim_root_back else R.drawable.anim_back_root
		binding.buttonBack.setImageDrawable(ContextCompat.getDrawable(binding.buttonBack.context, animationResourceId))
		binding.buttonBack.tag = if (atRoot) R.drawable.anim_back_root else R.drawable.anim_root_back // notice it's inverted!

		// scroll to end
		binding.recyclerViewBreadcrumbs.scrollToPosition(currentDirectory.absolutePath.split("/").size - 2)

		// back button listener
		binding.buttonBack.setOnClickListener {

			if (State.currentDirectory.absolutePath != ExplorerFile.ACTUAL_ROOT) {
				val dir = State.currentDirectory.parentFile ?: return@setOnClickListener

				State.currentDirectory = dir
				onDirectoryChange(dir) // repopulate the breadcrumb bar
				EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.DIR_CHANGE))
			}
		}

		// cancel edit mode listener
		binding.buttonCancel.setOnClickListener {
			State.selectedTracks.clear() // update the state
			State.isSearchModeActive = false

			toggleEditMode(false)
			EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.SELECT_MODE_INACTIVE))
		}
		// set the add to playlist button listener
		binding.buttonAddSelection.setOnClickListener {
			// update state
			State.playlist.updateItems(State.selectedTracks, true)
			State.selectedTracks.clear()
			toggleEditMode(false)
			EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.SELECT_MODE_INACTIVE))
		}
		// set the play playlist button listener
		binding.buttonPlaySelected.setOnClickListener {
			// update state
			State.playlist.updateItems(State.selectedTracks)
			State.selectedTracks.clear()

			EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.PLAY_SELECTED))
			toggleEditMode(false)
		}

		binding.editTextQuery.addTextChangedListener(object : OnTextChangeListener {
			override fun afterTextChanged(s: Editable?) {
				EventBus.send(SystemEvent(EventSource.BREADCRUMB, EventType.SEARCH_QUERY, s.toString()))
			}
		})
	}

	override fun receive(data: EventBus.EventData) {
		if (data is SystemEvent && data.source != EventSource.BREADCRUMB) {
			Moirai.MAIN.post {

				when (data.type) {
					EventType.DIR_CHANGE -> onDirectoryChange(State.currentDirectory)

					EventType.SELECT_MODE_ADD,
					EventType.SELECT_MODE_SUB,
					EventType.SELECT_MODE_INACTIVE,
					EventType.SEARCH_MODE -> {
						toggleEditMode(State.isSelectModeActive || State.isSearchModeActive)
						toggleSelectMode()
						toggleSearchMode()
					}
				}
			}
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

	private fun onDirectoryChange(currentDirectory: File) {
		breadcrumbAdapter.update(currentDirectory)
		binding.recyclerViewBreadcrumbs.scrollToPosition(currentDirectory.absolutePath.split("/").size - 2)
		animateBackButton(!ExplorerFile.isAtRoot(currentDirectory.absolutePath)) // animate the back button icon depending on the current directory
	}

	private fun toggleSearchMode() {
		if (State.isSearchModeActive) {
			binding.editTextQuery.fadeIn(200L)
			binding.textViewSelectionCount.setText(binding.resources.getString(R.string.selectedCount, State.selectedTracks.count()))

			binding.editTextQuery.requestFocus()
			inputMethodManager?.showSoftInput(binding.editTextQuery, InputMethodManager.SHOW_IMPLICIT)

		} else {
			binding.editTextQuery.setText(String.EMPTY)
			binding.editTextQuery.fadeOut(200L)
			inputMethodManager?.hideSoftInputFromWindow(binding.editTextQuery.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
		}
	}
	private fun toggleSelectMode() {
		arrayOf(binding.textViewSelectionCount, binding.buttonAddSelection, binding.buttonPlaySelected).forEach { v: View ->
			if (State.isSelectModeActive) v.fadeIn(200L)
			else v.fadeOut(200L)
		}

		val selectionCount = State.selectedTracks.count()
		if (State.isSearchModeActive) binding.textViewSelectionCount.setText(binding.resources.getString(R.string.selectedCount, State.selectedTracks.count()))
		else binding.textViewSelectionCount.setText(binding.resources.getQuantityString(R.plurals.selectedCount, selectionCount, selectionCount))
	}
	private fun toggleEditMode(force: Boolean) {
		val isCurrentlyActive = binding.breadcrumbBarContainer.alpha != 1F

		if (force && !isCurrentlyActive) {
			binding.breadcrumbBarContainer.fadeOut(200L)
			binding.breadcrumbBarContainer.animateLayoutMargins(R.dimen.spacingZero, R.dimen.spacingLarge, 200L)
			binding.multiSelectBarContainer.fadeIn(200L)
			binding.multiSelectBarContainer.animateLayoutMargins(R.dimen.spacingZero, 200L)

		} else if (!force && isCurrentlyActive) {
			binding.breadcrumbBarContainer.fadeIn(200L)
			binding.breadcrumbBarContainer.animateLayoutMargins(R.dimen.spacingLarge, 200L)
			binding.multiSelectBarContainer.fadeOut(200L)
			binding.multiSelectBarContainer.animateLayoutMargins(R.dimen.spacingLarge, R.dimen.spacingZero, 200L)
		}

		if (!force) toggleSearchMode() // to clear the filter, dismiss the IME, etc.
	}

	// forward means that we're going deeper into the directory hierarchy
	private fun animateBackButton(forward: Boolean) {
		val anim = if (forward) R.drawable.anim_root_back else R.drawable.anim_back_root
		if (binding.buttonBack.tag == anim) return

		binding.buttonBack.tag = anim
		binding.buttonBack.animateDrawable(anim)
	}
}

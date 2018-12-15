package mohsen.muhammad.minimalist.core

/**
 * Created by muhammad.mohsen on 12/15/2018.
 * generic interfaces which relays list item interactions (clicks, long clicks) to the containing context class (in most cases, a fragment)
 */
interface OnListItemClickListener<in T> {
	fun onListItemClick(data: T?, source: Int)
}

interface OnListItemLongClickListener<in T> {
	fun onListItemLongClick(data: T?, source: Int)
}

// combines the above interfaces into a single one
interface OnListItemInteractionListener<in T> : OnListItemClickListener<T>, OnListItemLongClickListener<T>
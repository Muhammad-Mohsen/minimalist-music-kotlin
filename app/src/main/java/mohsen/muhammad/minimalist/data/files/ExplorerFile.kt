package mohsen.muhammad.minimalist.data.files

import mohsen.muhammad.minimalist.core.ext.EMPTY
import java.io.File


/**
 * Created by muhammad.mohsen on 4/15/2017.
 * Holds a file's metadata.
 * Offers no distinction between a music file and a directory...just like java
 * Metadata is obtained asynchronously via the MetadataAsyncTask (no longer the case)
 */

class ExplorerFile(pathname: String) : File(pathname) {

	var album: String = String.EMPTY
	var artist: String = String.EMPTY
	var duration: String = String.EMPTY

	var trackCount: Int = 0
}

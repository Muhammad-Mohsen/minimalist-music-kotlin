package mohsen.muhammad.minimalist.data.files

import android.os.AsyncTask
import mohsen.muhammad.minimalist.core.EMPTY
import mohsen.muhammad.minimalist.core.FileHelper
import mohsen.muhammad.minimalist.data.Prefs


/**
 * Created by muhammad.mohsen on 4/16/2017.
 * gets a file metadata asynchronously and updates the UI.
 * obtaining metadata is dog slow. Also, notifyItemChanged has to be called somewhere.
 */

class MetadataAsyncTask : AsyncTask<ArrayList<ExplorerFile>, Void, ArrayList<ExplorerFile>>() {

    override fun doInBackground(vararg params: ArrayList<ExplorerFile>): ArrayList<ExplorerFile> {
        for (model in params[0]) {
            val helper = FileHelper(model)

            if (!model.isDirectory) {
                model.artist = helper.artist
                model.album = helper.album
                model.duration = helper.duration ?: String.EMPTY

            } else {
                model.trackCount = helper.trackCount
            }
        }

        return params[0]
    }

    override fun onPostExecute(explorerFiles: ArrayList<ExplorerFile>?) {
        // call notifyItemRangeChanged if the current directory is the same, and the adapter is not null.
        var isOnCurrentDirectory = false
        if (explorerFiles != null && explorerFiles.size > 0)
            isOnCurrentDirectory =
                    Prefs.currentDirectory.absolutePath == explorerFiles[0].parentFile.absolutePath

        // val adapter = ExplorerManager.explorerAdapter
        // if (adapter != null && isOnCurrentDirectory) adapter.notifyDataSetChanged() // until I implement an item animator, there won't be change animations!
    }
}

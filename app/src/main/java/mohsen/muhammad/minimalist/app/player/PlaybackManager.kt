package mohsen.muhammad.minimalist.app.player

import mohsen.muhammad.minimalist.data.files.ExplorerFile
import mohsen.muhammad.minimalist.app.main.MainFragment


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * In the same vein as ExplorerManager, and BreadcrumbManager, manages playback.
 * Has a hold of both the play service and the main fragment.
 * initialize, terminate methods cannot used here as the service and the fragment aren't necessarily created/destroyed together.
 */

object PlaybackManager {

    var playerService: PlayerService? = null
        private set

        var mainFragment: MainFragment? = null

    fun setPlaybackService(service: PlayerService) {
        playerService = service
    }

    // when the play/pause button is clicked
    fun playPause(play: Boolean) {
        playerService!!.playPause(play)
    }

    fun startPlay(track: ExplorerFile) {

    }

}
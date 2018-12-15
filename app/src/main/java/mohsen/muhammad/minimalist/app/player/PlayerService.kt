package mohsen.muhammad.minimalist.app.player

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * Background service that's actually responsible for playing the music
 */

class PlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    private var mPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // initialize the media player
        initializeMediaPlayer()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        // destroy the Player instance
    }

    fun playPause(play: Boolean) {
        if (play)
            mPlayer!!.start()
        else
            mPlayer!!.pause()
    }

    override fun onAudioFocusChange(focusChange: Int) {

    }

    override fun onCompletion(mp: MediaPlayer) {
        // TODO ask the PlaybackManager to play the next track
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        // TODO start playing?
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        // TODO don't know yet!
    }

    private fun initializeMediaPlayer() {
        mPlayer = MediaPlayer()

        //Set up MediaPlayer event listeners
        mPlayer!!.setOnCompletionListener(this)
        mPlayer!!.setOnErrorListener(this)
        mPlayer!!.setOnPreparedListener(this)
        mPlayer!!.setOnSeekCompleteListener(this)

        //Reset so that the MediaPlayer is not pointing to another data source
        mPlayer!!.reset()
    }

    // override is mandated by the framework
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}

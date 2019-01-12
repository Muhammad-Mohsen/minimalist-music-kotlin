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

	private var player: MediaPlayer? = null
	private var playlist: Playlist? = null

	// API
	fun playPause(play: Boolean) {
		if (play) player?.start()
		else player?.pause()
	}

	fun playTrack(path: String) {

		// update playlist
		playlist = Playlist(path)
		playlist?.setTrack(path, true)

		// play the track
		player?.setDataSource(path)
		player?.prepareAsync()

		// TODO inform UI
	}

	// updates the attributes (shuffle/repeat mode) of the playlist
	fun updatePlaylistAttr() {

	}

	fun updateSeek() {

	}

	// events
	override fun onAudioFocusChange(focusChange: Int) {
		if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			player?.pause()

			// TODO inform UI
		}
	}

	override fun onCompletion(mp: MediaPlayer) {
		val nextTrack = playlist?.getNextTrack(true)
		if (nextTrack != null) {
			player?.setDataSource(nextTrack)
			player?.prepareAsync()
		}

		// TODO inform UI
	}

	override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
		return false
	}

	override fun onPrepared(mp: MediaPlayer) {
		player?.start()
	}

	override fun onSeekComplete(mp: MediaPlayer) {
		// TODO don't know yet!
	}

	private fun initializeMediaPlayer() {
		player = MediaPlayer()

		//Set up MediaPlayer event listeners
		player?.setOnCompletionListener(this)
		player?.setOnErrorListener(this)
		player?.setOnPreparedListener(this)
		player?.setOnSeekCompleteListener(this)
	}

	// life cycle
	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		// initialize the media player
		initializeMediaPlayer()

		// register the service instance
		registerSelf(this)

		return super.onStartCommand(intent, flags, startId)
	}
	override fun onDestroy() {
		player?.release() // destroy the Player instance
	}

	// override is mandated by the framework
	override fun onBind(intent: Intent): IBinder? { return null }

	companion object {

		// Oh, look! It's accessible!
		// TODO make this an interface reference and it'll be a lot prettier
		var instance: PlayerService? = null

		private fun registerSelf(serviceInstance: PlayerService) {
			instance = serviceInstance
		}
	}
}

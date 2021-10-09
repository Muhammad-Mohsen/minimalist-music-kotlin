package mohsen.muhammad.minimalist.app.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

/**
 * Created by muhammad.mohsen on 4/7/2019.
 * Handles audio focus requests, it is fucking infuriating!!
 * Thanks #google
 * https://developer.android.com/guide/topics/media-apps/audio-focus
 */

@Suppress("DEPRECATION")
class AudioFocusHandler(private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener, context: Context) {

	private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
	private val focusRequest = createFocusRequest(audioFocusChangeListener)

	// request audio focus
	fun request(): Int {
		val focusRequest = createFocusRequest(audioFocusChangeListener)
		return audioManager.requestAudioFocus(focusRequest!!)
	}

	// abandon audio focus
	fun abandon(): Int {
		return audioManager.abandonAudioFocusRequest(focusRequest!!)
	}

	// creates a "FocusRequest" for Oreo and above
	private fun createFocusRequest(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener): AudioFocusRequest? {
		return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {

			setAudioAttributes(AudioAttributes.Builder().run {
				setUsage(AudioAttributes.USAGE_MEDIA)
				setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				build()
			})

			setOnAudioFocusChangeListener(audioFocusChangeListener)
			setAcceptsDelayedFocusGain(false)
			build()
		}
	}
}

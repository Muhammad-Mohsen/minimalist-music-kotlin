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

	fun request(): Int {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val focusRequest = createFocusRequest(audioFocusChangeListener)
			audioManager.requestAudioFocus(focusRequest!!)

		} else audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
	}

	fun abandon(): Int {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			audioManager.abandonAudioFocusRequest(focusRequest!!)

		} else audioManager.abandonAudioFocus(audioFocusChangeListener)
	}

	private fun createFocusRequest(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener): AudioFocusRequest? {

		return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) null
		else AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {

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
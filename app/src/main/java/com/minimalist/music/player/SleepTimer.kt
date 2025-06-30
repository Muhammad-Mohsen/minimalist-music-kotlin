package com.minimalist.music.player

import android.os.CountDownTimer
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Target
import com.minimalist.music.foundation.EventBus.Type

object SleepTimer {

	private var timer: CountDownTimer? = null

	fun start(delay: Int) {
		timer = object : CountDownTimer(delay * 60 * 1000L, 1000) {
			override fun onTick(millisUntilFinished: Long) {
				EventBus.dispatch(Event(Type.SLEEP_TIMER_TICK, Target.SESSION, mapOf("tick" to millisUntilFinished / 1000)))
			}

			override fun onFinish() {
				EventBus.dispatch(Event(Type.SLEEP_TIMER_FINISH, Target.SESSION))
			}
		}

		timer?.start()
	}

	fun cancel() {
		timer?.cancel()
	}
}
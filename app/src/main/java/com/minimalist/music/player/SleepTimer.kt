package com.minimalist.music.player

import android.os.CountDownTimer
import com.minimalist.music.foundation.EventBus
import com.minimalist.music.foundation.EventBus.Event
import com.minimalist.music.foundation.EventBus.Target
import com.minimalist.music.foundation.EventBus.Type

object SleepTimer {

	private var timer: CountDownTimer? = null

	fun start(delay: Long) {
		timer = object : CountDownTimer(delay, 1000) {
			override fun onTick(millisUntilFinished: Long) {
				EventBus.dispatch(Event(Type.SLEEP_TIMER_TICK, Target.SERVICE, mapOf("tick" to millisUntilFinished)))
			}

			override fun onFinish() {
				EventBus.dispatch(Event(Type.SLEEP_TIMER_FINISH, Target.SERVICE))
			}
		}

		timer?.start()
	}

	fun cancel() {
		timer?.cancel()
	}
}

package mohsen.muhammad.minimalist.app.settings

import android.os.CountDownTimer
import mohsen.muhammad.minimalist.core.evt.EventBus
import mohsen.muhammad.minimalist.data.EventSource
import mohsen.muhammad.minimalist.data.EventType
import mohsen.muhammad.minimalist.data.SystemEvent

object SleepTimer {

	private var timer: CountDownTimer? = null

	fun start(delay: Int) {
		timer = object : CountDownTimer(delay * 60 * 1000L, 1000) {
			override fun onTick(millisUntilFinished: Long) {
				EventBus.send(SystemEvent(EventSource.SESSION, EventType.SLEEP_TIMER_TICK, (millisUntilFinished / 1000).toInt().toString()))
			}

			override fun onFinish() {
				EventBus.send(SystemEvent(EventSource.SESSION, EventType.SLEEP_TIMER_FINISH))
			}
		}

		timer?.start()
	}

	fun cancel() {
		timer?.cancel()
	}
}
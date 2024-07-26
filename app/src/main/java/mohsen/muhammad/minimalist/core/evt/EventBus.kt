package mohsen.muhammad.minimalist.core.evt

import java.lang.ref.WeakReference

/**
 * Created by muhammad.mohsen on 2/10/2019.
 * Inspired by the EventBus library, this is designed to send messages to the app components (service/UI/notification)
 * that need to be updated when various events occur
 */
object EventBus {

	private val subscribers = ArrayList<WeakReference<Subscriber>>()

	fun subscribe(subscriber: Subscriber) {
		val ref = WeakReference(subscriber)
		subscribers.add(ref)
	}
	fun unsubscribe(subscriber: Subscriber) {
		val ref = subscribers.firstOrNull {ref -> ref.get() == subscriber }
		subscribers.remove(ref)
	}

	fun send(data: EventData) {
		subscribers.forEach { sub -> sub.get()?.receive(data) }
	}

	interface Subscriber {
		fun receive(data: EventData)
	}

	// event args base class
	abstract class EventData
}
package mohsen.muhammad.minimalist.core

import android.os.HandlerThread

/**
 * Created by muhammad.mohsen on 1/26/2024.
 * the name is from bard :)  Ancient Greek mythology, the Moirai were the three goddesses of fate and destiny. They were known for spinning, measuring, and cutting the threads of life
 * threads...get it? threads?
 *
 * coroutines, nay!!
 * I'm using this instead of coroutines because coroutines were stupid slow when I tried them
 *
 * Also, kotlin doesn't support static extensions on classes that don't have a companion...So I couldn't extend the HandlerThread class, unfortunately
 */
object Moirai {

	val BG = HandlerThread("bg").apply {
		start()
	}
}

package mohsen.muhammad.minimalist.data

import android.view.ViewConfiguration
import androidx.core.view.animation.PathInterpolatorCompat

/**
 * Created by muhammad.mohsen on 11/3/2018.
 * just general constants
 */

object Const {
	const val MINIMALIST_SHARED_PREFERENCES = "Minimalist"
	const val PREV_THRESHOLD = 5000L

	const val PRIVACY_POLICY_URL = "https://muhammad-mohsen.github.io/minimalist-music-kotlin/"

	val exponentialInterpolator = PathInterpolatorCompat.create(.19F, 1F, .22F, 1F)
}

// defines the explorer recycler view adapter view types
// also used to determine the interaction (click, long click) source
object ItemType {
	const val DIRECTORY = 0
	const val TRACK = 1
	const val CRUMB = 2
}

object FabMenu {
	val DELAY = ViewConfiguration.getTapTimeout().toLong() * 2
	const val DURATION = 200L

	const val ANGLE_BRACKET_ARC =35F
	const val ANGLE_OFFSET = 5F
	const val GESTURE_MIN_DISTANCE = 230

	const val BUTTON_NEXT = 0
	const val BUTTON_REPEAT = 1
	const val BUTTON_SHUFFLE = 2
	const val BUTTON_PREV = 3
}

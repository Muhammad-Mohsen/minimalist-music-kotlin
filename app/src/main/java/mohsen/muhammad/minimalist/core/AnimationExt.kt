package mohsen.muhammad.minimalist.core

import android.graphics.drawable.AnimationDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * animation extensions
 */

fun ImageView.animateDrawable(drawableResourceId: Int) {
    val drawable = ContextCompat.getDrawable(this.context, drawableResourceId) // get the frame animation drawable

    (this.drawable as AnimationDrawable).stop() // stop any animation that might be running
	this.setImageDrawable(drawable)
    (this.drawable as AnimationDrawable).start()

    // set the drawable ID as the tag, so we know the final state of a given view
    // (e.g. to know that the back/root button is currently displaying the root icon)
	this.tag = drawableResourceId
}

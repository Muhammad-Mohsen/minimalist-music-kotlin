package mohsen.muhammad.minimalist.app.explorer

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView


/**
 * Created by muhammad.mohsen on 11/3/2018.
 * custom item animator for the Explorer recycler view
 * TODO :D
 */

class ExplorerItemAnimator : DefaultItemAnimator() {

    override fun animateDisappearance(
        viewHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo, postLayoutInfo: ItemHolderInfo?
    ): Boolean {
        return false
    }

    override fun animateAppearance(
        viewHolder: RecyclerView.ViewHolder, preLayoutInfo: ItemHolderInfo?,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        return if (preLayoutInfo != null && (preLayoutInfo.left != postLayoutInfo.left || preLayoutInfo.top != postLayoutInfo.top)) {
            animateMove(viewHolder, preLayoutInfo.left, preLayoutInfo.top, postLayoutInfo.left, postLayoutInfo.top)

        } else {
            animateAdd(viewHolder)
        }
    }

    override fun animatePersistence(
        viewHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        return false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preLayoutInfo: ItemHolderInfo,
        postLayoutInfo: ItemHolderInfo
    ): Boolean {
        return false
    }
}
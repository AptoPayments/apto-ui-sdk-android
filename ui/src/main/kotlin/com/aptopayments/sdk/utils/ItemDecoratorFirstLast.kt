package com.aptopayments.sdk.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecoratorFirstLast(private val offsetPixelSize: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, getTopPixelOffset(parent, view), 0, getBottomPixelOffset(parent, view))
    }

    private fun getTopPixelOffset(parent: RecyclerView, view: View) =
        if (isFirstElement(parent, view)) offsetPixelSize else 0

    private fun getBottomPixelOffset(parent: RecyclerView, view: View) =
        if (isLastElement(parent, view)) offsetPixelSize else 0

    private fun isLastElement(parent: RecyclerView, view: View) =
        parent.getChildAdapterPosition(view) == ((parent.adapter?.itemCount ?: 0) - 1)

    private fun isFirstElement(parent: RecyclerView, view: View) =
        parent.getChildAdapterPosition(view) == 0
}

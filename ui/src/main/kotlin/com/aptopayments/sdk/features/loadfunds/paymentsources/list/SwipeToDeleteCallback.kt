package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs

internal class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background: ColorDrawable = ColorDrawable(UIConfig.uiErrorColor)
    private var text: Paint = Paint()
    private val deleteText = "load_funds_payment_methods_delete_row_cta".localized()

    private val _event = MutableStateFlow(SwipedElement(position = -1, item = null))
    val event = _event as StateFlow<SwipedElement>

    init {
        text.color = UIConfig.textMessageColor
        text.textAlign = Paint.Align.LEFT
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView

        when {
            dX < 0 -> { // Swiping to the left
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top, itemView.right, itemView.bottom
                )
            }
            else -> // view is unSwiped
                background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        drawText(c, itemView, dX)
    }

    private val r: Rect = Rect()

    private fun drawText(canvas: Canvas, itemView: View, dX: Float) {
        canvas.getClipBounds(r)
        val containerWith: Int = r.width()

        text.textSize = itemView.context.resources.getDimension(R.dimen.text_size_medium_small)
        text.getTextBounds(deleteText, 0, deleteText.length, r)
        val xMargin = itemView.context.resources.getDimension(R.dimen.llsdk_medium_margin)

        val textWithMargins = r.width() + 2 * xMargin

        val x: Float = if (abs(dX) < textWithMargins) {
            containerWith - r.width() - xMargin
        } else {
            val xPos = containerWith - abs(dX)
            xPos + ((abs(dX) - r.width()) / 2)
        }

        val y: Float = (itemView.top + ((itemView.height + r.height()) / 2)).toFloat()
        canvas.drawText(deleteText, x, y, text)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is PaymentSourcesListAdapter.ExistingPaymentSourceViewHolder && viewHolder.item != null) {
            _event.value = SwipedElement(viewHolder.adapterPosition, viewHolder.item)
        }
    }

    override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (viewHolder is PaymentSourcesListAdapter.ExistingPaymentSourceViewHolder && viewHolder.item?.elem?.isPreferred == false) {
            ItemTouchHelper.LEFT
        } else {
            0
        }
    }

    class SwipedElement(val position: Int, val item: PaymentSourcesListItem?)
}

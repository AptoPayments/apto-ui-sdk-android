package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.aptopayments.sdk.R
import com.google.android.material.textfield.TextInputEditText

class PrefixEditText(context: Context, attributes: AttributeSet?) :
    TextInputEditText(context, attributes) {

    private var mOriginalLeftPadding: Float = -1f
    private var prefixValue = ""

    init {
        context.obtainStyledAttributes(attributes, R.styleable.PrefixEditText).apply {
            getString(R.styleable.PrefixEditText_prefixEdit)?.let { setPrefixEdit(it) }
        }.recycle()
    }

    fun setPrefixEdit(value: String) {
        prefixValue = value
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculatePrefix()
    }

    private fun calculatePrefix() {
        if (mOriginalLeftPadding == -1f && prefixValue.isNotEmpty()) {
            val widths = FloatArray(prefixValue.length)
            paint.getTextWidths(prefixValue, widths)
            var textWidth = 0f
            for (w in widths) {
                textWidth += w
            }
            mOriginalLeftPadding = compoundPaddingLeft.toFloat()
            setPadding(
                (textWidth + mOriginalLeftPadding).toInt(),
                paddingRight, paddingTop,
                paddingBottom
            )
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (prefixValue.isNotEmpty())
            canvas?.drawText(
                prefixValue, mOriginalLeftPadding,
                getLineBounds(0, null).toFloat(), paint
            )
    }
}

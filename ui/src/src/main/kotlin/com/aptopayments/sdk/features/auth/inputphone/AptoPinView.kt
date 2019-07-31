package com.aptopayments.sdk.features.auth.inputphone

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
private const val NUMBER_OF_DIGITS = 6

class AptoPinView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0):
        AppCompatEditText(
                context,
                attrs,
                defStyleAttr
        ) {

    private var mNumChars = 6

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        mNumChars = attrs?.getAttributeIntValue(
                XML_NAMESPACE_ANDROID, "maxLength", NUMBER_OF_DIGITS) ?: 6
    }

    override fun onDraw(canvas: Canvas) {
        val availableWidth = width - paddingRight - paddingLeft
        val charWidth = availableWidth / (mNumChars * 2f - 1)
        val semiCharWidth = charWidth / 2
        val charHeight = charHeight()
        val semiCharHeight = charHeight / 2
        val centerY = height / 2f
        val text = text
        var currentX = ((width - availableWidth) / 2f)
        val textLength = text?.length ?: 0
        for (i in 0 until textLength) {
            canvas.drawText(text, i, i + 1, currentX - semiCharWidth, centerY - semiCharHeight, paint)
            currentX += (charWidth * 2)
        }
        for (i in textLength until mNumChars) {
            canvas.drawLine(currentX - semiCharWidth, centerY, currentX + semiCharWidth, centerY, paint)
            currentX += (charWidth * 2)
        }
    }

    override fun setBackgroundColor(color: Int) {
        val background = background
        background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        this.background = background
    }

    private fun charHeight(): Float {
        val fm = paint.fontMetrics
        return fm.descent + fm.ascent
    }
}

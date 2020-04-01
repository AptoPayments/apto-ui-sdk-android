package com.aptopayments.sdk.features.auth.inputphone

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.core.extension.starsExceptLast
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat

private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
private const val DEFAULT_NUMBER_OF_DIGITS = 6
private const val DEFAULT_IS_PASSWORD = false

class AptoPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var mNumChars = DEFAULT_NUMBER_OF_DIGITS
    private var isPassword = DEFAULT_IS_PASSWORD

    init {
        getAttributes(attrs)
        setTextColor(UIConfig.textPrimaryColor)
    }

    private fun getAttributes(attrs: AttributeSet?) {
        attrs?.let {
            mNumChars = it.getAttributeIntValue(XML_NAMESPACE_ANDROID, "maxLength", DEFAULT_NUMBER_OF_DIGITS)
            isPassword = isInputTypePassword(it)
        }
    }

    private fun isInputTypePassword(it: AttributeSet): Boolean {
        val variation = getAttributeInputType(it)
        val passwordInputType = (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
        val webPasswordInputType =
            (variation == EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
        val numberPasswordInputType =
            (variation == EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD)

        return passwordInputType || webPasswordInputType || numberPasswordInputType
    }

    private fun getAttributeInputType(it: AttributeSet) =
        it.getAttributeIntValue(XML_NAMESPACE_ANDROID, "inputType", EditorInfo.TYPE_NULL)

    override fun onDraw(canvas: Canvas) {
        val availableWidth = width - paddingRight - paddingLeft
        val charWidth = availableWidth / (mNumChars * 2f - 1)
        val semiCharWidth = charWidth / 2
        val charHeight = charHeight()
        val semiCharHeight = charHeight / 2
        val centerY = height / 2f
        val text = text?.toString() ?: ""
        var currentX = ((width - availableWidth) / 2f)
        val textLength = text.length
        paint.color = textColors.defaultColor
        for (i in 0 until textLength) {
            canvas.drawText(getText(text), i, i + 1, currentX - semiCharWidth, centerY - semiCharHeight, paint)
            currentX += (charWidth * 2)
        }
        for (i in textLength until mNumChars) {
            canvas.drawLine(currentX - semiCharWidth, centerY, currentX + semiCharWidth, centerY, paint)
            currentX += (charWidth * 2)
        }
    }

    private fun getText(text: String) = if (isPassword) text.starsExceptLast() else text

    override fun setBackgroundColor(color: Int) {
        val background = background
        background.setColorFilterCompat(color, PorterDuff.Mode.SRC_ATOP)
        this.background = background
    }

    private fun charHeight(): Float {
        val fm = paint.fontMetrics
        return fm.descent + fm.ascent
    }
}

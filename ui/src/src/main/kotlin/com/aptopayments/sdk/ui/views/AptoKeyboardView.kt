package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.*
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import androidx.core.content.ContextCompat
import com.aptopayments.sdk.R

@Suppress("DEPRECATION")
class AptoKeyboardView : KeyboardView {
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private lateinit var mKeyBoard: Keyboard
    private val textDpSize = 28f

    override fun onDraw(canvas: Canvas) {
        mKeyBoard = this.keyboard
        val keys: List<Keyboard.Key>? = mKeyBoard.keys

        if (keys != null) {
            for (key in keys) {
                drawKeyBackground(R.drawable.transparent_rounded_button, canvas, key)
                drawText(canvas, key)
            }
        }
    }

    private fun drawKeyBackground(drawableId: Int, canvas: Canvas, key: Keyboard.Key) {
        val npd = ContextCompat.getDrawable(context, drawableId)
        npd?.let {
            val drawableState = key.currentDrawableState
            if (key.codes[0] != 0) npd.state = drawableState
            npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
            npd.draw(canvas)
        }
    }

    private fun drawText(canvas: Canvas, key: Keyboard.Key) {
        val bounds = Rect()
        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        if (key.label != null) {
            val pixelSize = applyDimension(COMPLEX_UNIT_DIP, textDpSize, resources.displayMetrics)
            paint.textSize = pixelSize
            paint.typeface = Typeface.DEFAULT
            paint.getTextBounds(key.label.toString(), 0, key.label.toString().length, bounds)
            canvas.drawText(
                key.label.toString(), (key.x + (key.width / 2)).toFloat(),
                ((key.y + key.height / 2) + bounds.height() / 2).toFloat(), paint
            )
        } else if (key.icon != null) {
            key.icon.setBounds(
                key.x + (key.width - key.icon.intrinsicWidth) / 2,
                key.y + (key.height - key.icon.intrinsicHeight) / 2,
                key.x + (key.width - key.icon.intrinsicWidth) / 2 + key.icon.intrinsicWidth,
                key.y + (key.height - key.icon.intrinsicHeight) / 2 + key.icon.intrinsicHeight
            )
            key.icon.draw(canvas)
        }
    }
}

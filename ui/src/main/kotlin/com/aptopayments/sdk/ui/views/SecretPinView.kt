package com.aptopayments.sdk.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.invisibleIf
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.view_secret_pin.view.*

private const val DEFAULT_PIN_LENGTH = 4

internal class SecretPinView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private var length = 0
    private val pinImages = mutableListOf<ImageView>()
    private var currentPin = ""
        set(value) {
            field = value
            pin_btn_back.invisibleIf(value.isEmpty())
        }

    var delegate: Delegate? = null

    interface Delegate {
        fun onPinEntered(currentPin: String)
        fun onForgotPressed()
    }

    init {
        inflate(context, R.layout.view_secret_pin, this)
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.SecretPinView, 0, 0)
            configureForgot(typedArray)
            configureLength(typedArray)
            typedArray.recycle()
        }
        configureButtons()
        createPinView()
        pin_btn_back.hide()
    }

    fun clean() {
        currentPin = ""
        pinImages.forEach { changeImagePosition(it, false) }
    }

    private fun createPinView() {
        for (i in 1..length) {
            val image = createDotImage()
            addDotToView(image)
        }
    }

    private fun addDotToView(image: ImageView) {
        dots.addView(image, createLayoutParams())
        pinImages.add(image)
    }

    private fun createDotImage(): ImageView {
        val image = ImageView(context)
        image.setImageResource(R.drawable.ic_circle)
        tintImage(image, getTintColor(false))
        return image
    }

    private fun createLayoutParams(): LinearLayout.LayoutParams {
        val imParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        val margin = resources.getDimensionPixelSize(R.dimen._12sdp)
        imParams.setMargins(margin, margin, margin, margin)
        return imParams
    }

    private fun configureButtons() {
        configureClickAndColor(pin_btn_0, 0)
        configureClickAndColor(pin_btn_1, 1)
        configureClickAndColor(pin_btn_2, 2)
        configureClickAndColor(pin_btn_3, 3)
        configureClickAndColor(pin_btn_4, 4)
        configureClickAndColor(pin_btn_5, 5)
        configureClickAndColor(pin_btn_6, 6)
        configureClickAndColor(pin_btn_7, 7)
        configureClickAndColor(pin_btn_8, 8)
        configureClickAndColor(pin_btn_9, 9)
        pin_btn_back.setOnClickListener { removeDigit() }
        pin_btn_forgot.setOnClickListenerSafe { delegate?.onForgotPressed() }
    }

    private fun configureLength(typedArray: TypedArray) {
        length = typedArray.getInt(R.styleable.SecretPinView_length, DEFAULT_PIN_LENGTH)
    }

    fun showForgot(show: Boolean) {
        pin_btn_forgot.visibleIf(show)
    }

    private fun configureClickAndColor(pinBtn: TextView, value: Int) {
        pinBtn.setTextColor(UIConfig.textPrimaryColor)
        pinBtn.setOnClickListener { addDigit(value) }
    }

    private fun configureForgot(typedArray: TypedArray) {
        val showForgot = typedArray.getBoolean(R.styleable.SecretPinView_showForgot, false)
        pin_btn_forgot.visibleIf(showForgot)
    }

    private fun addDigit(value: Int) {
        currentPin += value.toString()
        pin_btn_back.show()
        changeImagePosition(pinImages[currentPin.length - 1], true)
        checkIfPinEntered()
    }

    private fun checkIfPinEntered() {
        if (currentPin.length == length) {
            delegate?.onPinEntered(currentPin)
        }
    }

    private fun changeImagePosition(imageView: ImageView, active: Boolean) {
        tintImage(imageView, getTintColor(active))
    }

    private fun tintImage(imageView: ImageView, color: Int) {
        imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    private fun getTintColor(active: Boolean) = if (active) UIConfig.uiPrimaryColor else UIConfig.iconPrimaryColor

    private fun removeDigit() {
        changeImagePosition(pinImages[currentPin.length - 1], false)
        currentPin = currentPin.dropLast(1)
    }
}

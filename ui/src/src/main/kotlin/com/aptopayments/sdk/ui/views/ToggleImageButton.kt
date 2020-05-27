package com.aptopayments.sdk.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.ImageButton
import com.aptopayments.sdk.R

/*
 * Taken from here:
 * https://stackoverflow.com/a/15247353/1411844
 */
class ToggleImageButton : ImageButton, Checkable {
    var onCheckedChangeListener: OnCheckedChangeListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setChecked(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setChecked(attrs)
    }

    override fun isChecked(): Boolean = isSelected

    @SuppressLint("CustomViewStyleable")
    private fun setChecked(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.toggle_image_button)
        isChecked = a.getBoolean(R.styleable.toggle_image_button_android_checked, false)
        a.recycle()
    }

    override fun setChecked(checked: Boolean) {
        isSelected = checked

        if (onCheckedChangeListener != null) {
            onCheckedChangeListener!!.onCheckedChanged(this, checked)
        }
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(buttonView: ToggleImageButton, isChecked: Boolean)
    }
}

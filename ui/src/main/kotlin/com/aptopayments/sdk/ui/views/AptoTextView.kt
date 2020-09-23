package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R

class AptoTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {

    var localizedText: String = ""
        set(value) = localizeAndSet(value)

    init {
        attrs?.let { attributeSet ->
            val typedArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.AptoTextView, 0, 0)

            typedArray.getString(R.styleable.AptoTextView_localize)?.let {
                localizeAndSet(it)
            }
            typedArray.recycle()
        }
    }

    private fun localizeAndSet(value: String) {
        text = if (!isInEditMode) {
            value.localized()
        } else {
            getEditModeStringResource(value)
        }
    }

    private fun getEditModeStringResource(localize: String): String {
        return try {
            val packageName = this.context.packageName
            val resId = resources.getIdentifier(localize, "string", packageName)
            this.context.getString(resId)
        } catch (ex: Exception) {
            ""
        }
    }
}

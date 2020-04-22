package com.aptopayments.sdk.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.ui.views.AptoTextView

object DataBindingUtils {

    @JvmStatic
    @BindingAdapter("visibileIf")
    fun visibleIf(view: View, value: Boolean) {
        view.visibleIf(value)
    }

    @JvmStatic
    @BindingAdapter("loadFromUrl")
    fun loadFromUrl(view: ImageView, value: String?) {
        if (value?.isNotEmpty() == true) {
            view.loadFromUrl(value)
        } else {
            view.setImageResource(0)
        }
    }

    @JvmStatic
    @BindingAdapter("localizeBinding")
    fun localizeBinding(textView : AptoTextView, value : String?){
        textView.localizedText = value ?: ""
    }

}

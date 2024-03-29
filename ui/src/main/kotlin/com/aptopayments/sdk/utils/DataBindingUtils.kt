package com.aptopayments.sdk.utils

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.sdk.core.extension.goneIf
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.ui.views.AptoTextView
import com.aptopayments.sdk.ui.views.CardShippingStatusView
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe

object DataBindingUtils {

    @JvmStatic
    @BindingAdapter("visibleIf")
    fun visibleIf(view: View, bool: Boolean) = view.visibleIf(bool)

    @JvmStatic
    @BindingAdapter("goneIf")
    fun goneIf(view: View, bool: Boolean) = view.goneIf(bool)

    @BindingAdapter("requestFocus")
    @JvmStatic
    fun requestFocus(editText: EditText, requestFocus: Boolean?) {
        if (requestFocus == true) {
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
        }
    }

    @BindingAdapter("onOkInSoftKeyboard")
    @JvmStatic
    fun setOnOkInSoftKeyboardListener(editText: EditText, listener: (() -> Unit)?) {
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.setOnEditorActionListener { _, actionId, event ->
            if ((event?.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) &&
                listener != null
            ) {
                listener.invoke()
                true
            } else {
                false
            }
        }
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
    fun localizeBinding(textView: AptoTextView, value: String?) {
        textView.localizedText = value ?: ""
    }

    @BindingAdapter(value = ["items", "itemSelected", "itemSelectedAttrChanged"], requireAll = false)
    @JvmStatic
    fun <T> setElements(
        spinner: AppCompatSpinner,
        items: List<T>?,
        itemSelected: T,
        listener: InverseBindingListener?
    ) {
        if (items != null) {
            spinner.adapter = CustomArrayAdapter(spinner.context, items)
            setCurrentSelection(spinner, itemSelected)
            setSpinnerListener(spinner, listener)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @InverseBindingAdapter(attribute = "itemSelected")
    @JvmStatic
    fun <T> getSelectedItem(spinner: AppCompatSpinner): T {
        return spinner.selectedItem as T
    }

    private fun setSpinnerListener(spinner: AppCompatSpinner, listener: InverseBindingListener?) {
        spinner.onItemSelectedListener = if (listener == null) {
            null
        } else {
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    listener.onChange()

                override fun onNothingSelected(adapterView: AdapterView<*>) = listener.onChange()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> setCurrentSelection(spinner: AppCompatSpinner, selectedItem: T?): Boolean {
        if (selectedItem == null) {
            return false
        } else {
            for (index in 0 until spinner.adapter.count) {
                val currentItem = spinner.getItemAtPosition(index) as T
                if (currentItem == selectedItem) {
                    spinner.setSelection(index)
                    return true
                }
            }
            return false
        }
    }

    @JvmStatic
    @BindingAdapter("onClickSafe")
    fun setDebouncedListener(view: View, onClickListener: View.OnClickListener) {
        view.setOnClickListenerSafe { onClickListener.onClick(view) }
    }

    @JvmStatic
    @BindingAdapter("onCheckedChangePressed")
    fun setCheckedChangePressed(view: CompoundButton, listener: CompoundButton.OnCheckedChangeListener) {
        view.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView?.isPressed == true) {
                listener.onCheckedChanged(buttonView, isChecked)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("card")
    internal fun setCard(view: CardShippingStatusView, card: Card) {
        view.card = card
    }
}

package com.aptopayments.sdk.utils

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import com.aptopayments.sdk.R

@BindingAdapter("dropDownItems")
fun AutoCompleteTextView.setItems(items: Array<String>?) =
    setAdapter(ArrayAdapter(context, R.layout.dropdown_menu_popup_item, (items ?: emptyArray())))

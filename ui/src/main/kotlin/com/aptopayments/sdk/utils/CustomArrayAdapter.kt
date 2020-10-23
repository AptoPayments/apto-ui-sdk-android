package com.aptopayments.sdk.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R

class CustomArrayAdapter<T>(
    context: Context,
    list: List<T>
) : ArrayAdapter<T>(context, R.layout.dropdown_menu_popup_item, list) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view = super.getView(position, convertView, parent) as TextView
        view.setTextColor(UIConfig.textPrimaryColor)
        view.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        return view
    }
}

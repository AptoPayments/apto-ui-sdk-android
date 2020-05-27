package com.aptopayments.sdk.features.selectcountry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.core.data.geo.Country
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.theme.themeManager

internal class CountryListAdapter(
    private val countryList: List<CountryListItem>
) : RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {

    interface Delegate {
        fun onCountryTapped(selectedCountry: Country)
    }

    var delegate: Delegate? = null

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var flag: TextView = itemView.findViewById(R.id.tv_flag)
        internal var name: TextView = itemView.findViewById(R.id.tv_country_name)
        internal var selector: AppCompatRadioButton = itemView.findViewById(R.id.rb_country_selector)
        internal var countryRow: RelativeLayout = itemView.findViewById(R.id.root_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val countryItemView = inflater.inflate(R.layout.country_item, parent, false)
        return ViewHolder(countryItemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val countryItem = countryList[position]
        themeManager().customizeMainItem(viewHolder.name)
        themeManager().customizeRadioButton(viewHolder.selector)
        viewHolder.name.text = countryItem.country.name
        viewHolder.flag.text = countryItem.country.flag
        viewHolder.selector.isChecked = countryItem.isSelected
        viewHolder.countryRow.setOnClickListener { delegate?.onCountryTapped(countryItem.country) }
        viewHolder.selector.setOnClickListener { delegate?.onCountryTapped(countryItem.country) }
    }

    override fun getItemCount(): Int {
        return countryList.size
    }
}

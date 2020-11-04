package com.aptopayments.sdk.features.card.fundingsources

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNullable
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import java.util.Locale

internal class FundingSourceAdapter(
    lifecycleOwner: LifecycleOwner,
    viewModel: FundingSourcesViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Delegate {
        fun onFundingSourceTapped(balance: Balance)
        fun onAddFundingSourceTapped()
    }

    var delegate: Delegate? = null
    private var fundingSourceListItems: List<FundingSourceListItem>? = null

    init {
        lifecycleOwner.observeNullable(viewModel.fundingSourceListItems) {
            fundingSourceListItems = it
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = fundingSourceListItems?.size ?: 0

    override fun getItemViewType(position: Int): Int {
        return fundingSourceListItems!![position].itemType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            FundingSourceListItem.FUNDING_SOURCE_ROW_TYPE ->
                inflater.inflate(R.layout.funding_sources_detail, parent, false)
            FundingSourceListItem.ADD_FUNDING_SOURCE_VIEW_TYPE ->
                inflater.inflate(R.layout.funding_sources_add_button, parent, false)
            else -> throw IllegalArgumentException("Unexpected transaction view type")
        }
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.let { viewHolder ->
            when (viewHolder.itemViewType) {
                FundingSourceListItem.FUNDING_SOURCE_ROW_TYPE -> customizeFundingSourceView(viewHolder, position)
                FundingSourceListItem.ADD_FUNDING_SOURCE_VIEW_TYPE -> customizeAddFundingSourceView(viewHolder, position)
                else -> {
                    throw IllegalArgumentException("Unexpected transaction view type")
                }
            }
        }
    }

    private fun customizeFundingSourceView(viewHolder: ViewHolder, position: Int) {
        (fundingSourceListItems?.get(position) as? FundingSourceListItem.FundingSourceRow)?.let { listIem ->
            val balance = listIem.balance
            viewHolder.source?.text = balance.custodianWallet?.custodian?.name?.let { capitalizeString(it) }
            viewHolder.rate?.text = balance.custodianWallet?.balance?.toAbsString()
            viewHolder.amount?.text = balance.balance.toString()
            viewHolder.selector?.isChecked = listIem.selected
            viewHolder.fundingSourceRow?.setOnClickListenerSafe {
                delegate?.onFundingSourceTapped(balance)
                notifyDataSetChanged()
            }
        }
    }

    fun capitalizeString(stringToCapitalize: String): String {
        return stringToCapitalize.toUpperCase(Locale.getDefault())[0] +
                stringToCapitalize.substring(1, stringToCapitalize.length)
    }

    private fun customizeAddFundingSourceView(viewHolder: ViewHolder, position: Int) {
        (fundingSourceListItems?.get(position) as? FundingSourceListItem.AddFundingSourceButton)?.let {
            viewHolder.addFundingSourceButton?.setOnClickListenerSafe {
                delegate?.onAddFundingSourceTapped()
            }
        }
    }

    private class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        // Funding Source row
        var source: TextView? = null
        var rate: TextView? = null
        var amount: TextView? = null
        var fundingSourceRow: View? = null
        var selector: AppCompatRadioButton? = null

        // Add funding source row
        var addFundingSourceButton: TextView? = null

        init {
            when (viewType) {
                FundingSourceListItem.FUNDING_SOURCE_ROW_TYPE -> setupAsFundingSourceRow()
                FundingSourceListItem.ADD_FUNDING_SOURCE_VIEW_TYPE -> setupAsAddFundingSourceButton()
            }
        }

        private fun setupAsFundingSourceRow() {
            source = itemView.findViewById(R.id.source)
            rate = itemView.findViewById(R.id.rate)
            amount = itemView.findViewById(R.id.amount)
            fundingSourceRow = itemView.findViewById(R.id.root_view)
            selector = itemView.findViewById(R.id.radio_button)
            with(themeManager()) {
                customizeMainItem(source!!)
                customizeTimestamp(rate!!)
                customizeAmountMedium(amount!!)
                customizeRadioButton(selector!!)
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupAsAddFundingSourceButton() {
            addFundingSourceButton = itemView.findViewById(R.id.add_funding_source_button)
            addFundingSourceButton!!.text = "card_settings_add_funding_source_button_title".localized()
            with(themeManager()) {
                customizeSubmitButton(addFundingSourceButton!!)
            }
        }
    }
}

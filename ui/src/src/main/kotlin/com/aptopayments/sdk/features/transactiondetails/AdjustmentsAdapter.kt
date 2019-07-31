package com.aptopayments.sdk.features.transactiondetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.sdk.R
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.data.transaction.TransactionAdjustment
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.platform.theme.themeManager
import java.text.NumberFormat

internal class AdjustmentsAdapter(
        private var mTransaction: Transaction,
        private var mAdjustments: List<TransactionAdjustment>,
        private var mContext: Context
) : RecyclerView.Adapter<AdjustmentsAdapter.ViewHolder>() {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var description: TextView = itemView.findViewById(R.id.tv_transfer_description)
        internal var amount: TextView = itemView.findViewById(R.id.tv_transfer_amount)
        internal var exchangeRate: TextView = itemView.findViewById(R.id.tv_transfer_exchange_rate)
        internal var fee: TextView = itemView.findViewById(R.id.tv_transfer_fee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val transactionView = inflater.inflate(R.layout.cv_adjustment, parent, false)
        return ViewHolder(transactionView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val adjustment = mAdjustments[position]
        themeManager().customizeMainItem(viewHolder.description)
        when {
            adjustment.type == TransactionAdjustment.Type.REFUND -> viewHolder.description.text = "transaction_details_adjustment_transfer_to_text".localized(mContext)
            adjustment.type == TransactionAdjustment.Type.CAPTURE -> viewHolder.description.text = "transaction_details_adjustment_transfer_from_text".localized(mContext)
            else -> viewHolder.description.text = adjustment.fundingSourceName
        }

        adjustment.nativeAmount?.let { nativeAmount ->
            val amount: String = if(mTransaction.transactionType.isCredit()) {
                mTransaction.getAmountPrefix() + nativeAmount.toAbsString()
            } else {
                nativeAmount.toAbsString()
            }
            viewHolder.amount.text = String.format("%s %s", "transaction_details.adjustment.amount.title".localized(mContext), amount)
            themeManager().customizeTimestamp(viewHolder.amount)

            adjustment.exchangeRate?.let { exchangeRate ->
                val currency = adjustment.localAmount?.currencySymbol()
                viewHolder.exchangeRate.text = String.format("1 %s @ %s %s", nativeAmount.currency,
                        currency, formatDouble(exchangeRate))
                themeManager().customizeTimestamp(viewHolder.exchangeRate)
            } ?: viewHolder.exchangeRate.remove()
        } ?: viewHolder.amount.remove()

        adjustment.feeAmount?.let {
            val feeAmount: String = if(mTransaction.transactionType.isCredit()) {
                mTransaction.getAmountPrefix() + it.toAbsString()
            } else {
                it.toAbsString()
            }
            viewHolder.fee.text = String.format("%s %s", "transaction_details.adjustment.fee.title".localized(mContext), feeAmount)
            themeManager().customizeTimestamp(viewHolder.fee)
        } ?: viewHolder.fee.remove()
    }

    override fun getItemCount(): Int {
        return mAdjustments.size
    }

    private fun formatDouble(value: Double): String {
        val numberFormatter =  NumberFormat.getNumberInstance()
        numberFormatter.minimumFractionDigits = 2
        numberFormatter.maximumFractionDigits = 2
        return numberFormatter.format(value)
    }
}

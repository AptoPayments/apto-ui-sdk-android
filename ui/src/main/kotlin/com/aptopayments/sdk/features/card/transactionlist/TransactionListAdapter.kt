package com.aptopayments.sdk.features.card.transactionlist

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.utils.extensions.formatForTransactionList
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.iconResource
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.managecard.TransactionListItem
import com.aptopayments.sdk.utils.extensions.toCapitalized
import kotlin.properties.Delegates

internal class TransactionListAdapter(
    transactionListItems: List<TransactionListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Delegate {
        fun onTransactionTapped(transaction: Transaction)
    }

    var delegate: Delegate? = null
    var transactionListItems: List<TransactionListItem> by Delegates.observable(transactionListItems) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun getItemCount() = transactionListItems.count()
    override fun getItemViewType(position: Int) = transactionListItems[position].itemType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            TransactionListItem.SECTION_HEADER_VIEW_TYPE ->
                inflater.inflate(R.layout.view_transaction_section_title, parent, false)
            TransactionListItem.TRANSACTION_ROW_VIEW_TYPE ->
                inflater.inflate(R.layout.view_transaction_row, parent, false)
            else -> throw IllegalArgumentException("Unexpected transaction view type")
        }
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.let { viewHolder ->
            when (viewHolder.itemViewType) {
                TransactionListItem.SECTION_HEADER_VIEW_TYPE -> customizeSectionTitle(viewHolder, position)
                TransactionListItem.TRANSACTION_ROW_VIEW_TYPE -> customizeTransactionRow(viewHolder, position)
                else -> {
                    throw IllegalArgumentException("Unexpected transaction view type")
                }
            }
        }
    }

    private fun customizeSectionTitle(viewHolder: ViewHolder, position: Int) {
        (transactionListItems[position] as TransactionListItem.SectionHeader).let { listItem ->
            viewHolder.sectionTitleTextView?.text = listItem.title
        }
    }

    private fun customizeTransactionRow(viewHolder: ViewHolder, position: Int) {
        (transactionListItems[position] as TransactionListItem.TransactionRow).let { listIem ->
            val transaction = listIem.transaction
            transaction.merchant?.mcc?.let {
                viewHolder.mccIcon?.setImageResource(it.iconResource)
                viewHolder.mccIcon?.setColorFilter(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
            }
            transaction.transactionDescription?.let { viewHolder.transactionDescriptionView?.setText(it.toCapitalized()) }
            viewHolder.transactionDateView?.text = transaction.createdAt.formatForTransactionList()
            transaction.localAmount?.let { viewHolder.transactionAmountView?.setText(transaction.getLocalAmountRepresentation()) }
            transaction.nativeBalance?.let { viewHolder.transactionNativeAmountView?.setText(transaction.getNativeBalanceRepresentation()) }
            if (isLastPositionOfSection(position)) viewHolder.transactionRowSeparator?.hide()
            else viewHolder.transactionRowSeparator?.show()
            viewHolder.transactionRow?.setOnClickListener { delegate?.onTransactionTapped(transaction) }
        }
    }

    private fun isLastPositionOfSection(position: Int): Boolean {
        if (position >= transactionListItems.size - 1) return true
        return transactionListItems[position].itemType() != transactionListItems[position + 1].itemType()
    }

    private class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        // Section title
        var sectionTopSeparatorView: View? = null
        var sectionTitleTextView: TextView? = null
        var sectionBottomSeparatorView: View? = null

        // Transaction row
        var mccIcon: ImageView? = null
        var transactionDescriptionView: TextView? = null
        var transactionDateView: TextView? = null
        var transactionAmountView: TextView? = null
        var transactionNativeAmountView: TextView? = null
        var transactionRowSeparator: View? = null
        var transactionRow: LinearLayout? = null

        init {
            when (viewType) {
                TransactionListItem.SECTION_HEADER_VIEW_TYPE -> setupSectionTitle()
                TransactionListItem.TRANSACTION_ROW_VIEW_TYPE -> setupAsTransactionRow()
            }
        }

        private fun setupSectionTitle() {
            sectionTopSeparatorView = itemView.findViewById(R.id.ll_top_separator)
            sectionTitleTextView = itemView.findViewById(R.id.tv_section_title)
            sectionBottomSeparatorView = itemView.findViewById(R.id.ll_bottom_separator)
            sectionTopSeparatorView?.setBackgroundColor(UIConfig.uiTertiaryColor)
            sectionBottomSeparatorView?.setBackgroundColor(UIConfig.uiTertiaryColor)
            themeManager().customizeStarredSectionTitle(sectionTitleTextView!!, UIConfig.textSecondaryColor)
        }

        private fun setupAsTransactionRow() {
            mccIcon = itemView.findViewById(R.id.iv_icon)
            transactionDescriptionView = itemView.findViewById(R.id.tv_transaction_title)
            transactionDateView = itemView.findViewById(R.id.tv_transaction_description)
            transactionAmountView = itemView.findViewById(R.id.tv_transaction_amount)
            transactionNativeAmountView = itemView.findViewById(R.id.tv_transaction_native_amount)
            transactionRowSeparator = itemView.findViewById(R.id.ll_separator)
            transactionRow = itemView.findViewById(R.id.ll_transaction_row)
            with(themeManager()) {
                customizeMainItem(transactionDescriptionView!!)
                customizeTimestamp(transactionDateView!!)
                customizeAmountSmall(transactionAmountView!!)
                customizeTimestamp(transactionNativeAmountView!!)
            }
            mccIcon!!.background?.setTint(UIConfig.uiTertiaryColor)
            transactionRowSeparator?.setBackgroundColor(UIConfig.uiTertiaryColor)
        }
    }
}

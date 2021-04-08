package com.aptopayments.sdk.features.managecard

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.PCICardView
import com.aptopayments.sdk.utils.extensions.formatForTransactionList
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.aptopayments.sdk.utils.extensions.toCapitalized
import kotlinx.android.synthetic.main.include_transaction_list_header.view.*
import kotlinx.android.synthetic.main.view_transaction_row.view.*
import kotlinx.android.synthetic.main.view_transaction_section_title.view.*

internal open class TransactionListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: ManageCardViewModel
) : RecyclerView.Adapter<TransactionListAdapter.BaseViewHolder>() {

    interface Delegate {
        fun onCardTapped()
        fun onCardSettingsTapped()
        fun onTransactionTapped(transaction: Transaction)
    }

    var delegate: Delegate? = null
    private var transactionListItems: List<TransactionListItem> = listOf()

    fun setItems(list: List<TransactionListItem>) {
        transactionListItems = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = transactionListItems.size

    override fun getItemViewType(position: Int): Int {
        return transactionListItems[position].itemType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TransactionListItem.HEADER_VIEW_TYPE -> {
                val view = inflater.inflate(R.layout.include_transaction_list_header, parent, false)
                ViewHolderHeader(view)
            }
            TransactionListItem.SECTION_HEADER_VIEW_TYPE -> {
                val view = inflater.inflate(R.layout.view_transaction_section_title, parent, false)
                ViewHolderSectionTitle(view)
            }
            TransactionListItem.TRANSACTION_ROW_VIEW_TYPE -> {
                val view = inflater.inflate(R.layout.view_transaction_row, parent, false)
                ViewHolderTransaction(view) { delegate?.onTransactionTapped(it) }
            }
            else -> {
                throw IllegalArgumentException("Unexpected transaction view type")
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(transactionListItems[position], position)
    }

    private fun isLastPositionOfSection(position: Int): Boolean {
        transactionListItems.let {
            if (position >= it.size - 1) return true
            return it[position].itemType() != it[position + 1].itemType()
        }
    }

    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: TransactionListItem, position: Int)
    }

    inner class ViewHolderHeader(view: View) : BaseViewHolder(view) {

        init {
            view.view_card_top_background?.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
            view.view_card_bottom_background?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
            view.pci_card_view?.let { cardView ->
                cardView.setConfiguration(viewModel.cardConfiguration)
                with(lifecycleOwner) {
                    observeNullable(viewModel.fundingSource) { cardView.setValidFundingSource(it?.state == Balance.BalanceState.VALID) }
                    observeNullable(viewModel.card) { card -> card?.let { cardView.setCardInfo(CardInfo.fromCard(it)) } }
                    observeNotNullable(viewModel.showCardDetails) { cardView.showCardDetails(it) }
                }
                cardView.delegate = object : PCICardView.Delegate {
                    override fun cardViewTapped() {
                        delegate?.onCardTapped()
                    }
                }
            }
            view.card_settings_button?.let { cardSettingsButton ->
                cardSettingsButton.backgroundTintList = ColorStateList.valueOf(UIConfig.uiPrimaryColor)
                cardSettingsButton.setOnClickListenerSafe { delegate?.onCardSettingsTapped() }
            }
        }

        override fun bind(item: TransactionListItem, position: Int) {
            // do nothing
        }
    }

    inner class ViewHolderSectionTitle(private val view: View) : BaseViewHolder(view) {

        init {
            view.ll_top_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
            view.ll_bottom_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
            themeManager().customizeStarredSectionTitle(view.tv_section_title, UIConfig.textSecondaryColor)
        }

        override fun bind(item: TransactionListItem, position: Int) {
            val listItem = item as TransactionListItem.SectionHeader
            view.tv_section_title.text = listItem.title
        }
    }

    inner class ViewHolderTransaction(
        private val view: View,
        private val listener: (Transaction) -> Unit
    ) : BaseViewHolder(view) {

        init {
            with(themeManager()) {
                customizeMainItem(view.tv_transaction_title)
                customizeTimestamp(view.tv_transaction_description)
                customizeAmountSmall(view.tv_transaction_amount)
                customizeTimestamp(view.tv_transaction_native_amount)
            }
            view.ll_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        }

        override fun bind(item: TransactionListItem, position: Int) {
            val transaction = (item as TransactionListItem.TransactionRow).transaction
            setState(transaction)
            transaction.transactionDescription?.let { view.tv_transaction_title.text = it.toCapitalized() }
            transaction.localAmount?.let {
                view.tv_transaction_amount.text = transaction.getLocalAmountRepresentation()
            }
            transaction.nativeBalance?.let {
                view.tv_transaction_native_amount.text = transaction.getNativeBalanceRepresentation()
            }
            view.tv_transaction_native_amount.goneIf(transaction.localAmount == transaction.nativeBalance)
            view.ll_separator.invisibleIf(isLastPositionOfSection(position))
            view.ll_transaction_row.setOnClickListenerSafe { listener.invoke(transaction) }
        }

        private fun setState(transaction: Transaction) {
            if (transaction.state == Transaction.TransactionState.DECLINED)
                customizeDeclinedTransaction(view, transaction)
            else
                customizeCompletedTransaction(view, transaction)
        }

        private fun customizeDeclinedTransaction(view: View, transaction: Transaction) {
            transaction.merchant?.mcc?.let {
                view.iv_icon.setImageResource(android.R.color.transparent)
                view.iv_icon.setBackgroundResource(R.drawable.ic_ico_declined)
                view.iv_icon.background?.setTint(UIConfig.uiErrorColor)
            }

            val declinedHeader = transaction.state.toLocalizedString()
            val date = transaction.createdAt.formatForTransactionList()
            view.tv_transaction_description.text = "$declinedHeader $date"

            view.tv_transaction_amount.setTextColor(UIConfig.uiErrorColor)
        }

        private fun customizeCompletedTransaction(view: View, transaction: Transaction) {
            transaction.merchant?.mcc?.let {
                view.iv_icon.setImageResource(it.iconResource)
                view.iv_icon.setColorFilter(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
                view.iv_icon.setBackgroundResource(R.drawable.circle)
                view.iv_icon.background?.setTint(UIConfig.uiTertiaryColor)
            }
            view.tv_transaction_description.text = transaction.createdAt.formatForTransactionList()
            view.tv_transaction_amount.setTextColor(UIConfig.textPrimaryColor)
        }
    }
}

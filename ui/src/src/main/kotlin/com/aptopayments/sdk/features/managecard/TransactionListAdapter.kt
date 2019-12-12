package com.aptopayments.sdk.features.managecard

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.toCapitalized
import com.aptopayments.core.extension.toTransactionListFormat
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.data.transaction.iconResource
import com.aptopayments.sdk.core.extension.invisibleIf
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.extension.observeNullable
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.CardView
import kotlinx.android.synthetic.main.include_transaction_list_header.view.*
import kotlinx.android.synthetic.main.view_transaction_row.view.*
import kotlinx.android.synthetic.main.view_transaction_section_title.view.*

internal open class TransactionListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: ManageCardViewModel
) : RecyclerView.Adapter<TransactionListAdapter.BaseViewHolder>(), CardView.Delegate {

    interface Delegate {
        fun onCardTapped()
        fun onCardSettingsTapped()
        fun onPanTapped()
        fun onTransactionTapped(transaction: Transaction)
    }

    var delegate: Delegate? = null
    private var transactionListItems: List<TransactionListItem> = listOf()

    init {
        lifecycleOwner.observeNotNullable(viewModel.transactionListItems) {
            transactionListItems = it
            notifyDataSetChanged()
        }
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
                ViewHolderHeader(view, this, delegate, lifecycleOwner)
            }
            TransactionListItem.SECTION_HEADER_VIEW_TYPE -> {
                val view = inflater.inflate(R.layout.view_transaction_section_title, parent, false)
                ViewHolderSectionTitle(view)
            }
            TransactionListItem.TRANSACTION_ROW_VIEW_TYPE -> {
                val view = inflater.inflate(R.layout.view_transaction_row, parent, false)
                ViewHolderTransaction(view) { transaction: Transaction -> delegate?.onTransactionTapped(transaction) }
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

    override fun cardViewTapped(cardView: CardView) {
        delegate?.onCardTapped()
    }

    override fun panNumberTappedInCardView(cardView: CardView) {
        delegate?.onPanTapped()
    }

    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: TransactionListItem, position: Int)
    }

    inner class ViewHolderHeader(
        view: View,
        cardDelegate: CardView.Delegate,
        transactionDelegate: Delegate?,
        lifecycleOwner: LifecycleOwner
    ) : BaseViewHolder(view) {

        init {
            view.view_card_top_background?.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
            view.view_card_bottom_background?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
            view.cv_card_view?.let { cardView ->
                with(lifecycleOwner) {
                    observeNullable(viewModel.cardHolder) { cardView.setCardholderName(it) }
                    observeNullable(viewModel.lastFour) { cardView.setLastFour(it) }
                    observeNullable(viewModel.cardInfo) { handleCardDetails(cardView, it) }
                    observeNullable(viewModel.cardNetwork) { cardView.setCardNetwork(it) }
                    observeNullable(viewModel.cardStyle) { cardView.setCardStyle(it) }
                    observeNullable(viewModel.fundingSource) { cardView.setBalance(it) }
                    observeNullable(viewModel.state) { cardView.setCardState(it) }
                }
                cardView.delegate = cardDelegate
            }
            view.card_settings_button?.let { cardSettingsButton ->
                cardSettingsButton.backgroundTintList = ColorStateList.valueOf(UIConfig.uiPrimaryColor)
                cardSettingsButton.setOnClickListener { transactionDelegate?.onCardSettingsTapped() }
            }
        }

        private fun handleCardDetails(cardView: CardView, details: CardDetails?) {
            if (details != null) {
                cardView.setPan(details.pan)
                cardView.setCvv(details.cvv)

                val month = String.format("%02d", details.expirationMonth)
                val year = String.format("%02d", details.expirationYear)
                cardView.setExpiryDate(month, year)
            } else {
                cardView.setPan(null)
                cardView.setCvv(null)
                cardView.setExpiryDate(null, null)
            }
        }

        override fun bind(item: TransactionListItem, position: Int) {
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
            view.ll_separator.invisibleIf(isLastPositionOfSection(position))
            view.ll_transaction_row.setOnClickListener { listener.invoke(transaction) }
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
            val date = transaction.createdAt.toTransactionListFormat()
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
            view.tv_transaction_description.text = transaction.createdAt.toTransactionListFormat()
            view.tv_transaction_amount.setTextColor(UIConfig.textPrimaryColor)
        }
    }
}

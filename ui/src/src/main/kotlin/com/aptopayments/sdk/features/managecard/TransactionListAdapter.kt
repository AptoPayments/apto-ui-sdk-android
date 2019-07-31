package com.aptopayments.sdk.features.managecard

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.core.extension.toCapitalized
import com.aptopayments.sdk.R
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.toTransactionListFormat
import com.aptopayments.sdk.core.data.transaction.iconResource
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

internal class TransactionListAdapter(
        private val lifecycleOwner: LifecycleOwner,
        private val viewModel: ManageCardViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CardView.Delegate {

    interface Delegate {
        fun onCardTapped()
        fun onCardSettingsTapped()
        fun onPanTapped()
        fun onTransactionTapped(transaction: Transaction)
    }

    var delegate: Delegate? = null
    private var transactionListItems: List<TransactionListItem>? = null

    init {
        lifecycleOwner.observeNullable(viewModel.transactionListItems) {
            transactionListItems = it
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = transactionListItems?.size ?: 0

    override fun getItemViewType(position: Int): Int {
        return transactionListItems!![position].itemType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when(viewType) {
            TransactionListItem.headerViewType -> inflater.inflate(R.layout.include_transaction_list_header, parent, false)
            TransactionListItem.sectionHeaderViewType -> inflater.inflate(R.layout.view_transaction_section_title, parent, false)
            TransactionListItem.transactionRowViewType -> inflater.inflate(R.layout.view_transaction_row, parent, false)
            else -> { throw IllegalArgumentException("Unexpected transaction view type") }
        }
        return ViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.let { viewHolder ->
            when (viewHolder.itemViewType) {
                TransactionListItem.headerViewType -> customizeHeaderView(viewHolder)
                TransactionListItem.sectionHeaderViewType -> customizeSectionTitle(viewHolder, position)
                TransactionListItem.transactionRowViewType -> customizeTransactionRow(viewHolder, position)
                else -> { throw IllegalArgumentException("Unexpected transaction view type") }
            }
        }
    }

    private fun customizeHeaderView(viewHolder: ViewHolder) {
        viewHolder.cardTopBackgroundView?.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        viewHolder.cardBottomBackgroundView?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        viewHolder.cardView?.let { cardView ->
            with(lifecycleOwner) {
                observeNullable(viewModel.cardHolder) { cardView.setCardholderName(it) }
                observeNullable(viewModel.lastFour) { cardView.setLastFour(it) }
                observeNullable(viewModel.cardInfoVisible) { handleCardDetailsShown(cardView, it) }
                observeNullable(viewModel.cardNetwork) { cardView.setCardNetwork(it) }
                observeNullable(viewModel.cardStyle) { cardView.setCardStyle(it) }
                observeNullable(viewModel.fundingSource) { cardView.setBalance(it) }
                observeNullable(viewModel.state) { cardView.setCardState(it) }
            }
            cardView.delegate = this
        }
        viewHolder.cardSettingsButton?.let { cardSettingsButton ->
            cardSettingsButton.backgroundTintList = ColorStateList.valueOf(UIConfig.uiPrimaryColor)
            cardSettingsButton.setOnClickListener { delegate?.onCardSettingsTapped() }
        }
    }

    private fun handleCardDetailsShown(cardView: CardView, value: Boolean?) {
        if (value == true) {
            cardView.setPan(viewModel.pan.value)
            cardView.setCvv(viewModel.cvv.value)
            var strMonth: String? = null
            viewModel.expirationMonth.value?.let { strMonth = String.format("%02d", it) }
            var strYear: String? = null
            viewModel.expirationYear.value?.let { strYear = String.format("%02d", it) }
            cardView.setExpiryDate(strMonth, strYear)
        }
        else {
            cardView.setPan(null)
            cardView.setCvv(null)
            cardView.setExpiryDate(null, null)
        }
    }

    private fun customizeSectionTitle(viewHolder: ViewHolder, position: Int) {
        (transactionListItems?.get(position) as? TransactionListItem.SectionHeader)?.let { listItem ->
            viewHolder.sectionTitleTextView?.text = listItem.title
        }
    }

    private fun customizeTransactionRow(viewHolder: ViewHolder, position: Int) {
        (transactionListItems?.get(position) as? TransactionListItem.TransactionRow)?.let { listIem ->
            val transaction = listIem.transaction
            if (transaction.state == Transaction.TransactionState.DECLINED) customizeDeclinedTransaction(viewHolder, transaction)
            else customizeCompletedTransaction(viewHolder, transaction)

            transaction.transactionDescription?.let { viewHolder.transactionTitleView?.setText(it.toCapitalized()) }
            transaction.localAmount?.let { viewHolder.transactionAmountView?.setText(transaction.getLocalAmountRepresentation()) }
            transaction.nativeBalance?.let { viewHolder.transactionNativeAmountView?.setText(transaction.getNativeBalanceRepresentation()) }
            if (isLastPositionOfSection(position)) viewHolder.transactionRowSeparator?.hide()
            else viewHolder.transactionRowSeparator?.show()
            viewHolder.transactionRow?.setOnClickListener { delegate?.onTransactionTapped(transaction)}
        }
    }

    private fun customizeDeclinedTransaction(viewHolder: ViewHolder, transaction: Transaction) {
        transaction.merchant?.mcc?.let {
            viewHolder.mccIcon?.setImageResource(android.R.color.transparent)
            viewHolder.mccIcon?.setBackgroundResource(R.drawable.ic_ico_declined)
            viewHolder.mccIcon?.background?.setTint(UIConfig.uiErrorColor)
        }
        viewHolder.itemView.context?.let {
            val declinedHeader = transaction.state.toString(it)
            val date = transaction.createdAt.toTransactionListFormat()
            viewHolder.transactionDescriptionView?.text = "$declinedHeader $date"
        }
        viewHolder.transactionAmountView?.setTextColor(UIConfig.uiErrorColor)
    }

    private fun customizeCompletedTransaction(viewHolder: ViewHolder, transaction: Transaction) {
        transaction.merchant?.mcc?.let {
            viewHolder.mccIcon?.setImageResource(it.iconResource)
            viewHolder.mccIcon?.setColorFilter(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
            viewHolder.mccIcon?.setBackgroundResource(R.drawable.circle)
            viewHolder.mccIcon?.background?.setTint(UIConfig.uiTertiaryColor)
        }
        viewHolder.transactionDescriptionView?.text = transaction.createdAt.toTransactionListFormat()
        viewHolder.transactionAmountView?.setTextColor(UIConfig.textPrimaryColor)
    }

    private fun isLastPositionOfSection(position: Int): Boolean {
        transactionListItems?.let {
            if (position >= it.size - 1) return true
            return it[position].itemType() != it[position+1].itemType()
        } ?: return true
    }

    override fun cardViewTapped(cardView: CardView) {
        delegate?.onCardTapped()
    }

    override fun panNumberTappedInCardView(cardView: CardView) {
        delegate?.onPanTapped()
    }

    private class ViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
        // Header view
        var cardView: CardView? = null
        var cardSettingsButton: FloatingActionButton? = null
        var cardTopBackgroundView: View? = null
        var cardBottomBackgroundView: View? = null

        // Section title
        var sectionTopSeparatorView: View? = null
        var sectionTitleTextView: TextView? = null
        var sectionBottomSeparatorView: View? = null

        // Transaction row
        var mccIcon: ImageView? = null
        var transactionTitleView: TextView? = null
        var transactionDescriptionView: TextView? = null
        var transactionAmountView: TextView? = null
        var transactionNativeAmountView: TextView? = null
        var transactionRowSeparator: View? = null
        var transactionRow: LinearLayout? = null

        init {
            when(viewType) {
                TransactionListItem.headerViewType -> setupAsHeaderView()
                TransactionListItem.sectionHeaderViewType -> setupSectionTitle()
                TransactionListItem.transactionRowViewType -> setupAsTransactionRow()
            }
        }

        private fun setupAsHeaderView() {
            cardView = itemView.findViewById(R.id.cv_card_view)
            cardSettingsButton = itemView.findViewById(R.id.card_settings_button)
            cardTopBackgroundView = itemView.findViewById(R.id.view_card_top_background)
            cardBottomBackgroundView = itemView.findViewById(R.id.view_card_bottom_background)
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
            transactionTitleView = itemView.findViewById(R.id.tv_transaction_title)
            transactionDescriptionView = itemView.findViewById(R.id.tv_transaction_description)
            transactionAmountView = itemView.findViewById(R.id.tv_transaction_amount)
            transactionNativeAmountView = itemView.findViewById(R.id.tv_transaction_native_amount)
            transactionRowSeparator = itemView.findViewById(R.id.ll_separator)
            transactionRow = itemView.findViewById(R.id.ll_transaction_row)
            with(themeManager()) {
                customizeMainItem(transactionTitleView!!)
                customizeTimestamp(transactionDescriptionView!!)
                customizeAmountSmall(transactionAmountView!!)
                customizeTimestamp(transactionNativeAmountView!!)
            }
            transactionRowSeparator?.setBackgroundColor(UIConfig.uiTertiaryColor)
        }
    }
}

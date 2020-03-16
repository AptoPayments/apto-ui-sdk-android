package com.aptopayments.sdk.features.managecard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.localized
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.AppBarStateChangeListener
import com.aptopayments.sdk.utils.MessageBanner
import kotlinx.android.synthetic.main.fragment_manage_card_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import kotlinx.android.synthetic.main.include_transaction_list_header.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.reflect.Modifier
import java.util.*

private const val FIVE_SECONDS = 5000
private const val CARD_ID_KEY = "CARD_ID"
private const val REQUEST_CODE_PUSH_PROVISIONING = 1100

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class ManageCardFragmentThemeTwo : BaseFragment(), ManageCardContract.View,
        TransactionListAdapter.Delegate, SwipeRefreshLayout.OnRefreshListener {
    override var delegate: ManageCardContract.Delegate? = null
    private lateinit var cardId: String
    private val viewModel: ManageCardViewModel by viewModel { parametersOf(cardId) }
    private lateinit var transactionListAdapter: TransactionListAdapter
    private var menu: Menu? = null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var mLastClickTime: Long = 0
    private var clicksAllowed = true

    override fun layoutId(): Int = R.layout.fragment_manage_card_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = arguments!![CARD_ID_KEY] as String
    }

    override fun onPresented() {
        super.onPresented()
        clicksAllowed = true
        delegate?.configureSecondaryStatusBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_activate_physical_card, menu)
        inflater.inflate(R.menu.menu_stats_chart, menu)
        inflater.inflate(R.menu.menu_account_settings, menu)
        setupMenuItem(menu, R.id.menu_card_stats)
        setupMenuItem(menu, R.id.menu_account_settings)
        setupMenuItem(menu, R.id.menu_activate_physical_card)
        tintMenuItems()
        if(this.menu == null) this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun setupListeners() {
        super.setupListeners()
        add_to_gpay.setOnClickListener {
            onGooglePayPressed()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        (this.menu?.findItem(R.id.menu_activate_physical_card))?.isVisible =
            (viewModel.orderedStatus.value == Card.OrderedStatus.ORDERED)
        (this.menu?.findItem(R.id.menu_card_stats))?.isVisible =
            AptoUiSdk.cardOptions.showStatsButton()
        (this.menu?.findItem(R.id.menu_account_settings))?.isVisible =
            AptoUiSdk.cardOptions.showAccountSettingsButton()
    }

    @SuppressLint("SetTextI18n")
    private fun tintMenuItems() {
        themeManager().customizeMenuImage(tb_llsdk_toolbar.menu.findItem(R.id.menu_account_settings))
        themeManager().customizeMenuImage(tb_llsdk_toolbar.menu.findItem(R.id.menu_card_stats))
        themeManager().customizeMenuImage(tb_llsdk_toolbar.menu.findItem(R.id.menu_activate_physical_card))

        tb_llsdk_toolbar.post {
            tb_llsdk_toolbar.findViewById<TextView>(R.id.tv_menu_activate_physical_card)?.let {
                themeManager().customizeMenuItem(it)
                it.text = "manage_card.activate_physical_card.top_bar_item.title".localized()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_account_settings -> {
                delegate?.onAccountSettingsTapped()
                return true
            }
            R.id.menu_activate_physical_card -> {
                viewModel.card.value?.let { delegate?.onActivatePhysicalCardTapped(it) }
                return true
            }
            R.id.menu_card_stats -> {
                delegate?.onCardStatsTapped()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(orderedStatus) { refreshMenuOptions() }
            observeThree(transactions, transactionsInfoRetrieved, showAddToGooglePay) { transactions, transactionInfoRetrieved, showAddToGooglePay ->
                handleEmptyCase(transactions, transactionInfoRetrieved, showAddToGooglePay!!)
            }
            observeNullable(fundingSource, ::handleBalance)
            observeNullable(cardStyle) { it?.balanceSelectorAsset?.let { url ->
                bv_balance_view.setSelectBalanceIcon(url) }
            }
            failure(failure) { handleFailure(it) }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
        }
    }

    private fun handleEmptyCase(
        transactions: List<Transaction>?,
        transactionInfoRetrieved: Boolean?,
        showAddToGooglePay: Boolean
    ) {
        if (transactionInfoRetrieved != true) {
            return
        }
        empty_state_container.visibleIf(transactions.isNullOrEmpty())
        add_to_gpay.visibleIf(showAddToGooglePay)
        tv_no_transactions.goneIf(!showAddToGooglePay)
    }

    private var previousMessageShownAt = 0L
    private fun handleBalance(balance: Balance?) {
        if (viewModel.balanceLoaded
                && (balance == null || balance.state == Balance.BalanceState.INVALID)) {
            val duration = FIVE_SECONDS
            if (Date().time - previousMessageShownAt < duration) return
            previousMessageShownAt = Date().time
            notify("invalid_funding_source_message".localized(), MessageBanner.MessageType.ERROR)
        }
        else {
            bv_balance_view.set(balance)
            bv_balance_view.setOnClickListener { onBalanceClicked() }
        }
    }

    private fun onBalanceClicked() {
        // Prevent multiple clicks using threshold of 1000ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        delegate?.onFundingSourceTapped(viewModel.fundingSource.value?.id)
    }

    override fun onCardTapped() {
        if (viewModel.fundingSource.value?.state != Balance.BalanceState.VALID) {
            delegate?.onFundingSourceTapped(viewModel.fundingSource.value?.id)
            return
        }
        showCardSettings()
    }

    override fun onCardSettingsTapped() {
        showCardSettings()
    }

    override fun onPanTapped() {
        if (viewModel.cardInfo.value == null) {
            showCardSettings()
        } else {
            copyPanToClipboard(viewModel.cardInfo.value!!.pan)
        }
    }

    private fun copyPanToClipboard(pan: String) {
        context?.let { context ->
            val clipData = ClipData.newPlainText("Card Number", pan)
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.let {
                it.setPrimaryClip(clipData)
                notify(
                    message = "manage_card_card_view_copy_pan_text".localized(),
                    type = MessageBanner.MessageType.SUCCESS
                )
            }
        }
    }

    private fun showCardSettings() = viewModel.card.value?.let {
        delegate?.onCardSettingsTapped(it)
    }

    override fun onTransactionTapped(transaction: Transaction) {
        if (clicksAllowed) {
            clicksAllowed = false
            delegate?.onTransactionTapped(transaction)
        }
    }

    override fun setupUI() {
        setupToolbar()
        setupTheme()
        setupSwipeToRefresh()
        setupRecyclerView()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTheme() {
        with (themeManager()) {
            customizeEmptyCase(tv_no_transactions)
        }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.iconTertiaryColor)
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = null,
                backButtonMode = BaseActivity.BackButtonMode.None // TODO: Use standalone mode
        )
        abl_manage_card.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(offsetPercent: Float) {
                swipe_refresh_container.isEnabled = offsetPercent == 0.0f
                abl_manage_card.post {
                    bv_balance_view?.applyAlphaAndTextSize(offsetPercent)
                    animateBackground(offsetPercent)
                }
            }
        })
    }

    private fun animateBackground(offsetPercent: Float) = view_card_top_background?.let {
        val layoutParams = it.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 1.0f - offsetPercent
        it.layoutParams = layoutParams
    }

    private fun setupRecyclerView() = context?.let { context ->
        transactionListAdapter = TransactionListAdapter(context as LifecycleOwner, viewModel)
        val linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.getMoreTransactions(cardId) { newTransactionCount ->
                    if (newTransactionCount > 0) transactionListAdapter.notifyDataSetChanged()
                }
            }
        }
        transactionListAdapter.delegate = this
        transactions_recycler_view.apply {
            layoutManager = linearLayoutManager
            addOnScrollListener(scrollListener!!)
            adapter = transactionListAdapter
        }
    }

    private fun setupSwipeToRefresh() = swipe_refresh_container.setOnRefreshListener(this)

    // Refresh listener
    override fun onRefresh() {
        viewModel.refreshData(cardId) {
            scrollListener?.resetState()
            swipe_refresh_container?.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.viewReady()
    }

    override fun onResume() {
        super.onResume()
        scrollListener?.resetState()
    }

    override fun onBackPressed() {
        if (AptoUiSdk.cardOptions.openingMode == CardOptions.OpeningMode.EMBEDDED) {
            delegate?.onBackFromManageCard()
        }
    }

    override fun refreshCardData() = viewModel.refreshCard(cardId = cardId)

    override fun refreshBalance() = viewModel.refreshBalance(cardId = cardId)

    override fun refreshTransactions() {
        showLoading()
        viewModel.refreshTransactions(cardId = cardId) {
            hideLoading()
        }
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    private fun onGooglePayPressed() {
        activity?.let {
            viewModel.onAddToGooglePayPressed(it, REQUEST_CODE_PUSH_PROVISIONING)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_PUSH_PROVISIONING -> viewModel.onReturnedFromAddToGooglePay()
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        fun newInstance(cardId: String) = ManageCardFragmentThemeTwo().apply {
            arguments = Bundle().apply { putString(CARD_ID_KEY, cardId) }
        }
    }
}

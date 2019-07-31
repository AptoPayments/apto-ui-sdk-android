package com.aptopayments.sdk.features.managecard

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.localized
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.AppBarStateChangeListener
import com.aptopayments.sdk.utils.MessageBanner
import kotlinx.android.synthetic.main.fragment_manage_card_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import kotlinx.android.synthetic.main.include_transaction_list_header.*
import java.lang.reflect.Modifier
import java.util.*

private const val FIVE_SECONDS = 5000
private const val CARD_ID_KEY = "CARD_ID"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class ManageCardFragmentThemeTwo : BaseFragment(), ManageCardContract.View,
        TransactionListAdapter.Delegate, SwipeRefreshLayout.OnRefreshListener {
    override var delegate: ManageCardContract.Delegate? = null
    private lateinit var cardId: String
    private lateinit var viewModel: ManageCardViewModel
    private lateinit var transactionListAdapter: TransactionListAdapter
    private var menu: Menu? = null
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var mLastClickTime: Long = 0

    override fun layoutId(): Int = R.layout.fragment_manage_card_theme_two

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardId = arguments!![CARD_ID_KEY] as String
    }

    override fun onPresented() {
        super.onPresented()
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
        context?.let { tintMenuItems(it) }
        if(this.menu == null) this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        (this.menu?.findItem(R.id.menu_activate_physical_card))?.isVisible =
                (viewModel.orderedStatus.value == Card.OrderedStatus.ORDERED)
        (this.menu?.findItem(R.id.menu_card_stats))?.isVisible = AptoPlatform.cardOptions.showStatsButton()
    }

    private fun tintMenuItems(context: Context) {
        val accountSettingsIcon = ContextCompat.getDrawable(context, R.drawable.ic_account_settings)
        accountSettingsIcon?.setTint(UIConfig.textTopBarColor)
        tb_llsdk_toolbar.menu.findItem(R.id.menu_account_settings)?.icon = accountSettingsIcon
        val statsChartIcon = ContextCompat.getDrawable(context, R.drawable.ic_chart)
        statsChartIcon?.setTint(UIConfig.textTopBarColor)
        tb_llsdk_toolbar.menu.findItem(R.id.menu_card_stats)?.icon = statsChartIcon
        val activateCardIcon = ContextCompat.getDrawable(context, R.drawable.ic_activate_physical_card)
        activateCardIcon?.setTint(UIConfig.textTopBarColor)
        tb_llsdk_toolbar.menu.findItem(R.id.menu_activate_physical_card)?.icon = activateCardIcon
        tb_llsdk_toolbar.post {
            tb_llsdk_toolbar.findViewById<TextView>(R.id.tv_menu_activate_physical_card)?.let {
                themeManager().customizeMenuItem(it)
                it.text = "manage_card.activate_physical_card.top_bar_item.title".localized(context)
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
        viewModel = viewModel(viewModelFactory) {
            observe(orderedStatus) { refreshMenuOptions() }
            observeTwo(transactions, transactionsInfoRetrieved) {
                transactions, transactionInfoRetrieved -> handleEmptyCase(transactions, transactionInfoRetrieved)}
            observeNullable(fundingSource, ::handleBalance)
            observeNullable(cardStyle) { it?.balanceSelectorAsset?.let { url ->
                bv_balance_view.setSelectBalanceIcon(url) } }
            failure(failure) {
                handleFailure(it)
            }
        }
    }

    private fun handleEmptyCase(transactions: List<Transaction>?, transactionInfoRetrieved: Boolean?) {
        if (transactionInfoRetrieved == null || transactionInfoRetrieved == false) return
        transactions?.let {
            if (it.isEmpty()) {
                tv_no_transactions.show()
            }
            else {
                tv_no_transactions.hide()
            }
        } ?: tv_no_transactions.show()
    }

    private var previousMessageShownAt = 0L
    private fun handleBalance(balance: Balance?) {
        if (viewModel.balanceLoaded
                && (balance == null || balance.state == Balance.BalanceState.INVALID)) {
            val duration = FIVE_SECONDS
            if (Date().time - previousMessageShownAt < duration) return
            previousMessageShownAt = Date().time
            activity?.let { MessageBanner(it).showBanner(R.string.invalid_funding_source_message,
                    MessageBanner.MessageType.ERROR, duration) }
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
        if (viewModel.cardInfoVisible.value != true) {
            showCardSettings()
            return
        }
        context?.let { context ->
            val clipData = ClipData.newPlainText("Card Number", viewModel.pan.value)
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.let {
                it.primaryClip = clipData
                notify(
                        message = "manage_card_card_view_copy_pan_text".localized(context),
                        type = MessageBanner.MessageType.SUCCESS
                )
            }
        }
    }

    private fun showCardSettings() {
        viewModel.card.value?.let { delegate?.onCardSettingsTapped(it, viewModel.cardInfoVisible.value == true) }
    }

    override fun onTransactionTapped(transaction: Transaction) {
        delegate?.onTransactionTapped(transaction)
    }

    override fun cardDetailsChanged(cardDetails: CardDetails?) {
        viewModel.cardDetailsChanged(cardDetails)
    }

    override fun setupUI() {
        setupToolbar()
        setupTheme()
        setupSwipeToRefresh()
        setupRecyclerView()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        with (themeManager()) {
            customizeEmptyCase(tv_no_transactions)
        }
        context?.let {
            tv_no_transactions.text = "manage_card_no_transactions".localized(it)
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
                    bv_balance_view.applyAlphaAndTextSize(offsetPercent)
                    animateBackground(offsetPercent)
                }
            }
        })
    }

    private fun animateBackground(offsetPercent: Float) {
        view_card_top_background?.let {
            val layoutParams = it.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 1.0f - offsetPercent
            it.layoutParams = layoutParams
        }
    }

    private fun setupRecyclerView() {
        context?.let { context ->
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
    }

    private fun setupSwipeToRefresh() {
        swipe_refresh_container.setOnRefreshListener(this)
    }

    // Refresh listener
    override fun onRefresh() {
        viewModel.refreshData(cardId) {
            scrollListener?.resetState()
            swipe_refresh_container?.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.viewReady(cardId = cardId)
    }

    override fun onResume() {
        super.onResume()
        scrollListener?.resetState()
    }

    override fun onBackPressed() {
        if (AptoPlatform.cardOptions.openingMode == CardOptions.OpeningMode.EMBEDDED) {
            delegate?.onBackFromManageCard()
        }
    }

    override fun refreshCardData() {
        viewModel.refreshCard(cardId = cardId)
    }

    override fun refreshBalance() {
        viewModel.refreshBalance(cardId = cardId)
    }

    override fun refreshTransactions() {
        showLoading()
        viewModel.refreshTransactions(cardId = cardId) {
            hideLoading()
        }
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

    companion object {
        fun newInstance(cardId: String): ManageCardFragmentThemeTwo {
            return ManageCardFragmentThemeTwo().apply { arguments = Bundle().apply { putString(CARD_ID_KEY, cardId) } }
        }
    }
}

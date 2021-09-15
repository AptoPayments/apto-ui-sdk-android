package com.aptopayments.sdk.features.managecard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.AppBarStateChangeListener
import com.aptopayments.sdk.repository.IAPHelper
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_manage_card.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import kotlinx.android.synthetic.main.include_transaction_list_header.*
import kotlinx.android.synthetic.main.layout_menu_account_settings.*
import kotlinx.android.synthetic.main.layout_menu_account_settings.view.*
import kotlinx.android.synthetic.main.layout_menu_activate_physical_card.*
import kotlinx.android.synthetic.main.layout_menu_stats_chart.*
import kotlinx.android.synthetic.main.layout_menu_stats_chart.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import org.koin.core.inject
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

private const val FIVE_SECONDS = 5
private const val CARD_ID_KEY = "CARD_ID"

internal class ManageCardFragment :
    BaseFragment(),
    ManageCardContract.View,
    TransactionListAdapter.Delegate,
    SwipeRefreshLayout.OnRefreshListener,
    KoinComponent {

    val iapHelper: IAPHelper by inject()
    override var delegate: ManageCardContract.Delegate? = null
    private lateinit var cardId: String
    private val viewModel: ManageCardViewModel by viewModel { parametersOf(cardId) }
    private lateinit var transactionListAdapter: TransactionListAdapter
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    private var previousMessageShownAt = LocalDateTime.of(2010, 1, 1, 0, 0, 0)

    override fun layoutId(): Int = R.layout.fragment_manage_card

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID_KEY] as String
    }

    override fun onPresented() {
        super.onPresented()
        customizeSecondaryNavigationStatusBar()
    }

    private fun configureMenuVisibility(menuState: ManageCardViewModel.MenuState) {
        tb_llsdk_toolbar?.apply {
            (menu.findItem(R.id.menu_activate_physical_card))?.isVisible = menuState.showPhysicalCardActivationMessage
            (menu.findItem(R.id.menu_card_stats))?.isVisible = menuState.showStats
            (menu.findItem(R.id.menu_account_settings))?.isVisible = menuState.showAccountSettings
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        add_to_gpay.setOnClickListenerSafe {
            onGooglePayPressed()
        }
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(menuState) { configureMenuVisibility(it) }
            observeNotNullable(emptyState) { handleEmptyState(it) }
            observeNullable(fundingSource, ::handleBalance)
            observeNullable(card) {
                it?.cardStyle?.balanceSelectorAsset?.let { url ->
                    bv_balance_view.setSelectBalanceIcon(url)
                }
            }
            observe(failure) { handleFailure(it) }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observeNullable(viewModel.showFundingSourceDialog) { delegate?.onFundingSourceTapped(it) }
            observeNotNullable(viewModel.transactionListItems) { transactionListAdapter.setItems(it) }
        }
    }

    private fun handleEmptyState(state: ManageCardViewModel.EmptyState) {
        with(state) {
            empty_state_container.visibleIf(showContainer)
            add_to_gpay.visibleIf(showAddToGPay)
            tv_no_transactions.visibleIf(showNoTransactions)
        }
    }

    private fun handleBalance(balance: Balance?) {
        if (viewModel.balanceLoaded && (balance == null || balance.state == Balance.BalanceState.INVALID)) {
            if (previousMessageShownAt.until(LocalDateTime.now(), ChronoUnit.SECONDS) < FIVE_SECONDS) {
                return
            } else {
                previousMessageShownAt = LocalDateTime.now()
                notify("invalid_funding_source_message".localized(), SnackbarMessageType.ERROR)
            }
        } else {
            bv_balance_view.set(balance)
            bv_balance_view.setOnClickListenerSafe { viewModel.onFundingSourceTapped() }
        }
    }

    override fun onCardSettingsTapped() {
        showCardSettings()
    }

    private fun showCardSettings() = viewModel.card.value?.let {
        delegate?.onCardSettingsTapped(it)
    }

    override fun onTransactionTapped(transaction: Transaction) {
        delegate?.onTransactionTapped(transaction)
    }

    override fun setupUI() {
        setupToolbar()
        setupTheme()
        setupSwipeToRefresh()
        setupRecyclerView()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeEmptyCase(tv_no_transactions)
        }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar?.apply {
            inflateMenu(R.menu.menu_manage_card)
            menu_container_stats?.setOnClickListenerSafe { delegate?.onCardStatsTapped() }
            menu_container_account_settings?.setOnClickListenerSafe { delegate?.onAccountSettingsTapped() }
            menu_container_activate_card?.setOnClickListenerSafe {
                viewModel.card.value?.let { delegate?.onActivatePhysicalCardTapped(it) }
            }
            tintMenuItems()
        }
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder().backButtonMode(getBackButtonMode()).setSecondaryTertiaryColors().build()
        )
        setOffsetChangedListener()
    }

    private fun getBackButtonMode(): BackButtonMode {
        return if (viewModel.showXOnToolbar) {
            BackButtonMode.Close(UIConfig.iconTertiaryColor)
        } else {
            BackButtonMode.None
        }
    }

    private fun setOffsetChangedListener() {
        abl_manage_card.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(offsetPercent: Float) {
                swipe_refresh_container.isEnabled = offsetPercent <= 0.4f
                abl_manage_card.post {
                    bv_balance_view?.applyAlphaAndTextSize(offsetPercent)
                    animateBackground(offsetPercent)
                }
            }
        })
    }

    private fun tintMenuItems() {
        themeManager().customizeMenuLayoutImage(menu_container_account_settings)
        themeManager().customizeMenuLayoutImage(menu_container_activate_card)
        themeManager().customizeMenuLayoutImage(menu_container_stats)
        tv_menu_activate_physical_card?.let { themeManager().customizeMenuItem(it) }
    }

    private fun animateBackground(offsetPercent: Float) = view_card_top_background?.let {
        val layoutParams = it.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 1.0f - offsetPercent
        it.layoutParams = layoutParams
    }

    private fun setupRecyclerView() = context?.let { context ->
        transactionListAdapter = TransactionListAdapter(viewLifecycleOwner, viewModel)
        val linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.getMoreTransactions()
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
        viewModel.refreshData {
            scrollListener?.resetState()
            swipe_refresh_container?.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        iapHelper.registerDataChanged()
        scrollListener?.resetState()
    }

    override fun onPause() {
        super.onPause()
        iapHelper.unregisterDataChanged()
    }

    override fun onBackPressed() {
        if (viewModel.canBackPress) {
            delegate?.onBackFromManageCard()
        }
    }

    override fun refreshCardData() = viewModel.refreshCard()

    override fun refreshBalance() = viewModel.refreshBalance()

    override fun refreshTransactions() = viewModel.refreshTransactions()

    private fun onGooglePayPressed() {
        activity?.let {
            viewModel.onAddToGooglePayPressed(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!viewModel.onActivityResult(requestCode, resultCode == Activity.RESULT_OK)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        fun newInstance(cardId: String) = ManageCardFragment().apply {
            arguments = Bundle().apply { putString(CARD_ID_KEY, cardId) }
        }
    }
}

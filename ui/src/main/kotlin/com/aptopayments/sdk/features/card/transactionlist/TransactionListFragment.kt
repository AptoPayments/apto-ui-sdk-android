package com.aptopayments.sdk.features.card.transactionlist

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.managecard.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.include_toolbar_two.*
import kotlinx.android.synthetic.main.transaction_list_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_KEY = "CARD"
private const val CONFIG_KEY = "CONFIG"

internal class TransactionListFragment :
    BaseFragment(),
    TransactionListContract.View,
    TransactionListAdapter.Delegate,
    SwipeRefreshLayout.OnRefreshListener {
    override var delegate: TransactionListContract.Delegate? = null

    lateinit var cardId: String
    lateinit var config: TransactionListConfig
    private val viewModel: TransactionListViewModel by viewModel { parametersOf(cardId, config) }
    private lateinit var transactionListAdapter: TransactionListAdapter
    private var scrollListener: EndlessRecyclerViewScrollListener? = null

    override fun layoutId(): Int = R.layout.transaction_list_fragment
    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_KEY] as String
        config = requireArguments()[CONFIG_KEY] as TransactionListConfig
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observeNotNullable(transactionListItems) { transactionListAdapter.transactionListItems = it }
        }
    }

    override fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        swipe_refresh_container.setOnRefreshListener(this)
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .title(config.mcc.toLocalizedString())
                .setSecondaryTertiaryColors()
                .build()
        )
    }

    private fun setupRecyclerView() {
        transactionListAdapter = TransactionListAdapter(emptyList())
        val linearLayoutManager = LinearLayoutManager(context)
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                viewModel.fetchMoreTransaction()
            }
        }
        transactionListAdapter.delegate = this
        transactions_recycler_view.apply {
            layoutManager = linearLayoutManager
            addOnScrollListener(scrollListener!!)
            adapter = transactionListAdapter
        }
    }

    override fun onTransactionTapped(transaction: Transaction) {
        delegate?.onTransactionTapped(transaction)
    }

    override fun onRefresh() = viewModel.fetchTransaction() {
        scrollListener?.resetState()
        swipe_refresh_container?.isRefreshing = false
    }

    override fun onBackPressed() {
        delegate?.onBackPressed()
    }

    companion object {
        fun newInstance(cardId: String, config: TransactionListConfig) = TransactionListFragment().apply {
            arguments = Bundle().apply {
                putString(CARD_KEY, cardId)
                putSerializable(CONFIG_KEY, config)
            }
        }
    }
}

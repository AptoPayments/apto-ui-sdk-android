package com.aptopayments.sdk.features.card.fundingsources

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.State
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import kotlinx.android.synthetic.main.fragment_funding_sources_dialog.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ACCOUNT_ID_KEY = "ACCOUNT_ID"
private const val SELECTED_BALANCE_ID_KEY = "SELECTED_BALANCE_ID"

internal class FundingSourceDialogFragment :
    BaseDialogFragment(),
    FundingSourceContract.View,
    FundingSourceAdapter.Delegate {

    override var delegate: FundingSourceContract.Delegate? = null

    private val viewModel: FundingSourcesViewModel by viewModel()
    private lateinit var mRecyclerView: RecyclerView
    private var mAccountId: String = ""
    private var mSelectedBalanceID: String? = ""

    override fun layoutId(): Int = R.layout.fragment_funding_sources_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccountId = requireArguments()[ACCOUNT_ID_KEY] as String
        mSelectedBalanceID = arguments?.getString(SELECTED_BALANCE_ID_KEY)
    }

    override fun onDestroyView() {
        mRecyclerView.adapter = null
        super.onDestroyView()
    }

    override fun setUpUI() {
        setupTheme()
        setupRecyclerView()
    }

    override fun setUpViewModel() {
        viewModel.apply {
            observe(fundingSourceListItems, ::handleBalanceList)
            observe(state, ::updateProgressState)
            observe(failure) { handleFailure(it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        positionDialog()
        viewModel.viewReady(mAccountId, mSelectedBalanceID)
    }

    private fun handleBalanceList(balanceList: List<FundingSourceListItem>?) {
        balanceList?.let { list ->
            if (list.isEmpty()) {
                hideResultsView()
                showNoFundingSourcesMessage()
            } else {
                showResultsView()
                hideNoFundingSourcesMessage()
            }
        }
    }

    private fun updateProgressState(state: State?) = if (state == State.IN_PROGRESS) showLoading() else hideLoading()

    private fun showLoading() {
        progress_holder.show()
        hideResultsView()
    }

    private fun hideLoading() {
        progress_holder.remove()
        showResultsView()
    }

    private fun showNoFundingSourcesMessage() {
        refresh_button.hide()
        no_funding_sources_holder.show()
    }

    private fun hideNoFundingSourcesMessage() {
        refresh_button.show()
        no_funding_sources_holder.hide()
    }

    private fun hideResultsView() = mRecyclerView.hide()

    private fun showResultsView() = mRecyclerView.show()

    override fun setUpListeners() {
        refresh_button.setOnClickListener {
            viewModel.fetchData(mAccountId, refresh = true) {}
        }
        add_funding_source_button.setOnClickListenerSafe {
            delegate?.onAddFundingSource(mSelectedBalanceID)
        }
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeHighlightTitleLabel(tv_dialog_title)
            customizeEmptyCase(tv_no_funding_sources)
            customizeSubmitButton(add_funding_source_button)
            view?.let { customizeRoundedBackground(it) }
            progress_bar.indeterminateDrawable.setColorFilterCompat(UIConfig.uiPrimaryColor, PorterDuff.Mode.SRC_IN)
        }
        refresh_button.setColorFilter(UIConfig.uiPrimaryColor)
    }

    private fun setupRecyclerView() {
        val fundingSourceAdapter = FundingSourceAdapter(viewLifecycleOwner, viewModel)
        fundingSourceAdapter.delegate = this
        val scroller = dialogView.findViewById<NestedScrollView>(R.id.nested_scrollbar)
        mRecyclerView = scroller.findViewById(R.id.funding_source_recycler)
        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.4).toInt()
            adapter = fundingSourceAdapter
        }
    }

    override fun onFundingSourceTapped(balance: Balance) = viewModel.setCardFundingSource(mAccountId, balance.id) {
        delegate?.onFundingSourceSelected(
            onFinish = {
                if (it) {
                    notify(
                        message = "manage_card.funding_source_selector.success.message".localized(),
                        messageType = SnackbarMessageType.HEADS_UP,
                        title = "manage_card.funding_source_selector.success.title".localized()
                    )
                }
            }
        )
    }

    override fun onAddFundingSourceTapped() {
        delegate?.onAddFundingSource(mSelectedBalanceID)
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(cardID: String, selectedBalanceID: String?) = FundingSourceDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ACCOUNT_ID_KEY, cardID)
                putString(SELECTED_BALANCE_ID_KEY, selectedBalanceID)
            }
        }
    }
}

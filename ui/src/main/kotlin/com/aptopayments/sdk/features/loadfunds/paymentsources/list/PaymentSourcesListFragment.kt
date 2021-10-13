package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListViewModel.Actions
import kotlinx.android.synthetic.main.fragment_payment_sources_list.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class PaymentSourcesListFragment : BaseFragment(), PaymentSourcesListContract.View {

    override fun layoutId() = R.layout.fragment_payment_sources_list
    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    private val viewModel: PaymentSourcesListViewModel by viewModel()
    private val adapter by lazy { PaymentSourcesListAdapter(viewModel) }

    override var delegate: PaymentSourcesListContract.Delegate? = null

    override fun onPresented() {
        super.onPresented()
        viewModel.onPresented()
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.sourceList) { adapter.setData(it) }
        observeNotNullable(viewModel.actions) { action ->
            when (action) {
                is Actions.NewPaymentSource -> delegate?.newCardPressed()
                is Actions.SourceSelected -> delegate?.onClosePaymentSourcesList()
                is Actions.ShowConfirmDeleteDialog -> showConfirmationDialog(action.sourceId, action.position)
            }
        }
    }

    private fun showConfirmationDialog(sourceId: String, position: Int) {
        confirm(
            title = "load_funds_payment_methods_delete_dialog_title".localized(),
            text = "load_funds_payment_methods_delete_dialog_message".localized(),
            confirm = "load_funds_payment_methods_delete_dialog_cta".localized(),
            cancel = "load_funds_payment_methods_delete_dialog_cancel".localized(),
            onConfirm = { viewModel.onPaymentSourceDeleteConfirmed(sourceId) },
            onCancel = { adapter.notifyItemChanged(position) }
        )
    }

    override fun setupUI() {
        setupToolbar()
        payment_sources_list.adapter = adapter
        payment_sources_list.layoutManager = LinearLayoutManager(context)
        configureSwipeToDelete()
    }

    private fun configureSwipeToDelete() {
        val swipeHandler = SwipeToDeleteCallback()
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(payment_sources_list)
        lifecycleScope.launch {
            swipeHandler.event.collect { element ->
                element.item?.let {
                    viewModel.onPaymentSourceDelete(it.elem.id, element.position)
                }
            }
        }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Close(UIConfig.textTopBarSecondaryColor))
                .title("load_funds_payment_methods_title".localized())
                .setSecondaryColors()
                .build()
        )
    }

    override fun onBackPressed() {
        delegate?.onClosePaymentSourcesList()
    }

    companion object {
        fun newInstance(tag: String) = PaymentSourcesListFragment().apply {
            TAG = tag
        }
    }
}

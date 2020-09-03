package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import androidx.recyclerview.widget.LinearLayoutManager
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import kotlinx.android.synthetic.main.fragment_payment_sources_list.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
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
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNullable(viewModel.sourceSelected) { delegate?.onClosePaymentSourcesList() }
        observeNullable(viewModel.newPaymentSource) { delegate?.newCardPressed() }
        observeNotNullable(viewModel.sourceList) { adapter.setData(it) }
    }

    override fun setupUI() {
        setupToolbar()
        payment_sources_list.adapter = adapter
        payment_sources_list.layoutManager = LinearLayoutManager(context)
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

package com.aptopayments.sdk.features.p2p.result

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentP2pResultBinding
import com.aptopayments.sdk.utils.extensions.setValue
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val RESULT_DATA = "RESULT_DATA_KEY"

internal class P2pResultFragment : BaseDataBindingFragment<FragmentP2pResultBinding>(), P2pResultContract.View {

    override var delegate: P2pResultContract.Delegate? = null
    private lateinit var result: P2pTransferResponse

    private val viewModel: P2pResultViewModel by viewModel { parametersOf(result) }

    override fun layoutId() = R.layout.fragment_p2p_result

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        hideKeyboard()
    }

    override fun setUpArguments() {
        super.setUpArguments()
        result = requireArguments()[RESULT_DATA] as P2pTransferResponse
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.action) {
            when (it) {
                is P2pResultViewModel.Action.CtaClicked -> delegate?.onAddFundsResultsDone()
            }
        }
    }

    override fun setupUI() {
        themeManager().customizeSubmitButton(binding.p2pResultCta)
        listOf(
            binding.p2pResultMainStatus,
            binding.p2pResultStatusTitle,
            binding.p2pResultStatusValue,
            binding.p2pResultTimeTitle,
            binding.p2pResultTimeValue,
        ).forEach { it.setTextColor(UIConfig.textPrimaryColor) }

        binding.p2pResultMainStatus.text = getStatusText(viewModel.state.status)
    }

    private fun getStatusText(status: PaymentStatus): String {
        return if (status == PaymentStatus.PROCESSED) {
            "p2p_transfer_result_correct_processed_title".localized().setValue(viewModel.state.name, "NAME")
                .setValue(viewModel.state.amount)
        } else {
            "p2p_transfer_result_pending_title".localized().setValue(viewModel.state.name)
        }
    }

    override fun onBackPressed() {
        delegate?.onAddFundsResultsDone()
    }

    companion object {
        fun newInstance(result: P2pTransferResponse, tag: String) = P2pResultFragment().apply {
            arguments = Bundle().apply {
                putSerializable(RESULT_DATA, result)
            }
            TAG = tag
        }
    }
}

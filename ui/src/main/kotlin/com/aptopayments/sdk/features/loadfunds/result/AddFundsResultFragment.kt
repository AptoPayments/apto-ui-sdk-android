package com.aptopayments.sdk.features.loadfunds.result

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentAddFundsResultBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID = "CARD_ID"
private const val PAYMENT_KEY = "PAYMENT_KEY"
private const val PAYMENT_SOURCE = "PAYMENT_SOURCE"

internal class AddFundsResultFragment : BaseBindingFragment<FragmentAddFundsResultBinding>(),
    AddFundsResultContract.View {

    override var delegate: AddFundsResultContract.Delegate? = null

    private val viewModel: AddFundsResultViewModel by viewModel { parametersOf(cardId, payment) }
    private lateinit var cardId: String
    private lateinit var payment: Payment

    override fun layoutId() = R.layout.fragment_add_funds_result
    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.onAgreement) { delegate?.onCardholderAgreement(it) }
        observeNotNullable(viewModel.onDone) { delegate?.onBackFromAddFundsResult() }
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
    }

    override fun setUpArguments() {
        super.setUpArguments()
        cardId = arguments!![CARD_ID] as String
        payment = arguments!![PAYMENT_KEY] as Payment
    }

    override fun setupUI() {
        setUpViews()
    }

    private fun setUpViews() {
        setTextColors()
        themeManager().customizeSubmitButton(binding.loadFundsResultDone)
    }

    private fun setTextColors() {
        setPrimaryTextColor()
        binding.loadFundsResultLegend.setTextColor(UIConfig.textSecondaryColor)
        binding.loadFundsResultLearnMore.setTextColor(UIConfig.textLinkColor)
    }

    private fun setPrimaryTextColor() {
        listOf(
            binding.loadFundsResultMainStatus,
            binding.loadFundsResultStatusTitle,
            binding.loadFundsResultStatusValue,
            binding.loadFundsResultTimeTitle,
            binding.loadFundsResultTimeValue,
            binding.loadFundsResultFromTitle,
            binding.loadFundsResultAuthTitle,
            binding.loadFundsResultAuthValue
        ).forEach { it.setTextColor(UIConfig.textPrimaryColor) }
    }

    override fun onBackPressed() {
        delegate?.onBackFromAddFundsResult()
    }

    companion object {
        fun newInstance(cardId: String, payment: Payment, tag: String) =
            AddFundsResultFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CARD_ID, cardId)
                    putSerializable(PAYMENT_KEY, payment)
                }
                TAG = tag
            }
    }
}

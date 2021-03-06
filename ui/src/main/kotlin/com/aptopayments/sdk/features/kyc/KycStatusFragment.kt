package com.aptopayments.sdk.features.kyc

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.parseHtmlLinks
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_kyc_status.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val KYC_STATUS_PARAMETER_KEY = "kyc_status"
private const val CARD_ID_PARAMETER_KEY = "card_id"

internal class KycStatusFragment : BaseFragment(), KycStatusContract.View {

    override fun layoutId() = R.layout.fragment_kyc_status
    private val viewModel: KycStatusViewModel by viewModel()
    private lateinit var kycStatus: KycStatus
    private lateinit var cardId: String
    override var delegate: KycStatusContract.Delegate? = null

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        kycStatus = requireArguments()[KYC_STATUS_PARAMETER_KEY] as KycStatus
        cardId = requireArguments()[CARD_ID_PARAMETER_KEY] as String
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(kycStatus, ::handleKycStatus)
            observe(failure) { handleFailure(it) }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
        }
    }

    override fun onBackPressed() {
        delegate?.onKycClosed()
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
    }

    override fun setupListeners() {
        super.setupListeners()
        refresh_button.setOnClickListenerSafe {
            viewModel.getKycStatus(cardId)
        }
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeLargeTitleLabel(tv_kyc_title)
            customizeRegularTextLabel(tv_status_text)
            customizeSubmitButton(refresh_button)
            customizeFormTextLink(tv_kyc_footer)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        tv_kyc_footer.text = "manage_card_kyc_footer".localized().parseHtmlLinks()
        tv_kyc_footer.movementMethod = LinkMovementMethod.getInstance()
        updateKycLabel()
    }

    private fun handleKycStatus(kycStatus: KycStatus?) {
        kycStatus?.let {
            if (it == KycStatus.PASSED) {
                delegate?.onKycPassed()
            } else {
                this.kycStatus = it
                updateKycLabel()
            }
        }
    }

    private fun updateKycLabel() {
        val text = when (kycStatus) {
            KycStatus.REJECTED -> "manage_card.kyc.state.rejected"
            KycStatus.RESUBMIT_DETAILS -> "manage_card.kyc.state.resubmit_details"
            KycStatus.PASSED -> "manage_card.kyc.state.passed"
            KycStatus.UPLOAD_FILE -> "manage_card.kyc.state.upload_file"
            KycStatus.UNKNOWN -> "manage_card.kyc.state.under_review"
            KycStatus.UNDER_REVIEW -> "manage_card.kyc.state.under_review"
            KycStatus.TEMPORARY_ERROR -> "manage_card.kyc.state.temporary_error"
        }
        tv_status_text.localizedText = text
    }

    companion object {
        fun newInstance(kycStatus: KycStatus, cardID: String) = KycStatusFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KYC_STATUS_PARAMETER_KEY, kycStatus)
                putSerializable(CARD_ID_PARAMETER_KEY, cardID)
            }
        }
    }
}

package com.aptopayments.sdk.features.kyc

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.KycStatus
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.StringUtils
import kotlinx.android.synthetic.main.fragment_kyc_status_theme_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val KYC_STATUS_PARAMETER_KEY = "kyc_status"
private const val CARD_ID_PARAMETER_KEY = "card_id"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class KycStatusFragmentThemeTwo: BaseFragment(), KycStatusContract.View {

    override fun layoutId() = R.layout.fragment_kyc_status_theme_two
    private val viewModel: KycStatusViewModel by viewModel()
    private lateinit var kycStatus: KycStatus
    private lateinit var cardId: String
    override var delegate: KycStatusContract.Delegate? = null

    override fun setUpArguments() {
        kycStatus = arguments!![KYC_STATUS_PARAMETER_KEY] as KycStatus
        cardId = arguments!![CARD_ID_PARAMETER_KEY] as String
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(kycStatus, ::handleKycStatus)
            failure(failure) { handleFailure(it) }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        delegate?.onKycClosed()
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
    }

    override fun setupListeners() {
        super.setupListeners()
        refresh_button.setOnClickListener {
            viewModel.getKycStatus(cardId)
        }
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeLargeTitleLabel(tv_kyc_title)
            customizeRegularTextLabel(tv_status_text)
            customizeSubmitButton(refresh_button)
            customizeFormTextLink(tv_kyc_footer)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        tv_kyc_title.text = "manage_card.kyc.title".localized()
        tv_kyc_footer.text = StringUtils.parseHtmlLinks("manage_card_kyc_footer".localized())
        tv_kyc_footer.movementMethod = LinkMovementMethod.getInstance()
        refresh_button.text = "manage_card.kyc.call_to_action.title".localized()
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

    @SuppressLint("SetTextI18n")
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
        tv_status_text.text = text.localized()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(kycStatus: KycStatus, cardID: String) = KycStatusFragmentThemeTwo().apply {
            arguments = Bundle().apply {
                putSerializable(KYC_STATUS_PARAMETER_KEY, kycStatus)
                putSerializable(CARD_ID_PARAMETER_KEY, cardID)
            }
        }
    }
}

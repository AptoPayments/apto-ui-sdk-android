package com.aptopayments.sdk.features.card.activatephysicalcard.success

import android.os.Bundle
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.FeatureStatus
import com.aptopayments.mobile.data.card.FeatureType
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_activate_physical_card_success.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CARD_KEY = "CARD"

internal class ActivatePhysicalCardSuccessFragment : BaseFragment(), ActivatePhysicalCardSuccessContract.View {

    private val viewModel: ActivatePhysicalCardSuccessViewModel by viewModel()
    private lateinit var card: Card
    override var delegate: ActivatePhysicalCardSuccessContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_activate_physical_card_success

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        card = requireArguments()[CARD_KEY] as Card
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(getPINFinished) { delegate?.getPinFinished() }
            failure(failure) { handleFailure(it) }
        }
    }

    override fun handleFailure(failure: Failure?) {
        when (failure) {
            is NoTelephonyError -> {
                notify("error_telephony_disabled".localized())
            }
            else -> super.handleFailure(failure)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.viewResumed()
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
    }

    override fun setupUI() {
        setupTheme()
        setupToolBar()
        setupFooter()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_title)
            customizeRegularTextLabel(tv_description)
            customizeFooterLabel(tv_footer)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolBar() = tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().build())

    override fun setupListeners() {
        super.setupListeners()
        when {
            card.features?.setPin?.status == FeatureStatus.ENABLED -> continue_button.setOnClickListenerSafe {
                delegate?.onSetPinClicked()
            }
            card.features?.getPin?.status == FeatureStatus.ENABLED -> continue_button.setOnClickListenerSafe {
                card.features?.getPin?.type?.let {
                    when (it) {
                        is FeatureType.Voip -> delegate?.onGetPinViaVoipClicked()
                        is FeatureType.Ivr -> activity?.let { activity ->
                            viewModel.getPinTapped(from = activity, phoneNumber = it.ivrPhone)
                        }
                        is FeatureType.Api -> TODO() // Not supported yet
                        is FeatureType.Unknown -> activity?.let {
                            notify("failure_server_error".localized())
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        delegate?.onCloseFromActivatePhysicalCardSuccess()
    }

    private fun setupFooter() {
        if (card.features?.getPin?.status == FeatureStatus.ENABLED) tv_footer.show()
        else tv_footer.remove()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(card: Card) = ActivatePhysicalCardSuccessFragment().apply {
            arguments = Bundle().apply { putSerializable(CARD_KEY, card) }
        }
    }
}

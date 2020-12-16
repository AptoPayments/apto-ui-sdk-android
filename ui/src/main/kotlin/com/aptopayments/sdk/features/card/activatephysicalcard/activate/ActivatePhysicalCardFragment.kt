package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import android.os.Bundle
import com.aptopayments.mobile.data.card.ActivatePhysicalCardResult
import com.aptopayments.mobile.data.card.ActivatePhysicalCardResultType
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_activate_physical_card.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val PIN_CHARACTERS = 6
private const val CARD_KEY = "CARD"

internal class ActivatePhysicalCardFragment : BaseFragment(), ActivatePhysicalCardContract.View {

    private val viewModel: ActivatePhysicalCardViewModel by viewModel()
    private lateinit var card: Card
    override var delegate: ActivatePhysicalCardContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_activate_physical_card

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        card = requireArguments()[CARD_KEY] as Card
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(failure) { handleFailure(it) }
        }
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        apto_pin_view.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        setupTheme()
        setupToolBar()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_physical_activation_title)
            customizeRegularTextLabel(tv_physical_activation_explanation)
        }
    }

    private fun setupToolBar() = tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().build())

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) {
                    activateCard()
                }
            }
        }
        apto_pin_view.addTextChangedListener(TextInputWatcher(validator, PIN_CHARACTERS, apto_pin_view))
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromActivatePhysicalCard()
    }

    private fun activateCard() {
        hideKeyboard()
        showLoading()
        viewModel.activatePhysicalCard(card.accountID, apto_pin_view.text.toString()) { result ->
            hideLoading()
            when (result?.result) {
                ActivatePhysicalCardResultType.ACTIVATED -> delegate?.onPhysicalCardActivated()
                ActivatePhysicalCardResultType.ERROR -> showError(result)
            }
        }
    }

    private fun showError(result: ActivatePhysicalCardResult) = result.errorCode?.toInt().let { errorCode ->
        val title = "banner_error_title".localized()
        val message = Failure.ServerError(errorCode).errorMessage()
        notify(title = title, message = message)
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(card: Card) = ActivatePhysicalCardFragment().apply {
            arguments = Bundle().apply { putSerializable(CARD_KEY, card) }
        }
    }
}

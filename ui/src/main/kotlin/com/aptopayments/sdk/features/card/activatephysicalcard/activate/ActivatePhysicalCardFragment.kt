package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import android.os.Bundle
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardViewModel.Action
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_activate_physical_card.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val PIN_CHARACTERS = 6
private const val CARD_KEY = "CARD"

internal class ActivatePhysicalCardFragment : BaseFragment(), ActivatePhysicalCardContract.View {

    private val viewModel: ActivatePhysicalCardViewModel by viewModel { parametersOf(card.accountID) }
    private lateinit var card: Card
    override var delegate: ActivatePhysicalCardContract.Delegate? = null
    private lateinit var watcher: TextInputWatcher

    override fun layoutId(): Int = R.layout.fragment_activate_physical_card

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        card = requireArguments()[CARD_KEY] as Card
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(failure) { handleFailure(it) }
            observeNotNullable(loading) { handleLoading(it) }
            observeNotNullable(action) {
                when (it) {
                    is Action.Activated -> {
                        hideKeyboard()
                        delegate?.onPhysicalCardActivated()
                    }
                    is Action.Error -> showError(it.failure)
                }
            }
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

    private fun setupToolBar() =
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().setSecondaryColors().build())

    override fun setupListeners() {
        super.setupListeners()
        watcher = createWatcher()
        apto_pin_view.addTextChangedListener(watcher)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        apto_pin_view.removeTextChangedListener(watcher)
    }

    private fun createWatcher(): TextInputWatcher {
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) {
                    activateCard()
                }
            }
        }
        return TextInputWatcher(validator, PIN_CHARACTERS, apto_pin_view)
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromActivatePhysicalCard()
    }

    private fun activateCard() {
        viewModel.activatePhysicalCard(apto_pin_view.text.toString())
    }

    private fun showError(failure: Failure) {
        val title = "banner_error_title".localized()
        cleartext()
        notify(title = title, message = failure.errorMessage())
    }

    private fun cleartext() {
        apto_pin_view.removeTextChangedListener(watcher)
        apto_pin_view.text?.clear()
        apto_pin_view.addTextChangedListener(watcher)
        apto_pin_view.requestFocus()
    }

    companion object {
        fun newInstance(card: Card) = ActivatePhysicalCardFragment().apply {
            arguments = Bundle().apply { putSerializable(CARD_KEY, card) }
        }
    }
}

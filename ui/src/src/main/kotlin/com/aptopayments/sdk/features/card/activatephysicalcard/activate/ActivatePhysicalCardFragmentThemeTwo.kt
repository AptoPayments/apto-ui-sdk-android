package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.ActivatePhysicalCardResult
import com.aptopayments.core.data.card.ActivatePhysicalCardResultType
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_activate_physical_card_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import java.lang.reflect.Modifier

private const val PIN_CHARACTERS = 6
private const val CARD_KEY = "CARD"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class ActivatePhysicalCardFragmentThemeTwo : BaseFragment(), ActivatePhysicalCardContract.View {

    private lateinit var mViewModel: ActivatePhysicalCardViewModel
    private lateinit var card: Card
    override var delegate: ActivatePhysicalCardContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_activate_physical_card_theme_two

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        card = arguments!![CARD_KEY] as Card
    }

    override fun setupViewModel() {
        mViewModel = viewModel(viewModelFactory) {
            failure(failure) {
                handleFailure(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
        apto_pin_view.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        setupTexts()
        setupTheme()
        setupToolBar()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        context?.let {
            tv_physical_activation_title.text = "manage_card.activate_physical_card_overlay.title".localized(it)
            tv_physical_activation_explanation.text = "manage_card.activate_physical_card_overlay.message".localized(it)
        }
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_physical_activation_title)
            customizeRegularTextLabel(tv_physical_activation_explanation)
        }
    }

    private fun setupToolBar() {
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = null,
                backButtonMode = BaseActivity.BackButtonMode.Back(null)
        )
    }

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) { if (isValid) activateCard() }
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
        mViewModel.activatePhysicalCard(card.accountID, apto_pin_view.text.toString()) { result ->
            hideLoading()
            when (result?.result) {
                ActivatePhysicalCardResultType.ACTIVATED -> delegate?.onPhysicalCardActivated()
                ActivatePhysicalCardResultType.ERROR -> { showError(result) }
            }
        }
    }

    private fun showError(result: ActivatePhysicalCardResult) {
        result.errorCode?.toInt().let { errorCode ->
            context?.let { context ->
                val title = "banner_error_title".localized(context)
                val message = Failure.ServerError(errorCode).errorMessage(context)
                notify(title = title, message = message)
            }
        }
    }

    override fun viewLoaded() {
        mViewModel.viewLoaded()
    }

    companion object {
        fun newInstance(card: Card) = ActivatePhysicalCardFragmentThemeTwo().apply {
            arguments = Bundle().apply { putSerializable(CARD_KEY, card) }
        }
    }
}

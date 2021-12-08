package com.aptopayments.sdk.features.card.setpin

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentSetPinBinding
import com.google.android.material.appbar.AppBarLayout

internal abstract class CardPinFragment : BaseDataBindingFragment<FragmentSetPinBinding>() {

    protected abstract val viewModel: CardPinViewModel

    override fun layoutId() = R.layout.fragment_set_pin

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(failure) { handleFailure(it) }
            observeNotNullable(loading) { handleLoading(it) }
            observe(action) {
                when (it) {
                    is CardPinViewModel.Action.WrongPin -> wrongPin()
                    is CardPinViewModel.Action.CorrectPin -> correctPin(it.pin)
                }
            }
        }
    }

    abstract fun wrongPin()

    abstract fun correctPin(pin: String)

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        binding.pinView.requestFocus()
        binding.pinView.text?.clear()
        showKeyboard()
    }

    override fun setupUI() {
        setupToolBar()
        setupTheme()
        binding.pinView.doAfterTextChanged { viewModel.setPin(it.toString()) }
    }

    private fun setupToolBar() =
        binding.tbLlsdkToolbarLayout.tbLlsdkToolbar.configure(
            this,
            ToolbarConfiguration.Builder().setSecondaryColors().build()
        )

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout as AppBarLayout)
            customizeLargeTitleLabel(binding.tvSetPinTitle)
            customizeRegularTextLabel(binding.tvSetPinExplanation)
        }
    }
}

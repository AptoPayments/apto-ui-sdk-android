package com.aptopayments.sdk.features.card.activatephysicalcard.success

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.FeatureStatus
import com.aptopayments.core.data.card.FeatureType
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_activate_physical_card_success_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val CARD_KEY = "CARD"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class ActivatePhysicalCardSuccessFragmentThemeTwo : BaseFragment(), ActivatePhysicalCardSuccessContract.View {

    private val viewModel: ActivatePhysicalCardSuccessViewModel by viewModel()
    private lateinit var card: Card
    override var delegate: ActivatePhysicalCardSuccessContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_activate_physical_card_success_theme_two

    override fun setUpArguments() {
        card = arguments!![CARD_KEY] as Card
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(getPINFinished) { delegate?.getPinFinished() }
            failure(failure) { handleFailure(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
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
        delegate?.configureStatusBar()
    }

    override fun setupUI() {
        setupTexts()
        setupTheme()
        setupToolBar()
        setupFooter()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        tv_title.text = "manage_card.get_pin_nue.title".localized()
        tv_description.text = "manage_card.get_pin_nue.explanation".localized()
        continue_button.text = "manage_card.get_pin_nue.call_to_action.title".localized()
        tv_footer.text = "manage_card.get_pin_nue.footer".localized()
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_title)
            customizeRegularTextLabel(tv_description)
            customizeFooterLabel(tv_footer)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolBar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Close(null)
    )

    override fun setupListeners() {
        super.setupListeners()
        when {
            card.features?.setPin?.status == FeatureStatus.ENABLED -> continue_button.setOnClickListener {
                delegate?.onSetPinClicked()
            }
            card.features?.getPin?.status == FeatureStatus.ENABLED -> continue_button.setOnClickListener {
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
        fun newInstance(card: Card) = ActivatePhysicalCardSuccessFragmentThemeTwo().apply {
            arguments = Bundle().apply { putSerializable(CARD_KEY, card) }
        }
    }
}

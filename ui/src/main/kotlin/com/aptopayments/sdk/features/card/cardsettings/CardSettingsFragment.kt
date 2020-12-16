package com.aptopayments.sdk.features.card.cardsettings

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.FeatureType.*
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.voip.Action.LISTEN_PIN
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCardSettingsBinding
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.ui.views.AuthenticationView
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleView
import com.aptopayments.sdk.ui.views.SectionSwitchViewTwo
import com.aptopayments.sdk.utils.deeplinks.InAppProvisioningDeepLinkGenerator
import com.aptopayments.sdk.utils.deeplinks.IntentGenerator
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_card_settings.*
import kotlinx.android.synthetic.main.include_custom_toolbar.*
import kotlinx.android.synthetic.main.view_section_switch_two.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

private const val CARD_KEY = "CARD"
private const val CARD_PRODUCT_KEY = "CARD_PRODUCT"
private const val PROJECT_CONFIGURATION_KEY = "PROJECT_CONFIGURATION"

internal class CardSettingsFragment :
    BaseBindingFragment<FragmentCardSettingsBinding>(),
    CardSettingsContract.View {

    private lateinit var card: Card
    private lateinit var cardProduct: CardProduct
    private lateinit var projectConfiguration: ProjectConfiguration
    private val viewModel: CardSettingsViewModel by viewModel { parametersOf(card, cardProduct) }
    private val intentGenerator: IntentGenerator by inject()
    private val iAPDeepLinkGenerator: InAppProvisioningDeepLinkGenerator by inject()
    override var delegate: CardSettingsContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_card_settings

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun setUpArguments() {
        card = requireArguments()[CARD_KEY] as Card
        cardProduct = requireArguments()[CARD_PRODUCT_KEY] as CardProduct
        projectConfiguration = requireArguments()[PROJECT_CONFIGURATION_KEY] as ProjectConfiguration
    }

    override fun onPresented() {
        super.onPresented()
        customizeSecondaryNavigationStatusBar()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(cardLocked, ::handleCardLocked)
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observe(viewModel.failure) { handleFailure(it) }
            observeNotNullable(cardDetailsClicked) { manageCardDetails() }
            observeNotNullable(authenticateCardDetails) { value -> handleAuthenticateCardDetails(value) }
            observeNotNullable(showContentPresenter) { showContentPresenter(it.first, it.second) }
        }
    }

    private fun handleAuthenticateCardDetails(authenticationNeeded: Boolean) {
        if (authenticationNeeded) {
            (activity as CardActivity).authenticate(
                AuthenticationView.AuthType.OPTIONAL,
                onCancelled = { viewModel.cardDetailsAuthenticationError() },
                onAuthenticated = { viewModel.cardDetailsAuthenticationSuccessful() }
            )
        }
    }

    private fun manageCardDetails() {
        onBackPressed()
    }

    private fun handleCardLocked(value: Boolean?) {
        if (rl_lock_card.sw_tv_section_switch_switch.isChecked != value) {
            silentlyToggleSwitch(rl_lock_card.sw_tv_section_switch_switch, ::lockUnlockCard)
        }
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
        setupToolBar()
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_close_button.setOnClickListenerSafe { onBackPressed() }
        rl_add_funds.setOnClickListenerSafe { addFundsPressed() }
        rl_get_pin.setOnClickListenerSafe { getPinPressed() }
        rl_set_pin.setOnClickListenerSafe { setPinPressed() }
        rl_lock_card.sw_tv_section_switch_switch.setOnCheckedChangeListener { _, value ->
            lockUnlockCard(value)
        }
        rl_detailed_card_activity.sw_tv_section_switch_switch.setOnCheckedChangeListener { _, value ->
            storeDetailedCardActivityPreference(value)
        }
        rl_contact_support.setOnClickListenerSafe { sendCustomerSupportEmail() }
        rl_report_stolen_card.setOnClickListenerSafe { reportLostOrStolenCard() }
        rl_ivr_support.setOnClickListenerSafe { callIvrSupport() }
        rl_statement.setOnClickListenerSafe { onStatementPressed() }
        rl_google_pay.setOnClickListenerSafe { onGooglePayPressed() }
    }

    private fun setupTheme() {
        themeManager().customizeToolbarTitle(tv_toolbar_title)
        (rl_lock_card as SectionSwitchViewTwo).hideBottomSeparator()
        if (AptoUiSdk.cardOptions.showDetailedCardActivityOption()) {
            transactions_section.show()
            rl_detailed_card_activity.sw_tv_section_switch_switch.isChecked = getDetailedCardActivityPreference()
        }
        if (!AptoUiSdk.cardOptions.showMonthlyStatementOption()) {
            rl_statement.remove()
            (rl_faq as SectionOptionWithSubtitleView).optionShowDivider = false
        }
    }

    private fun setupTexts() {
        tv_toolbar_title.localizedText = "card_settings.settings.title"

        (rl_lock_card as SectionSwitchViewTwo).set(
            title = "card_settings.settings.lock_card.title".localized(),
            description = "card_settings.settings.lock_card.description".localized()
        )
        (rl_detailed_card_activity as SectionSwitchViewTwo).set(
            title = "card_settings.transactions.detailed_card_activity.title".localized(),
            description = "card_settings.transactions.detailed_card_activity.description".localized()
        )
    }

    private fun setupToolBar() {
        val toolbar = tb_llsdk_custom_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_close_button.setColorFilter(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
    }

    override fun onBackPressed() {
        delegate?.onBackFromCardSettings()
    }

    private fun addFundsPressed() {
        delegate?.onAddFunds()
    }

    private fun getPinPressed() {
        when (val type = card.features?.getPin?.type) {
            is Voip -> delegate?.showVoip(action = LISTEN_PIN)
            is Ivr -> {
                context?.let { context ->
                    type.ivrPhone?.let {
                        viewModel.dial(phone = it, from = context)
                    }
                }
            }
            // Unsupported get pin actions
            is Api -> {
            }
            is Unknown -> {
            }
        }
    }

    private fun setPinPressed() = delegate?.onSetPin()

    private fun callIvrSupport() = context?.let { context ->
        card.features?.ivrSupport?.ivrPhone?.let { phoneNUmber ->
            viewModel.dial(phone = phoneNUmber, from = context)
        }
    }

    private fun onStatementPressed() {
        delegate?.showStatement()
    }

    private fun lockUnlockCard(value: Boolean) {
        if (value) {
            confirm(
                title = "card_settings.settings.confirm_lock_card.title".localized(),
                text = "card_settings.settings.confirm_lock_card.message".localized(),
                confirm = "card_settings.settings.confirm_lock_card.ok_button".localized(),
                cancel = "card_settings.settings.confirm_lock_card.cancel_button".localized(),
                onConfirm = {
                    showLoading()
                    viewModel.lockCard {
                        hideLoading()
                        delegate?.onCardStateChanged()
                    }
                },
                onCancel = { silentlyToggleSwitch(rl_lock_card.sw_tv_section_switch_switch, ::lockUnlockCard) }
            )
        } else {
            confirm(
                title = "card_settings.settings.confirm_unlock_card.title".localized(),
                text = "card_settings.settings.confirm_unlock_card.message".localized(),
                confirm = "card_settings.settings.confirm_unlock_card.ok_button".localized(),
                cancel = "card_settings.settings.confirm_unlock_card.cancel_button".localized(),
                onConfirm = {
                    showLoading()
                    viewModel.unlockCard {
                        hideLoading()
                        delegate?.onCardStateChanged()
                    }
                },
                onCancel = { silentlyToggleSwitch(rl_lock_card.sw_tv_section_switch_switch, ::lockUnlockCard) }
            )
        }
    }

    private fun silentlyToggleSwitch(switch: Switch, listener: (value: Boolean) -> Unit) {
        switch.setOnCheckedChangeListener(null)
        switch.toggle()
        switch.setOnCheckedChangeListener { _, value -> listener(value) }
    }

    private fun showContentPresenter(content: Content, titleToLocalize: String) {
        delegate?.showContentPresenter(content, titleToLocalize.localized())
    }

    private fun sendCustomerSupportEmail() {
        projectConfiguration.supportEmailAddress?.let { recipientAddress ->
            delegate?.showMailComposer(
                recipient = recipientAddress,
                subject = "help.mail.subject".localized(),
                body = "help.mail.body".localized()
            )
        }
    }

    private fun reportLostOrStolenCard() {
        confirm(
            title = "card_settings.settings.confirm_report_lost_card.title".localized(),
            text = "card_settings.settings.confirm_report_lost_card.message".localized(),
            confirm = "card_settings.settings.confirm_report_lost_card.ok_button".localized(),
            cancel = "card_settings.settings.confirm_report_lost_card.cancel_button".localized(),
            onConfirm = {
                viewModel.lockCard { }
                projectConfiguration.supportEmailAddress?.let { recipientAddress ->
                    delegate?.showMailComposer(
                        recipient = recipientAddress,
                        subject = "email_lost_card_subject".localized(),
                        body = null
                    )
                }
            },
            onCancel = { }
        )
    }

    private fun onGooglePayPressed() {
        activity?.let {
            iAPDeepLinkGenerator.setCardId(card.accountID)
            val intent = intentGenerator.invoke(iAPDeepLinkGenerator)
            it.startActivity(intent)
        }
    }

    private fun getDetailedCardActivityPreference() = aptoPlatformProtocol.isShowDetailedCardActivityEnabled()

    private fun storeDetailedCardActivityPreference(show: Boolean) {
        aptoPlatformProtocol.setIsShowDetailedCardActivityEnabled(show)
        delegate?.transactionsChanged()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(card: Card, cardProduct: CardProduct, projectConfiguration: ProjectConfiguration) =
            CardSettingsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CARD_KEY, card)
                    putSerializable(CARD_PRODUCT_KEY, cardProduct)
                    putSerializable(PROJECT_CONFIGURATION_KEY, projectConfiguration)
                }
            }
    }
}

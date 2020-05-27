package com.aptopayments.sdk.features.card.cardsettings

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.widget.Toolbar
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.FeatureType.*
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.config.ProjectConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.data.voip.Action.LISTEN_PIN
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.ui.views.AuthenticationView
import com.aptopayments.sdk.ui.views.SectionHeaderViewTwo
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleViewTwo
import com.aptopayments.sdk.ui.views.SectionSwitchViewTwo
import com.aptopayments.sdk.utils.deeplinks.InAppProvisioningDeepLinkGenerator
import com.aptopayments.sdk.utils.deeplinks.IntentGenerator
import kotlinx.android.synthetic.main.fragment_card_settings_theme_two.*
import kotlinx.android.synthetic.main.include_custom_toolbar_two.*
import kotlinx.android.synthetic.main.view_section_switch_two.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

private const val CARD_KEY = "CARD"
private const val CARD_PRODUCT_KEY = "CARD_PRODUCT"
private const val PROJECT_CONFIGURATION_KEY = "PROJECT_CONFIGURATION"

internal class CardSettingsFragmentThemeTwo : BaseFragment(), CardSettingsContract.View {

    private lateinit var card: Card
    private lateinit var cardProduct: CardProduct
    private lateinit var projectConfiguration: ProjectConfiguration
    private val viewModel: CardSettingsViewModel by viewModel { parametersOf(card, cardProduct) }
    private val intentGenerator: IntentGenerator by inject()
    private val iAPDeepLinkGenerator: InAppProvisioningDeepLinkGenerator by inject()
    override var delegate: CardSettingsContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_card_settings_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        card = arguments!![CARD_KEY] as Card
        cardProduct = arguments!![CARD_PRODUCT_KEY] as CardProduct
        projectConfiguration = arguments!![PROJECT_CONFIGURATION_KEY] as ProjectConfiguration
    }

    override fun onPresented() {
        super.onPresented()
        customizeSecondaryNavigationStatusBar()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(showGetPin, ::handleShowGetPin)
            observe(showSetPin, ::handleShowSetPin)
            observe(cardLocked, ::handleCardLocked)
            observe(showIvrSupport, ::handleShowIvrSupport)
            observe(faq, ::handleFaq)
            observeThree(cardholderAgreement, privacyPolicy, termsAndConditions, ::handleLegalSectionVisibility)
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            failure(viewModel.failure) { handleFailure(it) }
            observeNotNullable(cardDetailsFetchedCorrectly) { manageCardDetailsFetched(it) }
            observeNotNullable(hasCardDetails) { value -> silentlySetCardDetailSwitchValue(value) }
            observeNotNullable(authenticateCardDetails) { value -> handleAuthenticateCardDetails(value) }
        }
        viewModel.viewResumed()
    }

    private fun handleAuthenticateCardDetails(authenticationNeeded: Boolean) {
        if (authenticationNeeded) {
            (activity as CardActivity).authenticate(AuthenticationView.AuthType.OPTIONAL,
                onCancelled = { viewModel.cardDetailsAuthenticationCancelled() },
                onAuthenticated = { viewModel.cardDetailsAuthenticationSuccessful() })
        }
    }

    private fun manageCardDetailsFetched(fetched: Boolean) {
        if (fetched) {
            onBackPressed()
        } else {
            silentlySetCardDetailSwitchValue(false)
        }
    }

    private fun silentlySetCardDetailSwitchValue(value: Boolean) {
        silentlySetSwitch(rl_card_info.sw_tv_section_switch_switch, value) { cardDetailsTapped(it) }
    }

    private fun handleShowIvrSupport(value: Boolean?) =
        if (value == true) rl_ivr_support.show() else rl_ivr_support.remove()

    private fun handleShowGetPin(value: Boolean?) = if (value == true) rl_get_pin.show() else rl_get_pin.remove()

    private fun handleShowSetPin(value: Boolean?) = if (value == true) rl_set_pin.show() else rl_set_pin.remove()

    private fun handleCardLocked(value: Boolean?) {
        if (rl_lock_card.sw_tv_section_switch_switch.isChecked != value) {
            silentlyToggleSwitch(rl_lock_card.sw_tv_section_switch_switch, ::lockUnlockCard)
        }
    }

    private fun handleFaq(faq: Content?) {
        faq?.let { rl_faq.show() } ?: run { rl_faq.remove() }
    }

    private fun handleLegalSectionVisibility(
        cardholderAgreement: Content?,
        privacyPolicy: Content?,
        termsAndConditions: Content?
    ) {
        if (cardholderAgreement == null && privacyPolicy == null && termsAndConditions == null) {
            rl_legal.hide()
        } else {
            rl_legal.show()
        }
        cardholderAgreement?.let { rl_cardholder_agreement.show() } ?: run { rl_cardholder_agreement.remove() }
        privacyPolicy?.let { rl_privacy_policy.show() } ?: run { rl_privacy_policy.remove() }
        termsAndConditions?.let { rl_terms_of_service.show() } ?: run { rl_terms_of_service.remove() }
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
        setupToolBar()
        rl_google_pay.visibleIf(viewModel.showAddToGooglePay)
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_close_button.setOnClickListener { onBackPressed() }
        rl_get_pin.setOnClickListener { getPinPressed() }
        rl_set_pin.setOnClickListener { setPinPressed() }
        rl_card_info.sw_tv_section_switch_switch.setOnCheckedChangeListener { _, value ->
            cardDetailsTapped(value)
        }
        rl_lock_card.sw_tv_section_switch_switch.setOnCheckedChangeListener { _, value ->
            lockUnlockCard(value)
        }
        rl_detailed_card_activity.sw_tv_section_switch_switch.setOnCheckedChangeListener { _, value ->
            storeDetailedCardActivityPreference(value)
        }
        rl_faq.setOnClickListener { onFaqPressed() }
        rl_cardholder_agreement.setOnClickListener { onCardholderAgreementPressed() }
        rl_privacy_policy.setOnClickListener { onPrivacyPolicyPressed() }
        rl_terms_of_service.setOnClickListener { onTermsAndConditionsPressed() }
        rl_contact_support.setOnClickListener { sendCustomerSupportEmail() }
        rl_report_stolen_card.setOnClickListener { reportLostOrStolenCard() }
        rl_ivr_support.setOnClickListener { callIvrSupport() }
        rl_statement.setOnClickListener { onStatementPressed() }
        rl_google_pay.setOnClickListener { onGooglePayPressed() }
    }

    private fun setupTheme() {
        themeManager().customizeToolbarTitle(tv_toolbar_title)
        (rl_statement as SectionOptionWithSubtitleViewTwo).hideBottomSeparator()
        (rl_lock_card as SectionSwitchViewTwo).hideBottomSeparator()
        (rl_terms_of_service as SectionOptionWithSubtitleViewTwo).hideBottomSeparator()
        if (AptoUiSdk.cardOptions.showDetailedCardActivityOption()) {
            transactions_section.show()
            rl_detailed_card_activity.sw_tv_section_switch_switch.isChecked = getDetailedCardActivityPreference()
        }
        if (!AptoUiSdk.cardOptions.showMonthlyStatementOption()) {
            rl_statement.remove()
            (rl_faq as SectionOptionWithSubtitleViewTwo).hideBottomSeparator()
        }
    }

    private fun setupTexts() {
        tv_toolbar_title.localizedText = "card_settings.settings.title"
        (rl_settings as SectionHeaderViewTwo).set(
            title = "card_settings.settings.settings.title".localized()
        )
        (rl_get_pin as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.settings.get_pin.title".localized(),
            description = "card_settings.settings.get_pin.description".localized()
        )
        (rl_set_pin as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.settings.set_pin.title".localized(),
            description = "card_settings.settings.set_pin.description".localized()
        )
        (rl_card_info as SectionSwitchViewTwo).set(
            title = "card_settings.settings.card_details.title".localized(),
            description = "card_settings.settings.card_details.description".localized()
        )
        (rl_lock_card as SectionSwitchViewTwo).set(
            title = "card_settings.settings.lock_card.title".localized(),
            description = "card_settings.settings.lock_card.description".localized()
        )
        (rl_transactions as SectionHeaderViewTwo).set(
            title = "card_settings.transactions.title".localized()
        )
        (rl_detailed_card_activity as SectionSwitchViewTwo).set(
            title = "card_settings.transactions.detailed_card_activity.title".localized(),
            description = "card_settings.transactions.detailed_card_activity.description".localized()
        )
        (rl_help as SectionHeaderViewTwo).set(
            title = "card_settings.help.title".localized()
        )
        (rl_ivr_support as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.help.ivr_support.title".localized(),
            description = "card_settings.help.ivr_support.description".localized()
        )
        (rl_contact_support as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.help.contact_support.title".localized(),
            description = "card_settings.help.contact_support.description".localized()
        )
        (rl_report_stolen_card as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.help.report_lost_card.title".localized(),
            description = "card_settings.help.report_lost_card.description".localized()
        )
        (rl_faq as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.legal.faq.title".localized(),
            description = "card_settings.legal.faq.description".localized()
        )
        (rl_legal as SectionHeaderViewTwo).set(
            title = "card_settings.legal.title".localized()
        )
        (rl_cardholder_agreement as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.legal.cardholder_agreement.title".localized(),
            description = "card_settings.legal.cardholder_agreement.description".localized()
        )
        (rl_privacy_policy as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.legal.privacy_policy.title".localized(),
            description = "card_settings.legal.privacy_policy.description".localized()
        )
        (rl_terms_of_service as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.legal.terms_of_service.title".localized(),
            description = "card_settings.legal.terms_of_service.description".localized()
        )
        (rl_statement as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings.help.monthly_statements.title".localized(),
            description = "card_settings.help.monthly_statements.description".localized()
        )
        (rl_google_pay as SectionOptionWithSubtitleViewTwo).set(
            title = "card_settings_google_pay_provisioning_title".localized(),
            description = "card_settings_google_pay_provisioning_subtitle".localized()
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

    private fun silentlySetSwitch(switch: Switch, isChecked: Boolean, listener: (value: Boolean) -> Unit) {
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = isChecked
        switch.setOnCheckedChangeListener { _, value -> listener(value) }
    }

    private fun onFaqPressed() {
        showContentPresenter(viewModel.faq.value, "card_settings.legal.faq.title")
    }

    private fun onCardholderAgreementPressed() {
        showContentPresenter(viewModel.cardholderAgreement.value, "card_settings.legal.cardholder_agreement.title")
    }

    private fun onPrivacyPolicyPressed() {
        showContentPresenter(viewModel.privacyPolicy.value, "card_settings.legal.privacy_policy.title")
    }

    private fun onTermsAndConditionsPressed() {
        showContentPresenter(viewModel.termsAndConditions.value, "card_settings.legal.terms_of_service.title")
    }

    private fun showContentPresenter(content: Content?, titleToLocalize: String) {
        content?.let {
            delegate?.showContentPresenter(it, titleToLocalize.localized())
        }
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

    private fun cardDetailsTapped(value: Boolean) {
        viewModel.cardDetailsTapped(value)
    }

    companion object {
        fun newInstance(card: Card, cardProduct: CardProduct, projectConfiguration: ProjectConfiguration) =
            CardSettingsFragmentThemeTwo().apply {
                arguments = Bundle().apply {
                    putSerializable(CARD_KEY, card)
                    putSerializable(CARD_PRODUCT_KEY, cardProduct)
                    putSerializable(PROJECT_CONFIGURATION_KEY, projectConfiguration)
                }
            }
    }
}

package com.aptopayments.sdk.features.card.cardsettings

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Switch
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.Toolbar
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.card.FeatureType.*
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.config.ProjectConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.data.voip.Action.LISTEN_PIN
import com.aptopayments.core.extension.localized
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.repository.UserPreferencesRepository
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.SectionHeaderViewTwo
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleViewTwo
import com.aptopayments.sdk.ui.views.SectionSwitchViewTwo
import com.aptopayments.sdk.utils.BiometricAuthenticator
import com.aptopayments.sdk.utils.BiometricAvailability.*
import com.aptopayments.sdk.utils.runOnUiThreadAfter
import kotlinx.android.synthetic.main.fragment_card_settings_theme_two.*
import kotlinx.android.synthetic.main.include_custom_toolbar_two.*
import kotlinx.android.synthetic.main.view_section_switch_two.view.*
import java.lang.reflect.Modifier
import javax.inject.Inject

private const val CARD_KEY = "CARD"
private const val CARD_DETAILS_SHOWN_KEY = "CARD_DETAILS_SHOWN"
private const val CARD_PRODUCT_KEY = "CARD_PRODUCT"
private const val PROJECT_CONFIGURATION_KEY = "PROJECT_CONFIGURATION"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardSettingsFragmentThemeTwo : BaseFragment(), CardSettingsContract.View {

    private lateinit var card: Card
    private var cardDetailsShown = false
    private lateinit var cardProduct: CardProduct
    private lateinit var projectConfiguration: ProjectConfiguration
    private lateinit var viewModel: CardSettingsViewModel
    override var delegate: CardSettingsContract.Delegate? = null
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun layoutId(): Int = R.layout.fragment_card_settings_theme_two

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        card = arguments!![CARD_KEY] as Card
        cardDetailsShown = arguments!![CARD_DETAILS_SHOWN_KEY] as Boolean
        cardProduct = arguments!![CARD_PRODUCT_KEY] as CardProduct
        projectConfiguration = arguments!![PROJECT_CONFIGURATION_KEY] as ProjectConfiguration

        if (savedInstanceState != null) cardDetailsShown = savedInstanceState.getBoolean(CARD_DETAILS_SHOWN_KEY)
    }

    override fun onPresented() {
        super.onPresented()
        activity?.window?.let {
            themeManager().customizeSecondaryNavigationStatusBar(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(CARD_DETAILS_SHOWN_KEY, cardDetailsShown)
    }

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {
            observe(showGetPin, ::handleShowGetPin)
            observe(showSetPin, ::handleShowSetPin)
            observe(cardLocked, ::handleCardLocked)
            observe(cardDetailsShown, ::handleCardDetailsShown)
            observe(showIvrSupport, ::handleShowIvrSupport)
            observeNullable(cardDetails, ::handleCardDetails)
            observe(faq, ::handleFaq)
            observeThree(cardholderAgreement, privacyPolicy, termsAndConditions, ::handleLegalSectionVisibility)
            failure(failure) { handleFailure(it) }
        }
        viewModel.viewResumed(
                card = card,
                cardDetailsShown = cardDetailsShown,
                cardProduct = cardProduct)
    }

    private fun handleShowIvrSupport(value: Boolean?) {
        if (value == true) rl_ivr_support.show() else rl_ivr_support.remove()
    }

    private fun handleShowGetPin(value: Boolean?) {
        if (value == true) rl_get_pin.show() else rl_get_pin.remove()
    }

    private fun handleShowSetPin(value: Boolean?) {
        if (value == true) rl_set_pin.show() else rl_set_pin.remove()
    }

    private fun handleCardDetailsShown(value: Boolean?) {
        if (rl_card_info.sw_tv_section_switch_switch.isChecked != value) {
            silentlyToggleSwitch(rl_card_info.sw_tv_section_switch_switch, ::showHideCardDetails)
        }
        else if (value == true) {
            onBackPressed()
        }
    }

    private fun handleCardDetails(cardDetails: CardDetails?) {
        delegate?.cardDetailsChanged(cardDetails)
    }

    private fun handleCardLocked(value: Boolean?) {
        if (rl_lock_card.sw_tv_section_switch_switch.isChecked != value) {
            silentlyToggleSwitch(rl_lock_card.sw_tv_section_switch_switch, ::lockUnlockCard)
        }
    }

    private fun handleFaq(faq: Content?) {
        faq?.let { rl_faq.show() } ?: run { rl_faq.remove() }
    }

    private fun handleLegalSectionVisibility(cardholderAgreement: Content?, privacyPolicy: Content?, termsAndConditions: Content?) {
        if (cardholderAgreement==null && privacyPolicy==null && termsAndConditions==null) {
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
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_close_button.setOnClickListener { onBackPressed() }
        rl_get_pin.setOnClickListener { getPinPressed() }
        rl_set_pin.setOnClickListener { setPinPressed() }
        rl_card_info.sw_tv_section_switch_switch.setOnCheckedChangeListener { _, value ->
            showHideCardDetails(value)
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
    }

    private fun setupTheme() {
        themeManager().customizeToolbarTitle(tv_toolbar_title)
        (rl_faq as SectionOptionWithSubtitleViewTwo).hideBottomSeparator()
        (rl_lock_card as SectionSwitchViewTwo).hideBottomSeparator()
        (rl_terms_of_service as SectionOptionWithSubtitleViewTwo).hideBottomSeparator()
        if (AptoPlatform.cardOptions.showDetailedCardActivityOption()) {
            transactions_section.show()
            rl_detailed_card_activity.sw_tv_section_switch_switch.isChecked = getDetailedCardActivityPreference()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        context?.let {
            tv_toolbar_title.text = "card_settings.settings.title".localized(it)
            (rl_settings as SectionHeaderViewTwo).set(
                    title = "card_settings.settings.settings.title".localized(it)
            )
            (rl_get_pin as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.settings.get_pin.title".localized(it),
                    description = "card_settings.settings.get_pin.description".localized(it)
            )
            (rl_set_pin as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.settings.set_pin.title".localized(it),
                    description = "card_settings.settings.set_pin.description".localized(it)
            )
            (rl_card_info as SectionSwitchViewTwo).set(
                    title = "card_settings.settings.card_details.title".localized(it),
                    description = "card_settings.settings.card_details.description".localized(it)
            )
            (rl_lock_card as SectionSwitchViewTwo).set(
                    title = "card_settings.settings.lock_card.title".localized(it),
                    description = "card_settings.settings.lock_card.description".localized(it)
            )
            (rl_transactions as SectionHeaderViewTwo).set(
                    title = "card_settings.transactions.title".localized(it)
            )
            (rl_detailed_card_activity as SectionSwitchViewTwo).set(
                    title = "card_settings.transactions.detailed_card_activity.title".localized(it),
                    description = "card_settings.transactions.detailed_card_activity.description".localized(it)
            )
            (rl_help as SectionHeaderViewTwo).set(
                    title = "card_settings.help.title".localized(it)
            )
            (rl_ivr_support as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.help.ivr_support.title".localized(it),
                    description = "card_settings.help.ivr_support.description".localized(it)
            )
            (rl_contact_support as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.help.contact_support.title".localized(it),
                    description = "card_settings.help.contact_support.description".localized(it)
            )
            (rl_report_stolen_card as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.help.report_lost_card.title".localized(it),
                    description = "card_settings.help.report_lost_card.description".localized(it)
            )
            (rl_faq as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.legal.faq.title".localized(it),
                    description = "card_settings.legal.faq.description".localized(it)
            )
            (rl_legal as SectionHeaderViewTwo).set(
                    title = "card_settings.legal.title".localized(it)
            )
            (rl_cardholder_agreement as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.legal.cardholder_agreement.title".localized(it),
                    description = "card_settings.legal.cardholder_agreement.description".localized(it)
            )
            (rl_privacy_policy as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.legal.privacy_policy.title".localized(it),
                    description = "card_settings.legal.privacy_policy.description".localized(it)
            )
            (rl_terms_of_service as SectionOptionWithSubtitleViewTwo).set(
                    title = "card_settings.legal.terms_of_service.title".localized(it),
                    description = "card_settings.legal.terms_of_service.description".localized(it)
            )
        }
    }

    private fun setupToolBar() {
        val toolbar = tb_llsdk_custom_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
    }

    override fun onBackPressed() {
        delegate?.onBackFromCardSettings()
    }

    private fun getPinPressed() {
        when(val type = card.features?.getPin?.type) {
            is Voip -> delegate?.showVoip(action = LISTEN_PIN)
            is Ivr -> {
                context?.let { context ->
                    type.ivrPhone?.let {
                        viewModel.dial(phone = it, from = context)
                    }
                }
            }
            // Unsupported get pin actions
            is Api -> {}
            is Unknown -> {}
        }
    }

    private fun setPinPressed() = delegate?.onSetPin()

    private fun callIvrSupport() {
        context?.let { context ->
            card.features?.ivrSupport?.ivrPhone?.let { viewModel.dial(
                    phone = it,
                    from = context
            ) }
        }
    }

    private fun lockUnlockCard(value: Boolean) {
        context?.let { context ->
            if (value) {
                confirm(
                        title="card_settings.settings.confirm_lock_card.title".localized(context),
                        text = "card_settings.settings.confirm_lock_card.message".localized(context),
                        confirm = "card_settings.settings.confirm_lock_card.ok_button".localized(context),
                        cancel = "card_settings.settings.confirm_lock_card.cancel_button".localized(context),
                        onConfirm = {
                            showLoading()
                            viewModel.lockCard {
                                hideLoading()
                                delegate?.onCardStateChanged()
                            }
                        },
                        onCancel = { silentlyToggleSwitch(rl_lock_card.sw_tv_section_switch_switch, ::lockUnlockCard) }
                )
            }
            else {
                confirm(
                        title="card_settings.settings.confirm_unlock_card.title".localized(context),
                        text = "card_settings.settings.confirm_unlock_card.message".localized(context),
                        confirm = "card_settings.settings.confirm_unlock_card.ok_button".localized(context),
                        cancel = "card_settings.settings.confirm_unlock_card.cancel_button".localized(context),
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
    }

    private fun showHideCardDetails(value: Boolean) {
        if (!value) viewModel.hideCardDetails()
        else {
            context?.let { context ->
                val availability = BiometricAuthenticator.authAvailable(context)
                when (availability) {
                    AVAILABLE -> {
                        // Show the biometric authentication dialog here
                        delegate?.askForBiometricAuthentication(
                                title = "card_settings.show_card_data.biometrics.title".localized(context),
                                description = "card_settings.show_card_data.biometrics.description".localized(context),
                                onAuthSuccess = {
                                    showLoading()
                                    viewModel.getCardDetails {
                                        hideLoading()
                                    }
                                },
                                onAuthFailure = {
                                    notify("biometrics_fingerprint_authentication_failed".localized(context))
                                    // We need to update the state of the switch after a small delay
                                    // otherwise the Switch ends in an inconsistent state. This might
                                    // be due to previous UI operation not finished yet or because the UI
                                    // of the biometric is still shown.
                                    runOnUiThreadAfter(30, activity) {
                                        silentlyToggleSwitch(rl_card_info.sw_tv_section_switch_switch, ::showHideCardDetails)
                                    }
                                },
                                onAuthCancel = {
                                    silentlyToggleSwitch(rl_card_info.sw_tv_section_switch_switch, ::showHideCardDetails)
                                }
                        )
                    }
                    FINGERPRINT_PERMISSION_REVOKED, FINGERPRINT_NOT_CONFIGURED, LOCK_SCREEN_SECURITY_DISABLED -> {
                        notify(availability.toLocalizedDescription(context))
                        silentlyToggleSwitch(rl_card_info.sw_tv_section_switch_switch, ::showHideCardDetails)
                    }
                    NO_FINGERPRINT_SUPPORTED_IN_ANDROID_SDK, NO_FINGERPRINT_SUPPORTED_IN_DEVICE -> {
                        // No biometrics available. Just show / hide card details here
                        showLoading()
                        viewModel.getCardDetails {
                            hideLoading()
                        }
                    }
                }
            }
        }
    }

    private fun silentlyToggleSwitch(switch: Switch, listener: (value: Boolean) -> Unit) {
        switch.setOnCheckedChangeListener(null)
        switch.toggle()
        switch.setOnCheckedChangeListener { _, value -> listener(value) }
    }

    private fun onFaqPressed() {
        context?.let { context ->
            viewModel.faq.value?.let { content ->
                delegate?.showContentPresenter(
                        content = content,
                        title = "card_settings.legal.faq.title".localized(context)
                )
            }
        }
    }

    private fun onCardholderAgreementPressed() {
        context?.let { context ->
            viewModel.cardholderAgreement.value?.let { content ->
                delegate?.showContentPresenter(
                        content = content,
                        title = "card_settings.legal.cardholder_agreement.title".localized(context)
                )
            }
        }
    }

    private fun onPrivacyPolicyPressed() {
        context?.let { context ->
            viewModel.privacyPolicy.value?.let { content ->
                delegate?.showContentPresenter(
                        content = content,
                        title = "card_settings.legal.privacy_policy.title".localized(context)
                )
            }
        }
    }

    private fun onTermsAndConditionsPressed() {
        context?.let { context ->
            viewModel.termsAndConditions.value?.let { content ->
                delegate?.showContentPresenter(
                        content = content,
                        title = "card_settings.legal.terms_of_service.title".localized(context)
                )
            }
        }
    }

    private fun sendCustomerSupportEmail() {
        context?.let { context ->
            projectConfiguration.supportEmailAddress?.let { recipientAddress ->
                delegate?.showMailComposer(
                        recipient = recipientAddress,
                        subject = "help.mail.subject".localized(context),
                        body = "help.mail.body".localized(context)
                )
            }
        }
    }

    private fun reportLostOrStolenCard() {
        context?.let { context ->
            confirm(
                    title="card_settings.settings.confirm_report_lost_card.title".localized(context),
                    text = "card_settings.settings.confirm_report_lost_card.message".localized(context),
                    confirm = "card_settings.settings.confirm_report_lost_card.ok_button".localized(context),
                    cancel = "card_settings.settings.confirm_report_lost_card.cancel_button".localized(context),
                    onConfirm = {
                        viewModel.lockCard { }
                        projectConfiguration.supportEmailAddress?.let { recipientAddress ->
                            delegate?.showMailComposer(
                                    recipient = recipientAddress,
                                    subject = "email_lost_card_subject".localized(context),
                                    body = null
                            )
                        }
                    },
                    onCancel = { }
            )
        }
    }

    private fun getDetailedCardActivityPreference() = userPreferencesRepository.showDetailedCardActivity

    private fun storeDetailedCardActivityPreference(show: Boolean) {
        userPreferencesRepository.showDetailedCardActivity = show
        delegate?.transactionsChanged()
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

    companion object {
        fun newInstance(card: Card, cardDetailsShown: Boolean, cardProduct: CardProduct,
                        projectConfiguration: ProjectConfiguration) =
                CardSettingsFragmentThemeTwo().apply {
                    arguments = Bundle().apply {
                        putSerializable(CARD_KEY, card)
                        putBoolean(CARD_DETAILS_SHOWN_KEY, cardDetailsShown)
                        putSerializable(CARD_PRODUCT_KEY, cardProduct)
                        putSerializable(PROJECT_CONFIGURATION_KEY, projectConfiguration)
                    }
                }
    }
}

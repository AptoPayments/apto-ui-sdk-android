package com.aptopayments.sdk.features.card.cardsettings

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.voip.Action.LISTEN_PIN
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCardSettingsBinding
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsViewModel.Action
import com.aptopayments.sdk.ui.views.AuthenticationView
import com.aptopayments.sdk.utils.PhoneDialer
import com.aptopayments.sdk.utils.SendEmailUtil
import com.aptopayments.sdk.utils.deeplinks.InAppProvisioningDeepLinkGenerator
import com.aptopayments.sdk.utils.deeplinks.IntentGenerator
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_card_settings.*
import kotlinx.android.synthetic.main.include_custom_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

private const val CARD_KEY = "CARD"
private const val CARD_PRODUCT_KEY = "CARD_PRODUCT"
private const val PROJECT_CONFIGURATION_KEY = "PROJECT_CONFIGURATION"

internal class CardSettingsFragment :
    BaseDataBindingFragment<FragmentCardSettingsBinding>(),
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
        viewModel.onPresented()
        customizeSecondaryNavigationStatusBar()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(state, ::handleCardLocked)
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observe(viewModel.failure) { handleFailure(it) }
            observeNullable(action) {
                when (it) {
                    is Action.CustomerSupportEmail -> sendCustomerSupportEmail()
                    is Action.ContentPresenter -> showContentPresenter(it.content, it.title)
                    is Action.ShowCardDetails -> manageCardDetails()
                    is Action.AuthenticateCardDetails -> handleAuthenticateCardDetails()
                    is Action.CardStateChanged -> delegate?.onCardStateChanged()
                    is Action.SetCardPasscodeErrorDisabled -> cardPasscodeErrorCardDisabled()
                    is Action.SetCardPasscode -> delegate?.onSetCardPasscode()
                    is Action.CallIvr -> callIvr(it.phoneNumber)
                    is Action.ShowNoSimInsertedError -> showNoSimInsertedError()
                    is Action.CallVoIpListenPin -> delegate?.showVoip(action = LISTEN_PIN)
                    is Action.AddFunds -> delegate?.onAddFunds()
                    is Action.ShowAddFundsSelector -> delegate?.showAddFundsSelector()
                    is Action.ShowAddFundsAchDisclaimer -> delegate?.showAddFundsDisclaimer(it.disclaimer)
                    is Action.OrderPhysicalCard -> delegate?.showOrderPhysicalCard()
                    is Action.TransferMoneyAction -> delegate?.transferMoney()
                    is Action.SetPin -> delegate?.onSetPin()
                }
            }
        }
    }

    private fun handleAuthenticateCardDetails() {
        (activity as CardActivity).authenticate(
            AuthenticationView.AuthType.OPTIONAL,
            onCancelled = { viewModel.cardDetailsAuthenticationError() },
            onAuthenticated = { viewModel.cardDetailsAuthenticationSuccessful() }
        )
    }

    private fun manageCardDetails() {
        onBackPressed()
    }

    private fun handleCardLocked(state: CardSettingsViewModel.State) {
        if (binding.cardSettingsLockSwitch.isChecked != state.cardLocked) {
            binding.cardSettingsLockSwitch.silentlyToggleSwitch(::lockUnlockCard)
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
        binding.cardSettingsLockSwitch.setOnCheckedChangeListener { _, value -> lockUnlockCard(value) }
        binding.cardSettingsDetailedActivitySwitch.setOnCheckedChangeListener { _, value ->
            storeDetailedCardActivityPreference(value)
        }
        binding.rlReportStolenCard.setOnClickListenerSafe { reportLostOrStolenCard() }
        binding.rlStatement.setOnClickListenerSafe { onStatementPressed() }
        binding.rlGooglePay.setOnClickListenerSafe { onGooglePayPressed() }
    }

    private fun setupTheme() {
        themeManager().customizeToolbarTitle(tv_toolbar_title)

        if (AptoUiSdk.cardOptions.showDetailedCardActivityOption()) {
            transactions_section.show()
            binding.cardSettingsDetailedActivitySwitch.isChecked = getDetailedCardActivityPreference()
        }
    }

    private fun setupTexts() {
        tv_toolbar_title.localizedText = "card_settings.settings.title"
    }

    private fun setupToolBar() {
        val toolbar = tb_llsdk_custom_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_close_button.setColorFilter(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
    }

    override fun onBackPressed() {
        delegate?.onBackFromCardSettings()
    }

    private fun callIvr(phoneNumber: PhoneNumber) = context?.let { context ->
        val phoneDialer = PhoneDialer(context)
        phoneDialer.dialPhone(phoneNumber.toStringRepresentation(), null)
    }

    private fun showNoSimInsertedError() {
        notify("card_settings_help_ivr_support_no_sim".localized(), SnackbarMessageType.ERROR)
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
                onConfirm = { viewModel.lockCard() },
                onCancel = { binding.cardSettingsLockSwitch.silentlyToggleSwitch(::lockUnlockCard) }
            )
        } else {
            confirm(
                title = "card_settings.settings.confirm_unlock_card.title".localized(),
                text = "card_settings.settings.confirm_unlock_card.message".localized(),
                confirm = "card_settings.settings.confirm_unlock_card.ok_button".localized(),
                cancel = "card_settings.settings.confirm_unlock_card.cancel_button".localized(),
                onConfirm = { viewModel.unlockCard() },
                onCancel = { binding.cardSettingsLockSwitch.silentlyToggleSwitch(::lockUnlockCard) }
            )
        }
    }

    private fun showContentPresenter(content: Content, titleToLocalize: String) {
        delegate?.showContentPresenter(content, titleToLocalize.localized())
    }

    private fun sendCustomerSupportEmail() {
        (projectConfiguration.supportEmailAddress)?.let {
            sendEmail(it, "help_mail_subject".localized(), "help_mail_body".localized())
        }
    }

    private fun sendEmail(recipient: String, subject: String?, body: String?) {
        activity?.let { activity ->
            SendEmailUtil(recipient, subject, body).execute(activity).runIfLeft { handleFailure(it) }
        }
    }

    private fun reportLostOrStolenCard() {
        confirm(
            title = "card_settings.settings.confirm_report_lost_card.title".localized(),
            text = "card_settings.settings.confirm_report_lost_card.message".localized(),
            confirm = "card_settings.settings.confirm_report_lost_card.ok_button".localized(),
            cancel = "card_settings.settings.confirm_report_lost_card.cancel_button".localized(),
            onConfirm = {
                viewModel.lockCard()
                projectConfiguration.supportEmailAddress?.let { recipientAddress ->
                    sendEmail(
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

    private fun cardPasscodeErrorCardDisabled() {
        notify(
            message = "manage_card_set_passcode_error_card_not_enabled".localized(),
            type = SnackbarMessageType.ERROR
        )
    }

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

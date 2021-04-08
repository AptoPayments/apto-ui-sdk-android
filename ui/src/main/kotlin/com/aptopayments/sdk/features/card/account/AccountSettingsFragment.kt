package com.aptopayments.sdk.features.card.account

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentAccountSettingsBinding
import com.aptopayments.sdk.utils.SendEmailUtil
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.aptopayments.sdk.utils.chatbot.ChatbotActivityLauncher
import com.aptopayments.sdk.utils.chatbot.ChatbotParameters
import kotlinx.android.synthetic.main.include_custom_toolbar.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

private const val ACCOUNT_SETTINGS_BUNDLE = "contextConfigurationBundle"
private const val CARD_ID = "CARD_ID"

internal class AccountSettingsFragment :
    BaseBindingFragment<FragmentAccountSettingsBinding>(),
    AccountSettingsContract.View {

    override var delegate: AccountSettingsContract.Delegate? = null
    private lateinit var contextConfiguration: ContextConfiguration
    private lateinit var cardId: String

    private val viewModel: AccountSettingsViewModel by viewModel {
        parametersOf(
            cardId,
            contextConfiguration.projectConfiguration
        )
    }
    private val uiSdkProtocol: AptoUiSdkProtocol by inject()
    private val chatbotLauncher: ChatbotActivityLauncher by inject()

    override fun layoutId(): Int = R.layout.fragment_account_settings

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.fingerprintEnabled) {
            if (binding.accountSettingsFingerprintSwitch.isChecked != it) {
                binding.accountSettingsFingerprintSwitch.silentlyToggleSwitch { value -> viewModel.onFingerprintSwitchTapped(value) }
            }
        }
        observeNotNullable(viewModel.action) { action ->
            when (action) {
                is AccountSettingsViewModel.Action.CustomerSupportEmail -> sendCustomerSupportEmail()
                is AccountSettingsViewModel.Action.LaunchChatbot -> launchChatbot(action.param)
            }
        }
    }

    private fun launchChatbot(params: ChatbotParameters) {
        activity?.let {
            chatbotLauncher.show(it, params)
        }
    }

    override fun setUpArguments() {
        contextConfiguration = requireArguments()[ACCOUNT_SETTINGS_BUNDLE] as ContextConfiguration
        cardId = requireArguments()[CARD_ID] as String
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setupTexts()
    }

    override fun setupListeners() {
        super.setupListeners()
        binding.accountSettingsCustomToolbar.iv_close_button.setOnClickListenerSafe { onBackPressed() }
        binding.accountSettingsSignOut.setOnClickListenerSafe { showConfirmLogOutDialog() }
        binding.rlNotifications.setOnClickListenerSafe { delegate?.showNotificationPreferences() }
        binding.accountSettingsStatements.setOnClickListenerSafe { delegate?.onMonthlyStatementTapped() }
        binding.rlPasscode.setOnClickListenerSafe { onChangePasscodeTapped() }
        binding.accountSettingsFingerprintSwitch.setOnCheckedChangeListener { _, value -> viewModel.onFingerprintSwitchTapped(value) }
    }

    private fun onChangePasscodeTapped() {
        delegate?.onChangePasscodeTapped()
    }

    override fun onBackPressed() {
        delegate?.onAccountSettingsClosed()
    }

    private fun setupTheme() {
        activity?.window?.let {
            with(themeManager()) {
                customizeSecondaryNavigationStatusBar(it)
                customizeToolbarTitle(binding.accountSettingsCustomToolbar.tv_toolbar_title)
            }
        }
    }

    private fun setupToolbar() {
        with(binding.accountSettingsCustomToolbar) {
            setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
            iv_close_button.setColorFilter(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
            tv_toolbar_title.localizedText = "account_settings.settings.title"
        }
    }

    private fun setupTexts() {
        with(binding.rlAppVersion) {
            optionDescription = uiSdkProtocol.getAppVersion(activity)
            hideRightArrow()
        }
    }

    private fun showConfirmLogOutDialog() {
        confirm(
            title = "account_settings.logout.confirm_logout.title".localized(),
            text = "account_settings.logout.confirm_logout.message".localized(),
            confirm = "account_settings.logout.confirm_logout.ok_button".localized(),
            cancel = "account_settings_logout_confirm_logout_cancel_button".localized(),
            onConfirm = { delegate?.onLogOut() },
            onCancel = { }
        )
    }

    private fun sendCustomerSupportEmail() = contextConfiguration.projectConfiguration.supportEmailAddress?.let {
        activity?.let { activity ->
            val subject = "help_mail_subject".localized()
            val body = "help_mail_body".localized()
            SendEmailUtil(it, subject, body).execute(activity)
        }
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(contextConfiguration: ContextConfiguration, cardId: String) = AccountSettingsFragment().apply {
            this.arguments = Bundle().apply {
                putSerializable(ACCOUNT_SETTINGS_BUNDLE, contextConfiguration)
                putString(CARD_ID, cardId)
            }
        }
    }
}

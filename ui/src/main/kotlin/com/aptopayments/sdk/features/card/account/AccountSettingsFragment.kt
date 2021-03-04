package com.aptopayments.sdk.features.card.account

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Switch
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentAccountSettingsBinding
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleView
import com.aptopayments.sdk.ui.views.SectionSwitchViewTwo
import com.aptopayments.sdk.utils.SendEmailUtil
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.aptopayments.sdk.utils.chatbot.ChatbotActivityLauncher
import com.aptopayments.sdk.utils.chatbot.ChatbotParameters
import kotlinx.android.synthetic.main.include_custom_toolbar.view.*
import kotlinx.android.synthetic.main.view_section_switch_two.view.*
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
            silentlySetSwitch(
                it,
                binding.rlFingerprint.sw_tv_section_switch_switch
            ) { viewModel.onFingerprintSwitchTapped() }
            binding.rlFingerprint.sw_tv_section_switch_switch.isChecked = it
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

    private fun silentlySetSwitch(value: Boolean, switch: Switch, listener: () -> Unit) {
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = value
        switch.setOnCheckedChangeListener { _, _ -> listener() }
    }

    override fun setupListeners() {
        super.setupListeners()
        binding.accountSettingsCustomToolbar.iv_close_button.setOnClickListenerSafe { onBackPressed() }
        binding.accountSettingsSignOut.setOnClickListenerSafe { viewModel.onCustomerSupport() }
        binding.accountSettingsSignOut.setOnClickListenerSafe { showConfirmLogOutDialog() }

        binding.rlNotifications.setOnClickListenerSafe { delegate?.showNotificationPreferences() }
        binding.accountSettingsStatements.setOnClickListenerSafe { delegate?.onMonthlyStatementTapped() }
        binding.rlPasscode.setOnClickListenerSafe { onChangePasscodeTapped() }
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
            optionSubtitle = uiSdkProtocol.getAppVersion(activity)
            hideRightArrow()
        }
        (binding.rlFingerprint as SectionSwitchViewTwo).apply {
            set("account_settings_security_fingerprint".localized())
            hideBottomSeparator()
        }
        (binding.accountSettingsContactSupport as SectionOptionWithSubtitleView).apply {
            optionTitle = viewModel.supportTexts.first
            optionSubtitle = viewModel.supportTexts.second
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

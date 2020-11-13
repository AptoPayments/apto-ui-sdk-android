package com.aptopayments.sdk.features.card.account

import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleView
import com.aptopayments.sdk.ui.views.SectionSwitchViewTwo
import com.aptopayments.sdk.utils.SendEmailUtil
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_account_settings.*
import kotlinx.android.synthetic.main.include_custom_toolbar.*
import kotlinx.android.synthetic.main.view_section_switch_two.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject

internal const val ACCOUNT_SETTINGS_BUNDLE = "contextConfigurationBundle"

internal class AccountSettingsFragment : BaseFragment(), AccountSettingsContract.View {

    override var delegate: AccountSettingsContract.Delegate? = null
    private var contextConfiguration: ContextConfiguration? = null
    private val viewModel: AccountSettingsViewModel by viewModel()
    private val uiSdkProtocol: AptoUiSdkProtocol by inject()

    override fun layoutId(): Int = R.layout.fragment_account_settings

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setupViewModel() {
        observeNotNullable(viewModel.fingerprintEnabled) {
            silentlySetSwitch(it, rl_fingerprint.sw_tv_section_switch_switch) { fingerprintChanged() }
            rl_fingerprint.sw_tv_section_switch_switch.isChecked = it
        }
    }

    override fun setUpArguments() {
        contextConfiguration = requireArguments()[ACCOUNT_SETTINGS_BUNDLE] as ContextConfiguration
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setupTexts()
        setUpVisibility()
    }

    private fun setUpVisibility() {
        rl_statements.visibleIf(viewModel.monthlyStatementVisibility)
        rl_security.visibleIf(viewModel.securityVisibility)
        rl_fingerprint.visibleIf(viewModel.fingerprintVisibility)
        rl_passcode.visibleIf(viewModel.securityVisibility)
        rl_settings_header.visibleIf(viewModel.notificationVisibility)
        rl_notifications.visibleIf(viewModel.notificationVisibility)
    }

    private fun silentlySetSwitch(value: Boolean, switch: Switch, listener: () -> Unit) {
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = value
        switch.setOnCheckedChangeListener { _, _ -> listener() }
    }

    private fun fingerprintChanged() {
        viewModel.onFingerprintSwitchTapped()
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_close_button.setOnClickListenerSafe { onBackPressed() }
        rl_contact_support.setOnClickListenerSafe { sendCustomerSupportEmail() }
        rl_sign_out.setOnClickListenerSafe { showConfirmLogOutDialog() }
        rl_notifications.setOnClickListenerSafe { delegate?.showNotificationPreferences() }
        rl_statements.setOnClickListenerSafe { delegate?.onMonthlyStatementTapped() }
        rl_passcode.setOnClickListenerSafe { onChangePasscodeTapped() }
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
                customizeToolbarTitle(tv_toolbar_title)
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = tb_llsdk_custom_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_close_button.setColorFilter(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
        tv_toolbar_title.localizedText = "account_settings.settings.title"
    }

    private fun setupTexts() {
        with(rl_app_version as SectionOptionWithSubtitleView) {
            optionSubtitle = uiSdkProtocol.getAppVersion(activity)
            hideRightArrow()
        }
        (rl_fingerprint as SectionSwitchViewTwo).apply {
            set("account_settings_security_fingerprint".localized())
            hideBottomSeparator()
        }
    }

    private fun showConfirmLogOutDialog() {
        confirm(title = "account_settings.logout.confirm_logout.title".localized(),
            text = "account_settings.logout.confirm_logout.message".localized(),
            confirm = "account_settings.logout.confirm_logout.ok_button".localized(),
            cancel = "account_settings_logout_confirm_logout_cancel_button".localized(),
            onConfirm = { delegate?.onLogOut() },
            onCancel = { })
    }

    private fun sendCustomerSupportEmail() = contextConfiguration?.projectConfiguration?.supportEmailAddress?.let {
        activity?.let { activity ->
            val subject = "help.mail.subject".localized()
            val body = "help.mail.body".localized()
            SendEmailUtil(it, subject, body).execute(activity)
        }
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(contextConfiguration: ContextConfiguration) = AccountSettingsFragment().apply {
            this.arguments = Bundle().apply { putSerializable(ACCOUNT_SETTINGS_BUNDLE, contextConfiguration) }
        }
    }
}

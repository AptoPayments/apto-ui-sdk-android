package com.aptopayments.sdk.features.card.account

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.Switch
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.Toolbar
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.ui.views.AuthenticationView
import com.aptopayments.sdk.ui.views.SectionHeaderViewTwo
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleViewTwo
import com.aptopayments.sdk.ui.views.SectionSwitchViewTwo
import com.aptopayments.sdk.utils.SendEmailUtil
import kotlinx.android.synthetic.main.fragment_account_settings_theme_two.*
import kotlinx.android.synthetic.main.include_custom_toolbar_two.*
import kotlinx.android.synthetic.main.view_section_switch_two.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

const val ACCOUNT_SETTINGS_BUNDLE = "contextConfigurationBundle"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class AccountSettingsFragmentThemeTwo : BaseFragment(), AccountSettingsContract.View {

    override var delegate: AccountSettingsContract.Delegate? = null
    private var contextConfiguration: ContextConfiguration? = null
    private val viewModel: AccountSettingsViewModel by viewModel()

    override fun layoutId(): Int = R.layout.fragment_account_settings_theme_two

    override fun setupViewModel() {
        observeNotNullable(viewModel.fingerprintEnabled) {
            silentlySetSwitch(it, rl_fingerprint.sw_tv_section_switch_switch) { fingerprintChanged() }
            rl_fingerprint.sw_tv_section_switch_switch.isChecked = it
        }
    }

    override fun setUpArguments() {
        contextConfiguration = arguments!![ACCOUNT_SETTINGS_BUNDLE] as ContextConfiguration
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
        viewModel.onFingerprintSwichTapped()
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_close_button.setOnClickListener { onBackPressed() }
        rl_contact_support.setOnClickListener { sendCustomerSupportEmail() }
        rl_sign_out.setOnClickListener { showConfirmLogOutDialog() }
        rl_notifications.setOnClickListener { delegate?.showNotificationPreferences() }
        rl_statements.setOnClickListener { delegate?.onMonthlyStatementTapped() }
        rl_passcode.setOnClickListener { onChangePasscodeTapped() }
    }

    private fun onChangePasscodeTapped() {
        (activity as CardActivity).authenticate(
            type = AuthenticationView.AuthType.OPTIONAL,
            onlyPin = true,
            onCancelled = {},
            onAuthenticated = { delegate?.onChangePasscodeTapped() }
        )
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
        tv_toolbar_title.text = "account_settings.settings.title".localized()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        (rl_security as SectionHeaderViewTwo).set("account_settings_security_title".localized())
        (rl_fingerprint as SectionSwitchViewTwo).apply {
            set("account_settings_security_fingerprint".localized())
            hideBottomSeparator()
        }
        (rl_passcode as SectionOptionWithSubtitleViewTwo).set(
            "account_settings_security_change_pin_title".localized(),
            "account_settings_security_change_pin_description".localized()
        )
        (rl_settings_header as SectionHeaderViewTwo).set("account_settings.app_settings.title".localized())
        (rl_notifications as SectionOptionWithSubtitleViewTwo).apply {
            set(
                "account_settings.notification_preferences.title".localized(),
                "account_settings.notification_preferences.description".localized()
            )
            hideBottomSeparator()
        }
        (rl_support_header as SectionHeaderViewTwo).set("account_settings.help.title".localized())
        (rl_contact_support as SectionOptionWithSubtitleViewTwo).set(
            "account_settings.help.contact_support.title".localized(),
            "account_settings.help.contact_support.description".localized()
        )
        (rl_statements as SectionOptionWithSubtitleViewTwo).set(
            "card_settings.help.monthly_statements.title".localized(),
            "card_settings.help.monthly_statements.description".localized()
        )
        (rl_sign_out as SectionOptionWithSubtitleViewTwo).set("account_settings.logout.title".localized())
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
        fun newInstance(contextConfiguration: ContextConfiguration) = AccountSettingsFragmentThemeTwo().apply {
            this.arguments = Bundle().apply { putSerializable(ACCOUNT_SETTINGS_BUNDLE, contextConfiguration) }
        }
    }
}

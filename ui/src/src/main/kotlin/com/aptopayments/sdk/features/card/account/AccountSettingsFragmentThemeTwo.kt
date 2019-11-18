package com.aptopayments.sdk.features.card.account

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.Toolbar
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.SendEmailUtil
import kotlinx.android.synthetic.main.fragment_account_settings_theme_two.*
import kotlinx.android.synthetic.main.include_custom_toolbar_two.*
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
    }

    override fun setUpArguments() {
        contextConfiguration = arguments!![ACCOUNT_SETTINGS_BUNDLE] as ContextConfiguration
    }

    override fun setupUI() {
        if (AptoUiSdk.cardOptions.showNotificationPreferences()) ll_app_settings_holder.show()
        setupTheme()
        setupToolbar()
        setupTexts()
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_close_button.setOnClickListener { onBackPressed() }
        rl_contact_support.setOnClickListener { sendCustomerSupportEmail() }
        rl_sign_out.setOnClickListener { showConfirmLogOutDialog() }
        rl_notifications.setOnClickListener { delegate?.showNotificationPreferences() }
        statements_container.setOnClickListener { delegate?.onMonthlyStatementTapped() }
    }

    override fun onBackPressed() {
        delegate?.onAccountSettingsClosed()
    }

    private fun setupTheme() {
        activity?.window?.let {
            with(themeManager()) {
                customizeSecondaryNavigationStatusBar(it)
                customizeToolbarTitle(tv_toolbar_title)
                customizeStarredSectionTitle(tv_app_settings_header, UIConfig.textSecondaryColor)
                customizeMainItem(tv_notifications)
                customizeTimestamp(tv_notifications_description)
                customizeStarredSectionTitle(tv_support_header, UIConfig.textSecondaryColor)
                customizeMainItem(tv_contact_support)
                customizeTimestamp(tv_contact_support_description)
                customizeMainItem(tv_sign_out)
                customizeMainItem(statements_title)
                customizeTimestamp(statements_description)
            }
        }
        iv_notifications_icon.setColorFilter(UIConfig.uiTertiaryColor)
        iv_help_center_icon.setColorFilter(UIConfig.uiTertiaryColor)
        iv_sign_out_icon.setColorFilter(UIConfig.uiTertiaryColor)
        statements_icon.setColorFilter(UIConfig.uiTertiaryColor)
        evaluateMonthlyStatementsRemoval()
    }

    private fun evaluateMonthlyStatementsRemoval() {
        if (!AptoUiSdk.cardOptions.showMonthlyStatementOption()) {
            statements_container.remove()
            statements_separator.remove()
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
        tv_app_settings_header.text = "account_settings.app_settings.title".localized()
        tv_notifications.text = "account_settings.notification_preferences.title".localized()
        tv_notifications_description.text = "account_settings.notification_preferences.description".localized()
        tv_support_header.text = "account_settings.help.title".localized()
        tv_contact_support.text = "account_settings.help.contact_support.title".localized()
        tv_contact_support_description.text = "account_settings.help.contact_support.description".localized()
        tv_sign_out.text = "account_settings.logout.title".localized()
        statements_title.text = "card_settings.help.monthly_statements.title".localized()
        statements_description.text = "card_settings.help.monthly_statements.description".localized()
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

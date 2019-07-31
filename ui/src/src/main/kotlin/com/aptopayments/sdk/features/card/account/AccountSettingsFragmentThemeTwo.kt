package com.aptopayments.sdk.features.card.account

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.Toolbar
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.SendEmailUtil
import kotlinx.android.synthetic.main.fragment_account_settings_theme_two.*
import kotlinx.android.synthetic.main.include_custom_toolbar_two.*
import java.lang.reflect.Modifier

const val ACCOUNT_SETTINGS_BUNDLE = "contextConfigurationBundle"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class AccountSettingsFragmentThemeTwo : BaseFragment(), AccountSettingsContract.View {

    override var delegate: AccountSettingsContract.Delegate? = null
    private var contextConfiguration: ContextConfiguration? = null
    private lateinit var viewModel: AccountSettingsViewModel

    override fun layoutId(): Int = R.layout.fragment_account_settings_theme_two

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contextConfiguration = arguments!![ACCOUNT_SETTINGS_BUNDLE] as ContextConfiguration
    }

    override fun setupUI() {
        if (AptoPlatform.cardOptions.showNotificationPreferences()) ll_app_settings_holder.show()
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
            }
        }
        iv_notifications_icon.setColorFilter(UIConfig.uiTertiaryColor)
        iv_help_center_icon.setColorFilter(UIConfig.uiTertiaryColor)
        iv_sign_out_icon.setColorFilter(UIConfig.uiTertiaryColor)
    }

    private fun setupToolbar() {
        val toolbar = tb_llsdk_custom_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tv_toolbar_title.text = context?.let { "account_settings.settings.title".localized(it) }
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        context?.let {
            tv_app_settings_header.text = "account_settings.app_settings.title".localized(it)
            tv_notifications.text = "account_settings.notification_preferences.title".localized(it)
            tv_notifications_description.text = "account_settings.notification_preferences.description".localized(it)
            tv_support_header.text = "account_settings.help.title".localized(it)
            tv_contact_support.text = "account_settings.help.contact_support.title".localized(it)
            tv_contact_support_description.text = "account_settings.help.contact_support.description".localized(it)
            tv_sign_out.text = "account_settings.logout.title".localized(it)
        }
    }

    private fun showConfirmLogOutDialog() {
        context?.let { context ->
            confirm(
                    title="account_settings.logout.confirm_logout.title".localized(context),
                    text = "account_settings.logout.confirm_logout.message".localized(context),
                    confirm = "account_settings.logout.confirm_logout.ok_button".localized(context),
                    cancel = "account_settings_logout_confirm_logout_cancel_button".localized(context),
                    onConfirm = { delegate?.onLogOut() },
                    onCancel = { }
            )
        }
    }

    private fun sendCustomerSupportEmail() {
        contextConfiguration?.projectConfiguration?.supportEmailAddress?.let {
            activity?.let {activity ->
                val subject = "help.mail.subject".localized(activity)
                val body = "help.mail.body".localized(activity)
                SendEmailUtil(it, subject, body).execute(activity)
            }
        }
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

    companion object {
        fun newInstance(contextConfiguration: ContextConfiguration) = AccountSettingsFragmentThemeTwo().apply {
            this.arguments = Bundle().apply { putSerializable(ACCOUNT_SETTINGS_BUNDLE, contextConfiguration) }
        }
    }
}

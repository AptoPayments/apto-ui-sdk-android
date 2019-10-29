package com.aptopayments.sdk.features.card.notificationpreferences

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View.VISIBLE
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.core.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.fragment_notification_preferences_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val CARD_ID_PARAMETER_KEY = "card_id"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class NotificationPreferencesFragmentThemeTwo : BaseFragment(), NotificationPreferencesContract.View {

    private val viewModel: NotificationPreferencesViewModel by viewModel()
    private lateinit var cardId: String

    override var delegate: NotificationPreferencesContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_notification_preferences_theme_two

    override fun setUpArguments() {
        cardId = arguments!![CARD_ID_PARAMETER_KEY] as String
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(notificationPreferencesList, ::handleNotificationPreferencesList)
            observe(secondaryChannel, ::setHeader)
            failure(failure) {
                hideLoading()
                handleFailure(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPresented() {
        showLoading()
        viewModel.getNotificationPreferences()
    }

    private fun handleNotificationPreferencesList(notificationPreferences: List<NotificationPreferenceLineItem>?) {
        hideLoading()
        notificationPreferences?.let { notificationPreferencesList ->
            notificationPreferencesList.forEach {
                when (it.group) {
                    NotificationGroup.Group.PAYMENT_SUCCESSFUL -> handlePaymentSuccessfulNotificationPreference(it)
                    NotificationGroup.Group.PAYMENT_DECLINED -> handlePaymentDeclinedNotificationPreference(it)
                    NotificationGroup.Group.ATM_WITHDRAWAL -> handleAtmWithdrawalNotificationPreference(it)
                    NotificationGroup.Group.CARD_STATUS -> handleCardStatusNotificationPreference(it)
                    NotificationGroup.Group.LEGAL -> handleLegalNotificationPreference(it)
                    NotificationGroup.Group.INCOMING_TRANSFER -> { }
                }
            }
        }
    }

    private fun handlePaymentSuccessfulNotificationPreference(notificationPreferenceLineItem: NotificationPreferenceLineItem) {
        payment_successful_group.visibility = VISIBLE
        cb_payment_successful_primary_notification.isChecked = notificationPreferenceLineItem.isPrimaryChannelActive
        cb_payment_successful_secondary_notification.isChecked = notificationPreferenceLineItem.isSecondaryChannelActive
    }

    private fun handlePaymentDeclinedNotificationPreference(notificationPreferenceLineItem: NotificationPreferenceLineItem) {
        payment_declined_group.visibility = VISIBLE
        cb_payment_declined_primary_notification.isChecked = notificationPreferenceLineItem.isPrimaryChannelActive
        cb_payment_declined_secondary_notification.isChecked= notificationPreferenceLineItem.isSecondaryChannelActive
    }

    private fun handleAtmWithdrawalNotificationPreference(notificationPreferenceLineItem: NotificationPreferenceLineItem) {
        atm_withdrawal_group.visibility = VISIBLE
        cb_atm_withdrawal_primary_notification.isChecked = notificationPreferenceLineItem.isPrimaryChannelActive
        cb_atm_withdrawal_secondary_notification.isChecked= notificationPreferenceLineItem.isSecondaryChannelActive
    }

    private fun handleCardStatusNotificationPreference(notificationPreferenceLineItem: NotificationPreferenceLineItem) {
        card_status_group.visibility = VISIBLE
        cb_card_status_primary_notification.isChecked = notificationPreferenceLineItem.isPrimaryChannelActive
        cb_card_status_secondary_notification.isChecked= notificationPreferenceLineItem.isSecondaryChannelActive
    }

    private fun handleLegalNotificationPreference(notificationPreferenceLineItem: NotificationPreferenceLineItem) {
        legal_group.visibility = VISIBLE
        cb_legal_primary_notification.isChecked = notificationPreferenceLineItem.isPrimaryChannelActive
        cb_legal_secondary_notification.isChecked= notificationPreferenceLineItem.isSecondaryChannelActive
    }

    private fun setHeader(notificationChannel: NotificationChannel?) {
        if (notificationChannel == NotificationChannel.EMAIL) setPushOrEmailHeader()
        else setPushOrSmsHeader()
    }

    override fun setupUI() {
        setupTexts()
        setupTheme()
        setupToolBar()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() = context?.let {
        tv_card_activity_title.text = "notification_preferences.card_activity.title".localized(it)
        tv_card_activity_description.text = "notification_preferences.card_activity.description".localized(it)
        tv_payment_successful.text = "notification_preferences.card_activity.payment_successful.title".localized(it)
        tv_payment_declined.text = "notification_preferences.card_activity.payment_declined.title".localized(it)
        tv_atm_withdrawal.text = "notification_preferences.card_activity.atm_withdrawal.title".localized(it)
        tv_card_status_title.text = "notification_preferences.card_status.title".localized(it)
        tv_card_status_description.text = "notification_preferences.card_status.description".localized(it)
        tv_legal_title.text = "notification_preferences.legal.title".localized(it)
        tv_legal_description.text = "notification_preferences.legal.description".localized(it)
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        ll_notifications_header.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        with(themeManager()) {
            customizeStarredSectionTitle(tv_notifications_header)
            customizeSectionTitle(tv_card_activity_title)
            customizeTimestamp(tv_card_activity_description)
            customizeMainItem(tv_payment_successful)
            customizeMainItem(tv_payment_declined)
            customizeMainItem(tv_atm_withdrawal)
            customizeSectionTitle(tv_card_status_title)
            customizeTimestamp(tv_card_status_description)
            customizeSectionTitle(tv_legal_title)
            customizeTimestamp(tv_legal_description)
            customizeCheckBox(cb_payment_successful_primary_notification)
            customizeCheckBox(cb_payment_successful_secondary_notification)
            customizeCheckBox(cb_payment_declined_primary_notification)
            customizeCheckBox(cb_payment_declined_secondary_notification)
            customizeCheckBox(cb_atm_withdrawal_primary_notification)
            customizeCheckBox(cb_atm_withdrawal_secondary_notification)
            customizeCheckBox(cb_card_status_primary_notification)
            customizeCheckBox(cb_card_status_secondary_notification)
            customizeCheckBox(cb_legal_primary_notification)
            customizeCheckBox(cb_legal_secondary_notification)
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        cb_payment_successful_primary_notification.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                showLoading()
                viewModel.updateNotificationPreferences(NotificationGroup.Group.PAYMENT_SUCCESSFUL, isPrimary = true, active = isChecked) { hideLoading() }
            }
        }
        cb_payment_successful_secondary_notification.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                showLoading()
                viewModel.updateNotificationPreferences(NotificationGroup.Group.PAYMENT_SUCCESSFUL, isPrimary = false, active = isChecked) { hideLoading() }
            }
        }
        cb_payment_declined_primary_notification.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                showLoading()
                viewModel.updateNotificationPreferences(NotificationGroup.Group.PAYMENT_DECLINED, isPrimary = true, active = isChecked) { hideLoading() }
            }
        }
        cb_payment_declined_secondary_notification.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                showLoading()
                viewModel.updateNotificationPreferences(NotificationGroup.Group.PAYMENT_DECLINED, isPrimary = false, active = isChecked) { hideLoading() }
            }
        }
        cb_atm_withdrawal_primary_notification.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                showLoading()
                viewModel.updateNotificationPreferences(NotificationGroup.Group.ATM_WITHDRAWAL, isPrimary = true, active = isChecked) { hideLoading() }
            }
        }
        cb_atm_withdrawal_secondary_notification.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                showLoading()
                viewModel.updateNotificationPreferences(NotificationGroup.Group.ATM_WITHDRAWAL, isPrimary = false, active = isChecked) { hideLoading() }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setPushOrEmailHeader() = context?.let {
        tv_notifications_header.text = "notification_preferences.send_push_email.title".localized(it)
        setPrimaryNotificationChannelDrawable(R.drawable.ic_notifications_push)
        setSecondaryNotificationChannelDrawable(R.drawable.ic_notifications_mail)
    }

    @SuppressLint("SetTextI18n")
    private fun setPushOrSmsHeader() = context?.let {
        tv_notifications_header.text = "notification_preferences.send_push_sms.title".localized(it)
        setPrimaryNotificationChannelDrawable(R.drawable.ic_notifications_push)
        setSecondaryNotificationChannelDrawable(R.drawable.ic_notifications_sms)
    }

    private fun setPrimaryNotificationChannelDrawable(drawableResource: Int) = context?.let {
        val icon = ContextCompat.getDrawable(it, drawableResource)
        icon?.setColorFilter(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
        iv_primary_notification_channel.setImageDrawable(icon)
    }

    private fun setSecondaryNotificationChannelDrawable(drawableResource: Int) = context?.let {
        val icon = ContextCompat.getDrawable(it, drawableResource)
        icon?.setColorFilter(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
        iv_secondary_notification_channel.setImageDrawable(icon)
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.iconTertiaryColor)
        context?.let {
            delegate?.configureToolbar(
                    toolbar = tb_llsdk_toolbar,
                    title = "notification_preferences.title".localized(it),
                    backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.textTopBarSecondaryColor)
            )
        }
    }

    override fun onBackPressed() {
        delegate?.onBackFromNotificationsPreferences()
    }

    companion object {
        fun newInstance(cardId: String) = NotificationPreferencesFragmentThemeTwo().apply {
            arguments = Bundle().apply { putSerializable(CARD_ID_PARAMETER_KEY, cardId) }
        }
    }
}

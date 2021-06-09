package com.aptopayments.sdk.features.card.notificationpreferences

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentNotificationPreferencesBinding
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import kotlinx.android.synthetic.main.fragment_notification_preferences.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CARD_ID_PARAMETER_KEY = "card_id"

internal class NotificationPreferencesFragment :
    BaseBindingFragment<FragmentNotificationPreferencesBinding>(),
    NotificationPreferencesContract.View {

    private val viewModel: NotificationPreferencesViewModel by viewModel()
    private val notificationChannelResources: NotificationChannelResources by inject()
    private lateinit var cardId: String

    override var delegate: NotificationPreferencesContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_notification_preferences

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID_PARAMETER_KEY] as String
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.resources = notificationChannelResources
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(state) {
                setHeader(it.secondaryChannel)
                setNotificationPreferenceList(it.items)
            }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observeNotNullable(viewModel.failure) { handleFailure(it) }
        }
    }

    override fun onPresented() {
        viewModel.refreshNotificationPreferences()
    }

    private fun setNotificationPreferenceList(notificationPreferences: List<NotificationPreferenceLineItem>?) {
        notificationPreferences?.let { notificationPreferencesList ->
            notificationPreferencesList.forEach {
                when (it.group) {
                    NotificationGroup.Group.PAYMENT_SUCCESSFUL -> configureLine(
                        payment_successful_group,
                        cb_payment_successful_primary_notification,
                        cb_payment_successful_secondary_notification,
                        it
                    )
                    NotificationGroup.Group.PAYMENT_DECLINED -> configureLine(
                        payment_declined_group,
                        cb_payment_declined_primary_notification,
                        cb_payment_declined_secondary_notification,
                        it
                    )
                    NotificationGroup.Group.ATM_WITHDRAWAL -> configureLine(
                        atm_withdrawal_group,
                        cb_atm_withdrawal_primary_notification,
                        cb_atm_withdrawal_secondary_notification,
                        it
                    )
                    NotificationGroup.Group.CARD_STATUS -> configureLine(
                        card_status_group,
                        cb_card_status_primary_notification,
                        cb_card_status_secondary_notification,
                        it
                    )
                    NotificationGroup.Group.LEGAL -> configureLine(
                        legal_group,
                        cb_legal_primary_notification,
                        cb_legal_secondary_notification,
                        it
                    )
                    NotificationGroup.Group.INCOMING_TRANSFER -> {
                    }
                }
            }
        }
    }

    private fun configureLine(
        group: Group,
        firstCheckBox: CheckBox,
        secondCheckBox: CheckBox,
        item: NotificationPreferenceLineItem
    ) {
        group.show()
        firstCheckBox.isChecked = item.isPrimaryChannelActive
        secondCheckBox.isChecked = item.isSecondaryChannelActive
    }

    override fun setupUI() {
        setupTheme()
        setupToolBar()
    }

    private fun setupTheme() {
        ll_notifications_header.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        setNotificationChannelDrawable(iv_primary_notification_channel, R.drawable.ic_notifications_push)
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

    private fun setHeader(secondaryChannel: NotificationChannel) {
        setNotificationChannelDrawable(iv_secondary_notification_channel, notificationChannelResources.getIconImage(secondaryChannel))
    }

    private fun setNotificationChannelDrawable(view: ImageView, drawableResource: Int) = context?.let {
        val icon = ContextCompat.getDrawable(it, drawableResource)
        icon?.setColorFilterCompat(UIConfig.textTopBarSecondaryColor, PorterDuff.Mode.SRC_ATOP)
        view.setImageDrawable(icon)
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .title("notification_preferences.title".localized())
                .setSecondaryTertiaryColors()
                .build()
        )
    }

    override fun onBackPressed() {
        delegate?.onBackFromNotificationsPreferences()
    }

    companion object {
        fun newInstance(cardId: String) = NotificationPreferencesFragment().apply {
            arguments = Bundle().apply { putSerializable(CARD_ID_PARAMETER_KEY, cardId) }
        }
    }
}

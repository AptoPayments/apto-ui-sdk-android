package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.sdk.R
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NotificationChannelResourcesTest {

    private val sut = NotificationChannelResources()

    @Test
    fun `given secondaryChannel is Email when getTitleText then correct title is provided`() {
        val result = sut.getTitleText(NotificationChannel.EMAIL)

        assertEquals("notification_preferences_send_push_email_title", result)
    }

    @Test
    fun `given secondaryChannel is SMS when getTitleText then correct title is provided`() {
        val result = sut.getTitleText(NotificationChannel.SMS)

        assertEquals("notification_preferences_send_push_sms_title", result)
    }

    @Test
    fun `given secondaryChannel is Email when getIconImage then correct image is provided`() {
        val result = sut.getIconImage(NotificationChannel.EMAIL)

        assertEquals(R.drawable.ic_notifications_mail, result)
    }

    @Test
    fun `given secondaryChannel is SMS when getIconImage then correct image is provided`() {
        val result = sut.getIconImage(NotificationChannel.SMS)

        assertEquals(R.drawable.ic_notifications_sms, result)
    }
}

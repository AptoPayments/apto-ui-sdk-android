package com.aptopayments.sdk.features.notificaitonpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.ActiveChannels
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class NotificationPreferencesViewModelTest : AndroidTest() {

    private lateinit var sut: NotificationPreferencesViewModel

    @Mock
    private lateinit var mockNotificationPreferences: NotificationPreferences

    @Before
    override fun setUp() {
        super.setUp()
        sut = NotificationPreferencesViewModel()
        val mockPreferences = ArrayList<NotificationGroup>()
        mockPreferences.add(mock())
        whenever(mockNotificationPreferences.preferences).thenReturn(mockPreferences)
    }

    @Test
    fun `no notification list items are generated when notification preferences list is null`() {
        val result = sut.generateNotificationPreferenceListItems(null, NotificationChannel.EMAIL)
        assert(result.isEmpty())
    }

    @Test
    fun `no notification list items are generated when notification preferences list is empty`() {
        val result = sut.generateNotificationPreferenceListItems(emptyList(), NotificationChannel.EMAIL)
        assert(result.isEmpty())
    }

    @Test
    fun `notification list items are generated correctly`() {
        // Given
        val testIsPrimaryChannelActive = true
        val testIsSecondaryChannelActive = true
        val testGroupId = NotificationGroup.Group.ATM_WITHDRAWAL
        val testActiveChannels = HashMap<NotificationChannel, Boolean?>()
        testActiveChannels[NotificationChannel.PUSH] = testIsPrimaryChannelActive
        testActiveChannels[NotificationChannel.EMAIL] = testIsSecondaryChannelActive
        val testNotificationGroup = NotificationGroup(
            categoryId = NotificationGroup.Category.CARD_ACTIVITY,
            groupId = testGroupId,
            activeChannels = ActiveChannels(testActiveChannels),
            state = NotificationGroup.State.ENABLED
        )
        val testNotificationPreferencesList = ArrayList<NotificationGroup>()
        testNotificationPreferencesList.add(testNotificationGroup)

        // When
        val result =
            sut.generateNotificationPreferenceListItems(testNotificationPreferencesList, NotificationChannel.EMAIL)

        // Then
        assert(result.size == 1)
        assert(result[0].isPrimaryChannelActive == testIsPrimaryChannelActive)
        assert(result[0].isSecondaryChannelActive == testIsSecondaryChannelActive)
        assert(result[0].group == testGroupId)
    }
}
package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.ActiveChannels
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferenceListItemsCreatorTest {

    private val sut = NotificationPreferenceListItemsCreator()

    @Test
    fun `no notification list items are generated when notification preferences list is empty`() {
        val result = sut.create(NotificationPreferences(emptyList()))
        assert(result.isEmpty())
    }

    @Test
    fun `notification list items are generated correctly`() {
        // Given
        val isPrimaryChannelActive = true
        val isSecondaryChannelActive = true
        val groupId = NotificationGroup.Group.ATM_WITHDRAWAL
        val activeChannels = hashMapOf<NotificationChannel, Boolean?>(
            NotificationChannel.PUSH to isPrimaryChannelActive,
            NotificationChannel.EMAIL to isSecondaryChannelActive
        )
        val notificationGroup = NotificationGroup(
            categoryId = NotificationGroup.Category.CARD_ACTIVITY,
            groupId = groupId,
            activeChannels = ActiveChannels(activeChannels),
            state = NotificationGroup.State.ENABLED
        )
        val preferences = NotificationPreferences(listOf(notificationGroup))

        // When
        val result = sut.create(preferences)

        // Then
        assert(result.size == 1)
        assert(result[0].isPrimaryChannelActive == isPrimaryChannelActive)
        assert(result[0].isSecondaryChannelActive == isSecondaryChannelActive)
        assert(result[0].group == groupId)
    }

    @Test
    fun `given groupId is null when create then Item is not added`() {
        // Given
        val isPrimaryChannelActive = true
        val isSecondaryChannelActive = true
        val groupId = null
        val activeChannels = hashMapOf<NotificationChannel, Boolean?>(
            NotificationChannel.PUSH to isPrimaryChannelActive,
            NotificationChannel.EMAIL to isSecondaryChannelActive
        )
        val notificationGroup = NotificationGroup(
            categoryId = NotificationGroup.Category.CARD_ACTIVITY,
            groupId = groupId,
            activeChannels = ActiveChannels(activeChannels),
            state = NotificationGroup.State.ENABLED
        )
        val preferences = NotificationPreferences(listOf(notificationGroup))

        // When
        val result = sut.create(preferences)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `given PUSH is not active when create then Item is added`() {
        // Given
        val isPrimaryChannelActive = false
        val isSecondaryChannelActive = true
        val groupId = NotificationGroup.Group.ATM_WITHDRAWAL
        val activeChannels = hashMapOf<NotificationChannel, Boolean?>(
            NotificationChannel.PUSH to isPrimaryChannelActive,
            NotificationChannel.EMAIL to isSecondaryChannelActive
        )
        val notificationGroup = NotificationGroup(
            categoryId = NotificationGroup.Category.CARD_ACTIVITY,
            groupId = groupId,
            activeChannels = ActiveChannels(activeChannels),
            state = NotificationGroup.State.ENABLED
        )
        val preferences = NotificationPreferences(listOf(notificationGroup))

        // When
        val result = sut.create(preferences)

        // Then
        // Then
        assert(result.size == 1)
        assertFalse(result[0].isPrimaryChannelActive)
        assert(result[0].isSecondaryChannelActive == isSecondaryChannelActive)
        assert(result[0].group == groupId)
    }

    @Test
    fun `given secondary is not active when create then Item is added`() {
        // Given
        val isPrimaryChannelActive = true
        val isSecondaryChannelActive = false
        val groupId = NotificationGroup.Group.ATM_WITHDRAWAL
        val activeChannels = hashMapOf<NotificationChannel, Boolean?>(
            NotificationChannel.PUSH to isPrimaryChannelActive,
            NotificationChannel.EMAIL to isSecondaryChannelActive
        )
        val notificationGroup = NotificationGroup(
            categoryId = NotificationGroup.Category.CARD_ACTIVITY,
            groupId = groupId,
            activeChannels = ActiveChannels(activeChannels),
            state = NotificationGroup.State.ENABLED
        )
        val preferences = NotificationPreferences(listOf(notificationGroup))

        // When
        val result = sut.create(preferences)

        // Then
        assert(result.size == 1)
        assert(result[0].isPrimaryChannelActive == isPrimaryChannelActive)
        assertFalse(result[0].isSecondaryChannelActive)
        assert(result[0].group == groupId)
    }
}

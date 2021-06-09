package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.ActiveChannels
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstantExecutorExtension::class)
class NotificationPreferencesViewModelTest {

    private val defaultGroup = NotificationGroup.Group.ATM_WITHDRAWAL
    private val defaultPrimaryChannelActive = true
    private val defaultSecondaryChannelActive = false

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val listItemsCreator = NotificationPreferenceListItemsCreator()

    private val sut = NotificationPreferencesViewModel(aptoPlatform, listItemsCreator)

    @Test
    fun `whenever refreshNotificationPreferences then aptoPlatform fetchNotificationPreferences is called`() {
        sut.refreshNotificationPreferences()

        verify(aptoPlatform).fetchNotificationPreferences(any())
    }

    @Test
    fun `given defaultTestConfig when refreshNotificationPreferences then state is populated`() {
        configureNotifications()
        sut.refreshNotificationPreferences()

        val result = sut.state.getOrAwaitValue()

        assertEquals(NotificationChannel.EMAIL, result.secondaryChannel)
        assertEquals(1, result.items.size)
        val firstItem = result.items.first()
        assertEquals(defaultGroup, firstItem.group)
        assertEquals(defaultPrimaryChannelActive, firstItem.isPrimaryChannelActive)
        assertEquals(defaultSecondaryChannelActive, firstItem.isSecondaryChannelActive)
    }

    @Test
    fun `given defaultTestConfig when update then correct data is sent iadsjioas dioas dji o`() {
        configureNotifications()
        sut.refreshNotificationPreferences()
        val captor = argumentCaptor<NotificationPreferences>()
        whenever(
            aptoPlatform.updateNotificationPreferences(
                preferences = captor.capture(),
                callback = TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, NotificationPreferences>) -> Unit).invoke(
                mock<NotificationPreferences>().right()
            )
        }

        sut.updateNotificationPreferences(defaultGroup, NotificationChannel.PUSH, !defaultPrimaryChannelActive)

        verify(aptoPlatform).updateNotificationPreferences(any(), any())
        val capturedPreferences = captor.firstValue.preferences
        assertEquals(1, capturedPreferences.size)
        assertEquals(
            !defaultPrimaryChannelActive,
            capturedPreferences.first().activeChannels.isChannelActive(NotificationChannel.PUSH)
        )
        assertEquals(
            defaultSecondaryChannelActive,
            capturedPreferences.first().activeChannels.isChannelActive(NotificationChannel.EMAIL)
        )
    }

    private fun configureNotifications() {

        val activeChannels = hashMapOf<NotificationChannel, Boolean?>(
            NotificationChannel.PUSH to defaultPrimaryChannelActive,
            NotificationChannel.EMAIL to defaultSecondaryChannelActive
        )
        val notificationGroup = NotificationGroup(
            categoryId = NotificationGroup.Category.CARD_ACTIVITY,
            groupId = defaultGroup,
            activeChannels = ActiveChannels(activeChannels),
            state = NotificationGroup.State.ENABLED
        )
        val preferences = NotificationPreferences(listOf(notificationGroup))

        configureFetch(preferences.right())
    }

    private fun configureFetch(result: Either<Failure, NotificationPreferences>) {
        whenever(
            aptoPlatform.fetchNotificationPreferences(TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[0] as (Either<Failure, NotificationPreferences>) -> Unit).invoke(
                result
            )
        }
    }
}

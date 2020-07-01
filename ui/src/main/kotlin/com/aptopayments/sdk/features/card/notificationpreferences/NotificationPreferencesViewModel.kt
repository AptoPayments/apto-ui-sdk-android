package com.aptopayments.sdk.features.card.notificationpreferences

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.user.notificationpreferences.ActiveChannels
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import java.lang.reflect.Modifier

internal class NotificationPreferencesViewModel : BaseViewModel() {

    var secondaryChannel: MutableLiveData<NotificationChannel> = MutableLiveData()
    var notificationPreferencesList: MutableLiveData<List<NotificationPreferenceLineItem>> = MutableLiveData()
    var notificationGroupPreferencesMap: HashMap<NotificationGroup.Group, ActiveChannels?> = HashMap()

    fun getNotificationPreferences() {
        AptoPlatform.fetchNotificationPreferences { result ->
            result.either(::handleFailure) {
                val channel =
                    if (it.preferences?.firstOrNull()?.activeChannels?.get(NotificationChannel.EMAIL) != null) NotificationChannel.EMAIL
                    else NotificationChannel.SMS
                secondaryChannel.postValue(channel)
                notificationPreferencesList.postValue(generateNotificationPreferenceListItems(it.preferences, channel))
            }
        }
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun generateNotificationPreferenceListItems(
        preferences: List<NotificationGroup>?,
        secondaryChannel: NotificationChannel
    ): ArrayList<NotificationPreferenceLineItem> {
        val notificationList = ArrayList<NotificationPreferenceLineItem>()
        preferences?.forEach { notificationGroup ->
            notificationGroup.groupId?.let { groupId ->
                if (groupId != NotificationGroup.Group.LEGAL && groupId != NotificationGroup.Group.CARD_STATUS) {
                    notificationGroupPreferencesMap[groupId] = notificationGroup.activeChannels
                }
            }
            val secondaryChannelActive =
                if (secondaryChannel == NotificationChannel.EMAIL) notificationGroup.activeChannels?.isChannelActive(
                    NotificationChannel.EMAIL
                )
                else notificationGroup.activeChannels?.isChannelActive(NotificationChannel.SMS)
            if (listOfNotNull(
                    notificationGroup.groupId,
                    notificationGroup.activeChannels?.isChannelActive(NotificationChannel.PUSH),
                    secondaryChannelActive
                ).size == 3
            ) {
                notificationList.add(
                    NotificationPreferenceLineItem(
                        notificationGroup.groupId!!,
                        notificationGroup.activeChannels?.isChannelActive(NotificationChannel.PUSH)!!,
                        secondaryChannelActive!!
                    )
                )
            }
        }
        return notificationList
    }

    fun updateNotificationPreferences(
        groupId: NotificationGroup.Group,
        isPrimary: Boolean,
        active: Boolean,
        onComplete: (Either<Failure, Unit>) -> Unit
    ) {
        if (isPrimary) notificationGroupPreferencesMap[groupId]?.set(NotificationChannel.PUSH, active)
        else {
            when (secondaryChannel.value) {
                NotificationChannel.EMAIL ->
                    notificationGroupPreferencesMap[groupId]?.set(NotificationChannel.EMAIL, active)
                NotificationChannel.SMS ->
                    notificationGroupPreferencesMap[groupId]?.set(NotificationChannel.SMS, active)
                else -> {
                }
            }
        }
        AptoPlatform.updateNotificationPreferences(getUpdateNotificationPreferencesRequest()) {
            onComplete(Either.Right(Unit))
        }
    }

    private fun getUpdateNotificationPreferencesRequest(): NotificationPreferences {
        val request = ArrayList<NotificationGroup>()
        notificationGroupPreferencesMap.forEach {
            request.add(NotificationGroup(groupId = it.key, activeChannels = it.value))
        }
        return NotificationPreferences(request)
    }
}

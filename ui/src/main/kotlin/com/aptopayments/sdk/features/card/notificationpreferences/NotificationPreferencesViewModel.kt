package com.aptopayments.sdk.features.card.notificationpreferences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.user.notificationpreferences.ActiveChannels
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup.Group
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel

internal class NotificationPreferencesViewModel(
    private val aptoPlatform: AptoPlatformProtocol,
    private val listItemsCreator: NotificationPreferenceListItemsCreator,
) : BaseViewModel() {

    private val notificationGroupPreferencesMap: HashMap<Group, ActiveChannels> = HashMap()

    private val _state = MutableLiveData(State())
    val state = _state as LiveData<State>

    fun refreshNotificationPreferences() {
        showLoading()
        aptoPlatform.fetchNotificationPreferences { result ->
            hideLoading()
            result.either(::handleFailure) { preferences ->
                generateNotificationGroupMap(preferences)
                val channel = preferences.calculateSecondaryChannel()
                val items = listItemsCreator.create(preferences)
                _state.postValue(State(secondaryChannel = channel, items = items))
            }
        }
    }

    private fun generateNotificationGroupMap(preferences: NotificationPreferences) {
        preferences.preferences.forEach { notificationGroup ->
            notificationGroup.groupId?.let { groupId ->
                if (groupId != Group.LEGAL && groupId != Group.CARD_STATUS) {
                    notificationGroupPreferencesMap[groupId] = notificationGroup.activeChannels
                }
            }
        }
    }

    fun updateNotificationPreferences(groupId: Group, channel: NotificationChannel, active: Boolean) {
        showLoading()
        notificationGroupPreferencesMap[groupId]?.set(channel, active)
        aptoPlatform.updateNotificationPreferences(getNotificationPreferencesRequest()) { result ->
            hideLoading()
            result.runIfLeft { handleFailure(it) }
        }
    }

    private fun getNotificationPreferencesRequest(): NotificationPreferences {
        val request =
            notificationGroupPreferencesMap.map { NotificationGroup(groupId = it.key, activeChannels = it.value) }
        return NotificationPreferences(request)
    }

    data class State(
        val secondaryChannel: NotificationChannel = NotificationChannel.EMAIL,
        val items: List<NotificationPreferenceLineItem> = emptyList()
    )
}

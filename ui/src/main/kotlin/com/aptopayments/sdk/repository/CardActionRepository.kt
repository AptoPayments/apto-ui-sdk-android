package com.aptopayments.sdk.repository

import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.Timer

private const val DETAILS_EXPIRATION_THRESHOLD_SECONDS = 180L
private const val DETAILS_EXPIRATION_THRESHOLD_MILLI = DETAILS_EXPIRATION_THRESHOLD_SECONDS * 1000

internal interface CardActionRepository {
    val event: LiveEvent<CardAction>
    fun showCardDetails()
    fun hideCardDetails()
    fun setPin()
    fun clear()
}

internal enum class CardAction {
    IDLE, SHOW_DETAILS, HIDE_DETAILS, SET_PIN
}

internal class InMemoryLocalCardActionRepository(private val timer: Timer) :
    CardActionRepository {

    override val event = LiveEvent<CardAction>()
    private var showDetails: Boolean = false

    init {
        event.postValue(CardAction.IDLE)
        timer.setListener {
            if (showDetails) {
                hideCardDetails()
            }
        }
    }

    init {
        event.postValue(CardAction.IDLE)
        timer.setListener {
            if (showDetails) {
                hideCardDetails()
            }
        }
    }

    override fun showCardDetails() {
        showDetails = true
        timer.start(DETAILS_EXPIRATION_THRESHOLD_MILLI)
        event.postValue(CardAction.SHOW_DETAILS)
    }

    override fun hideCardDetails() {
        showDetails = false
        event.postValue(CardAction.HIDE_DETAILS)
    }

    override fun setPin() {
        showDetails = false
        event.postValue(CardAction.SET_PIN)
    }

    override fun clear() {
        showDetails = false
        timer.stop()
    }
}

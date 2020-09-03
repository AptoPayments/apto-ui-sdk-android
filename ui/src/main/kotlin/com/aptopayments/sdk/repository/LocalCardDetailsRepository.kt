package com.aptopayments.sdk.repository

import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.Timer

private const val DETAILS_EXPIRATION_THRESHOLD_SECONDS = 60L
private const val DETAILS_EXPIRATION_THRESHOLD_MILLI = DETAILS_EXPIRATION_THRESHOLD_SECONDS * 1000

internal interface LocalCardDetailsRepository {
    fun showCardDetails()
    fun hideCardDetails()
    fun getCardDetailsEvent(): LiveEvent<Boolean>
    fun clear()
}

internal class InMemoryLocalCardDetailsRepository(private val timer: Timer) :
    LocalCardDetailsRepository {

    private var _detailsLiveEvent = LiveEvent<Boolean>()
    private var showDetails: Boolean = false
        set(value) {
            field = value
            _detailsLiveEvent.postValue(value)
            if (showDetails) {
                timer.start(DETAILS_EXPIRATION_THRESHOLD_MILLI)
            }
        }

    init {
        _detailsLiveEvent.postValue(false)
        timer.setListener { hideCardDetails() }
    }

    override fun showCardDetails() {
        showDetails = true
    }

    override fun hideCardDetails() {
        showDetails = false
    }

    override fun getCardDetailsEvent() = _detailsLiveEvent

    override fun clear() {
        showDetails = false
        timer.stop()
    }
}

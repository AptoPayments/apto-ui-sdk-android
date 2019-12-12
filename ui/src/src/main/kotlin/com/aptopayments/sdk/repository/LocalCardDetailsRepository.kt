package com.aptopayments.sdk.repository

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.sdk.utils.DateProvider
import org.threeten.bp.LocalDateTime

private const val DETAILS_EXPIRATION_THRESHOLD_SECONDS = 60L
private const val DETAILS_EXPIRATION_THRESHOLD_MILLI = DETAILS_EXPIRATION_THRESHOLD_SECONDS * 1000

interface LocalCardDetailsRepository {
    fun saveCardDetails(details: CardDetails)
    fun getCardDetails(): CardDetails?
    fun getCardDetailsLiveData(): LiveData<CardDetails?>
    fun clear()
}

class InMemoryLocalCardDetailsRepository(private val dateProvider: DateProvider) : LocalCardDetailsRepository {

    private val handler = Handler()
    private var savedTime: LocalDateTime? = null
    private var _detailsLiveData = MutableLiveData<CardDetails?>()
    private var details: CardDetails? = null
        set(value) {
            field = value
            _detailsLiveData.postValue(value)
            clearHandler()
            handler.postDelayed({ clear() }, DETAILS_EXPIRATION_THRESHOLD_MILLI)
        }

    override fun saveCardDetails(details: CardDetails) {
        savedTime = dateProvider.localDateTime()
        this.details = details
    }

    override fun getCardDetails(): CardDetails? {
        if (!isTimeValid()) {
            details = null
            savedTime = null
        }
        return details
    }

    override fun getCardDetailsLiveData(): LiveData<CardDetails?> = _detailsLiveData

    override fun clear() {
        savedTime = null
        details = null
        clearHandler()
    }

    private fun clearHandler() = handler.removeCallbacksAndMessages(null)

    private fun isTimeValid(): Boolean =
        savedTime?.plusSeconds(DETAILS_EXPIRATION_THRESHOLD_SECONDS)?.isAfter(dateProvider.localDateTime()) ?: false
}

package com.aptopayments.sdk.features.analytics

import com.aptopayments.mobile.analytics.Event
import org.json.JSONObject

internal interface AnalyticsServiceContract {
    fun initialize(accessToken: String)
    fun track(event: Event, properties: JSONObject? = null)
    fun createUser(userId: String)
    fun loginUser(userId: String)
    fun logoutUser()
}

package com.aptopayments.sdk.features.common.analytics

import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import org.json.JSONObject

class AnalyticsManagerSpy : AnalyticsServiceContract {

    var initializeCalled = false
    var accessTokenPassed: String? = ""
    override fun initialize(accessToken: String) {
        initializeCalled = true
        accessTokenPassed = accessToken
    }

    var trackCalled = false
    var lastEvent: Event? = null
    var lastProperties: JSONObject? = null
    override fun track(event: Event, properties: JSONObject?) {
        trackCalled = true
        lastEvent = event
        lastProperties = properties
    }

    var createUserCalled = false
    override fun createUser(userId: String) {
        createUserCalled = true
    }

    var loginUserCalled = false
    override fun loginUser(userId: String) {
        loginUserCalled = true
    }

    var logoutUserCalled = false
    override fun logoutUser() {
        logoutUserCalled = true
    }
}

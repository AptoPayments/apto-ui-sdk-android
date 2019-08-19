package com.aptopayments.sdk.features.analytics

import android.app.Application
import com.aptopayments.core.analytics.Event
import org.json.JSONObject

internal class AnalyticsManager constructor(context: Application) : AnalyticsServiceContract {

    private var service = MixpanelAnalyticsService(context)

    override fun initialize(accessToken: String) {
        service.initialize(accessToken)
    }

    override fun track(event: Event, properties: JSONObject?) {
        service.track(event, properties)
    }

    override fun createUser(userId: String) {
        service.createUser(userId)
    }

    override fun loginUser(userId: String) {
        service.loginUser(userId)
    }

    override fun logoutUser() {
        service.logoutUser()
    }
}

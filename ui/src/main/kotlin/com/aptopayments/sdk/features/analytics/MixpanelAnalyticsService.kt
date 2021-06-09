package com.aptopayments.sdk.features.analytics

import android.app.Application
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

private const val USER_ID_PROFILE = "userId"

internal class MixpanelAnalyticsService(private val mContext: Application) : AnalyticsServiceContract {
    private var initialized = false
    private lateinit var mixpanel: MixpanelAPI
    private var createdUserId: String? = null
    private var loggedUserId: String? = null

    override fun initialize(accessToken: String) {
        mixpanel = MixpanelAPI.getInstance(mContext, accessToken)
        initialized = true
        createdUserId?.let {
            createUser(it)
            createdUserId = null
        }
        loggedUserId?.let {
            loginUser(it)
            loggedUserId = null
        }
    }

    override fun track(event: Event, properties: JSONObject?) {
        if (!initialized) return
        mixpanel.track(event.event, properties)
    }

    override fun createUser(userId: String) {
        if (!initialized) {
            createdUserId = userId
            return
        }
        mixpanel.alias(userId, mixpanel.distinctId)
        mixpanel.people.identify(mixpanel.distinctId)
        mixpanel.people.set(USER_ID_PROFILE, userId)
        val createUserJSON = JSONObject()
        createUserJSON.put(USER_ID_PROFILE, userId)
        mixpanel.registerSuperProperties(createUserJSON)
        track(Event.CreateUser)
    }

    override fun loginUser(userId: String) {
        if (!initialized) {
            loggedUserId = userId
            return
        }
        mixpanel.identify(userId)
        val loginUserJSON = JSONObject()
        loginUserJSON.put(USER_ID_PROFILE, userId)
        mixpanel.registerSuperProperties(loginUserJSON)
        track(Event.LoginUser)
    }

    override fun logoutUser() {
        loggedUserId = null
        createdUserId = null
        if (!initialized) return
        track(Event.LogoutUser)
        mixpanel.clearSuperProperties()
        mixpanel.flush()
    }
}

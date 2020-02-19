package com.aptopayments.sdk.utils

import android.content.Context
import android.content.res.Configuration
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol

class DarkThemeUtils(private val uiSdkProtocol: AptoUiSdkProtocol, context: Context) {

    private val appContext = context.applicationContext

    fun isEnabled(): Boolean {
        val mode = appContext.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        return uiSdkProtocol.cardOptions.darkThemeEnabled() && (mode == Configuration.UI_MODE_NIGHT_YES)
    }
}

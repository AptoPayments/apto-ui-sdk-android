package com.aptopayments.sdk.utils

import android.content.Context
import androidx.annotation.StringRes

interface StringProvider {
    fun provide(@StringRes id: Int): String
}

class StringProviderImpl(context: Context) : StringProvider {

    private val appContext = context.applicationContext

    override fun provide(id: Int) = appContext.getString(id)
}

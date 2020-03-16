package com.aptopayments.sdk.utils.deeplinks

import android.content.Context
import com.aptopayments.sdk.R

abstract class DeepLinkIntentGenerator(context: Context) {

    private val scheme = context.getString(R.string.apto_deep_link_scheme)
    abstract val host: String

    protected val parameters = mutableMapOf<String, String>()

    private fun constructDeepLink() = "$scheme://$host${getParameterUri()}"

    operator fun invoke() = constructDeepLink()

    private fun getParameterUri() =
        parameters.toList().joinToString(
            separator = "&",
            prefix = "?"
        ) { element -> "${element.first}=${element.second}" }

}

package com.aptopayments.sdk.utils.deeplinks

import com.aptopayments.sdk.R
import com.aptopayments.sdk.utils.StringProvider

abstract class DeepLinkIntentGenerator(stringProvider: StringProvider) {

    private val scheme = stringProvider.provide(R.string.apto_deep_link_scheme)
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

package com.aptopayments.sdk.utils.deeplinks

import android.content.Intent
import android.net.Uri

class IntentGenerator {

    operator fun invoke(deepLinkGenerator: DeepLinkIntentGenerator): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(deepLinkGenerator())
        return intent
    }
}

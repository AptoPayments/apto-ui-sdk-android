package com.aptopayments.sdk.features.card

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.cardFlow

class CardActivity : BaseActivity() {

    private val cardFlow: CardFlow?
        get() { return AptoPlatform.cardFlow?.get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        AptoPlatform.recreateAppComponent(application)
        super.onCreate(savedInstanceState)
        if (cardFlow == null) {
            this.finish()
        }
        attachFlow()
        if (savedInstanceState == null) {
            cardFlow?.start(animated = false)
        }
        else {
            cardFlow?.onRestoreInstanceState()
        }
    }

    override fun onResume() {
        super.onResume()
        attachFlow()
    }

    override fun onPause() {
        super.onPause()
        cardFlow?.detachFromActivity()
    }

    override fun onDestroy() {
        cardFlow?.detachFromActivity()
        super.onDestroy()
    }

    private fun attachFlow() {
        cardFlow?.attachTo(activity = this, fragmentContainer = R.id.fragmentContainer)
    }

    companion object {
        fun callingIntent(from: Context): Intent = Intent(from, CardActivity::class.java)
    }
}

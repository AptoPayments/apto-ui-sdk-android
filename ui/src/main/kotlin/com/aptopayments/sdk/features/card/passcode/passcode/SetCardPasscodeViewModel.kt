package com.aptopayments.sdk.features.card.passcode.passcode

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.setpin.CardPinViewModel

internal class SetCardPasscodeViewModel(analyticsManager: AnalyticsServiceContract) :
    CardPinViewModel(analyticsManager, Event.CardPasscodeSet) {

    override val title = "manage_card_set_passcode_set_title"
    override val description = "manage_card_set_passcode_set_description"
}

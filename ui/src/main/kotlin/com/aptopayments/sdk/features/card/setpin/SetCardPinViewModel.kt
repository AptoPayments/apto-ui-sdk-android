package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class SetCardPinViewModel(analyticsManager: AnalyticsServiceContract) :
    CardPinViewModel(analyticsManager, Event.ManageCardSetPin) {

    override val title = "manage_card_set_pin_title"
    override val description = "manage_card_set_pin_explanation"
}

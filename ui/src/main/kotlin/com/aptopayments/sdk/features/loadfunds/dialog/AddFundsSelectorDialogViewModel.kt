package com.aptopayments.sdk.features.loadfunds.dialog

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class AddFundsSelectorDialogViewModel(
    analytics: AnalyticsServiceContract
) : BaseViewModel() {

    val action = LiveEvent<Actions>()

    init {
        analytics.track(Event.AddFundsSelector)
    }

    fun onCardClicked() {
        action.postValue(Actions.CardClicked)
    }

    fun onAchClicked() {
        action.postValue(Actions.AchClicked)
    }

    sealed class Actions {
        object CardClicked : Actions()
        object AchClicked : Actions()
    }
}

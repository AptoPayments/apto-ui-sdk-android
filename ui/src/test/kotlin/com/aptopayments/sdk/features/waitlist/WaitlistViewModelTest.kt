package com.aptopayments.sdk.features.waitlist

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.waitlist.WaitlistViewModel
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.Test

class WaitlistViewModelTest {

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Test
    fun `test track is called on init`() {
        WaitlistViewModel(analyticsManager)

        verify(analyticsManager).track(Event.Waitlist)
    }
}

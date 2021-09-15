package com.aptopayments.sdk.features.passcode

import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class CreatePasscodeViewModelTest {

    private lateinit var sut: CreatePasscodeViewModel

    private val analyticsManager: AnalyticsManager = mock()

    @BeforeEach
    fun setUp() {
        sut = CreatePasscodeViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.CreatePasscodeStart)
        verifyNoMoreInteractions(analyticsManager)
    }
}

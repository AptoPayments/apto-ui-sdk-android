package com.aptopayments.sdk.features.passcode

import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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

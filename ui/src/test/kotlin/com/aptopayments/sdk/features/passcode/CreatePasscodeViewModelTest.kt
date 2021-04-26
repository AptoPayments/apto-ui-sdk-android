package com.aptopayments.sdk.features.passcode

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CreatePasscodeViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var sut: CreatePasscodeViewModel

    private val analyticsManager: AnalyticsManager = mock()

    @Before
    fun setUp() {
        sut = CreatePasscodeViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.CreatePasscodeStart)
        verifyNoMoreInteractions(analyticsManager)
    }
}

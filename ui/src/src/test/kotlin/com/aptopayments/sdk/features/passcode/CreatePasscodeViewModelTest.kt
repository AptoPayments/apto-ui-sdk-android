package com.aptopayments.sdk.features.passcode

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock

class CreatePasscodeViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var sut: CreatePasscodeViewModel
    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    @Before
    fun setUp() {
        sut = CreatePasscodeViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        verify(analyticsManager).track(Event.CreatePasscodeStart)
        verifyNoMoreInteractions(analyticsManager)
    }
}

package com.aptopayments.sdk.features.voip

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UNCHECKED_CAST")
class VoipViewModelTest : AndroidTest() {

    private lateinit var sut: VoipViewModel

    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    private val mockVoipHandler: VoipContract.Handler = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    @Before
    override fun setUp() {
        super.setUp()
        sut = VoipViewModel(aptoPlatform, analyticsManager, mockVoipHandler)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.ManageCardVoipCallStarted)
    }

    @Test
    fun `send digits send digits to voip handler`() {
        // Given
        val digits = "1"

        // When
        sut.sendDigits(digits)

        // Then
        verify(mockVoipHandler).sendDigits(digits)
    }

    @Test
    fun `disconnect ask voip handle to disconnect`() {
        // When
        sut.disconnect()

        // Then
        verify(mockVoipHandler).disconnect()
    }

    @Test
    fun `toggle change voip handle mute state`() {
        // Given
        given { mockVoipHandler.isMuted }.willReturn(false)

        // When
        sut.toggleMute()

        // Then
        verify(mockVoipHandler).isMuted
    }
}

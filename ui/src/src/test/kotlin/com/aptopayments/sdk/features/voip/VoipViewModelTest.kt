package com.aptopayments.sdk.features.voip

import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.voip.Action
import com.aptopayments.core.data.voip.VoipCall
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UNCHECKED_CAST")
class VoipViewModelTest : AndroidTest() {

    private lateinit var sut: VoipViewModel

    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    @Mock
    private lateinit var mockCall: VoipCall
    @Mock
    private lateinit var mockVoipHandler: VoipContract.Handler
    private val cardID = "TEST_CARD_ID"
    private val action = Action.LISTEN_PIN

    @Before
    override fun setUp() {
        super.setUp()
        sut = VoipViewModel(analyticsManager, mockVoipHandler)
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

package com.aptopayments.sdk.features.voip

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("UNCHECKED_CAST")
class VoipViewModelTest {

    private lateinit var sut: VoipViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()
    private val mockVoipHandler: VoipContract.Handler = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    @BeforeEach
    fun setUp() {
        sut = VoipViewModel(aptoPlatform, analyticsManager, mockVoipHandler)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.ManageCardVoipCallStarted)
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

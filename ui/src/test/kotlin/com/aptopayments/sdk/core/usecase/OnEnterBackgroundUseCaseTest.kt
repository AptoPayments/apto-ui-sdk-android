package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.core.platform.AuthStateProvider
import com.aptopayments.sdk.repository.AuthenticationRepository
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OnEnterBackgroundUseCaseTest {

    private val authState: AuthStateProvider = mock()
    private val authenticationRepo: AuthenticationRepository = mock()

    lateinit var sut: OnEnterBackgroundUseCase

    @BeforeEach
    fun before() {
        sut = OnEnterBackgroundUseCase(authState, authenticationRepo)
    }

    @Test
    fun whenNoAuthThenNoSaveNeedToAuthenticate() {
        whenever(authState.userTokenPresent()).thenReturn(false)

        sut()

        verify(authenticationRepo, times(0)).saveNeedToAuthenticate()
    }

    @Test
    fun whenAuthThenSaveNeedToAuthenticate() {
        whenever(authState.userTokenPresent()).thenReturn(true)

        sut()

        verify(authenticationRepo).saveNeedToAuthenticate()
    }
}

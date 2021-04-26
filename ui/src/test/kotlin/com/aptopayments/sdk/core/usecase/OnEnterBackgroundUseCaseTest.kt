package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.core.platform.AuthStateProvider
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

internal class OnEnterBackgroundUseCaseTest {

    private val authState: AuthStateProvider = mock()
    private val authenticationRepo: AuthenticationRepository = mock()

    lateinit var sut: OnEnterBackgroundUseCase

    @Before
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

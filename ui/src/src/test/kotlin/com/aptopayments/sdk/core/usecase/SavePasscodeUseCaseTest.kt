package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

private const val PIN = "1234"

internal class SavePasscodeUseCaseTest : UnitTest() {

    @Mock
    private lateinit var authenticationRepo: AuthenticationRepository
    lateinit var sut: SavePasscodeUseCase

    @Before
    fun before() {
        sut = SavePasscodeUseCase(authenticationRepo)
    }

    @Test
    fun whenSavePinThenRepoCalled() {
        val result = sut(PIN)

        assertTrue(result.isRight)
        verify(authenticationRepo).setPasscode(PIN)
    }
}

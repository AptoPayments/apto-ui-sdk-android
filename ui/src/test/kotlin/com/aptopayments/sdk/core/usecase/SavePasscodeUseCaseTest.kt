package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val PIN = "1234"

internal class SavePasscodeUseCaseTest {

    private val authenticationRepo: AuthenticationRepository = mock()
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

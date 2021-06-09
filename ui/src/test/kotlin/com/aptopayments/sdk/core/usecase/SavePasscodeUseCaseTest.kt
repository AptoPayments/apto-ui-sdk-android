package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val PIN = "1234"

internal class SavePasscodeUseCaseTest {

    private val authenticationRepo: AuthenticationRepository = mock()
    lateinit var sut: SavePasscodeUseCase

    @BeforeEach
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

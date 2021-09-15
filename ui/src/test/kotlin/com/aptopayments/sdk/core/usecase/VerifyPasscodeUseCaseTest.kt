package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.repository.AuthenticationRepository
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

private const val CORRECT_PIN = "1234"
private const val INCORRECT_PIN = "5678"

internal class VerifyPasscodeUseCaseTest {

    private val authenticationRepo: AuthenticationRepository = mock()

    lateinit var sut: VerifyPasscodeUseCase

    @BeforeEach
    fun before() {
        sut = VerifyPasscodeUseCase(authenticationRepo)
    }

    @Test
    fun `when Pin is correct then authTime is saved and true returned`() {
        whenever(authenticationRepo.getPasscode()).thenReturn(CORRECT_PIN)

        val result = sut(CORRECT_PIN)

        assertTrue(result.isRight)
        assertTrue((result as Either.Right).b)
        verify(authenticationRepo).saveAuthenticationTime()
    }

    @Test
    fun `when Pin is Not correct then authTime is Not saved and false returned`() {
        whenever(authenticationRepo.getPasscode()).thenReturn(CORRECT_PIN)

        val result = sut(INCORRECT_PIN)

        assertTrue(result.isRight)
        assertFalse((result as Either.Right).b)
        verify(authenticationRepo, times(0)).saveAuthenticationTime()
    }
}

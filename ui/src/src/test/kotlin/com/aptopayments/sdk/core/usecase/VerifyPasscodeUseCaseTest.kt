package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import kotlin.test.assertTrue

private const val CORRECT_PIN = "1234"
private const val INCORRECT_PIN = "5678"

internal class VerifyPasscodeUseCaseTest : UnitTest() {

    @Mock
    private lateinit var authenticationRepo: AuthenticationRepository

    lateinit var sut: VerifyPasscodeUseCase

    @Before
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

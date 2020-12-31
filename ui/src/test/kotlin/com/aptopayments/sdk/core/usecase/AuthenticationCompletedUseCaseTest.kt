package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import kotlin.test.assertTrue

class AuthenticationCompletedUseCaseTest {

    private val authenticationRepository: AuthenticationRepository = mock()

    private val sut = AuthenticationCompletedUseCase(authenticationRepository)

    @Test
    fun `when invoked then saveAuthenticatedCorrectly called`() {
        val result = sut.invoke()

        assertTrue(result.isRight)
        verify(authenticationRepository).saveAuthenticatedCorrectly()
    }
}

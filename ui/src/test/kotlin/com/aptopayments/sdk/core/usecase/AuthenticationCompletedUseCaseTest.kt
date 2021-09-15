package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.AuthenticationRepository
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.Test
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

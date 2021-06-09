package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BiometricsAuthCorrectUseCaseTest {

    private val authenticationRepository: AuthenticationRepository = mock()

    lateinit var sut: BiometricsAuthCorrectUseCase

    @BeforeEach
    fun configure() {
        sut = BiometricsAuthCorrectUseCase(authenticationRepository)
    }

    @Test
    fun whenExecutedThenAuthSaved() {
        val result = sut()

        assert(result.isRight)
        verify(authenticationRepository).saveAuthenticationTime()
    }
}

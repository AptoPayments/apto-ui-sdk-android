package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

internal class BiometricsAuthCorrectUseCaseTest {

    private val authenticationRepository: AuthenticationRepository = mock()

    lateinit var sut: BiometricsAuthCorrectUseCase

    @Before
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

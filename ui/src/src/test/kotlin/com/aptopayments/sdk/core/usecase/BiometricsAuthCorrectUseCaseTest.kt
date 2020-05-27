package com.aptopayments.sdk.core.usecase

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

internal class BiometricsAuthCorrectUseCaseTest : UnitTest() {

    @Mock
    private lateinit var authenticationRepository: AuthenticationRepository

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

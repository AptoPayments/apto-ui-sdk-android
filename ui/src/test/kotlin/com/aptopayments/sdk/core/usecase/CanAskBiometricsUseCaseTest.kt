package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class CanAskBiometricsUseCaseTest {

    private val repo: AuthenticationRepository = mock()
    private val biometricsWrapper: BiometricWrapper = mock()

    lateinit var sut: CanAskBiometricsUseCase

    @Before
    fun configure() {
        sut = CanAskBiometricsUseCase(repo, biometricsWrapper)
    }

    @Test
    fun `test UseCase behaviour`() {
        val list = mutableListOf(
            Triple(first = true, second = true, third = true),
            Triple(first = false, second = true, third = false),
            Triple(first = true, second = false, third = false),
            Triple(first = false, second = false, third = false)
        )

        list.forEach { (biometricsEnabled, canAskBiometrics, expectedResult) ->
            configurePreConditions(biometricsEnabled, canAskBiometrics)

            val result = sut()

            assertTrue { result.isRight }
            assertEquals((result as Either.Right).b, expectedResult)
        }
    }

    private fun configurePreConditions(biometricsEnabled: Boolean, canAskBiometric: Boolean) {
        whenever(repo.isBiometricsEnabledByUser()).thenReturn(biometricsEnabled)
        whenever(biometricsWrapper.canAskBiometric()).thenReturn(canAskBiometric)
    }
}

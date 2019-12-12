package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.AuthStateProvider
import com.aptopayments.sdk.repository.AuthenticationRepository
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.inject
import org.koin.test.mock.declareMock

internal class ShouldAuthenticateOnStartUpUseCaseTest : UnitTest() {

    private val authStateProvider: AuthStateProvider by inject()
    private val authenticationRepo: AuthenticationRepository by inject()
    lateinit var sut: ShouldAuthenticateOnStartUpUseCase

    @Before
    fun configureKoin() {
        startKoin {
            modules(listOf(useCaseModule, applicationModule))
        }
        declareMock<AuthStateProvider>()
        declareMock<AuthenticationRepository>()
        sut = ShouldAuthenticateOnStartUpUseCase(authStateProvider, authenticationRepo)
    }

    @Test
    fun `when authenticateOnStartup is false then no authentication needed`() {
        configureCardOptions(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
    }

    @Test
    fun `when don't have user token then no authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)

        verify(authStateProvider).userTokenPresent()
    }

    @Test
    fun `when authTimeValid no authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
        verify(authenticationRepo).isAuthenticationNeedSaved()
        verifyNoMoreInteractions(authenticationRepo)
    }

    @Test
    fun `when first login no authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(true)
        configureAuthTimeInvalid(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
        verify(authenticationRepo).isAuthTimeInvalid()
    }

    private fun configureAuthTimeInvalid(authTimeInvalid: Boolean) {
        given(authenticationRepo.isAuthTimeInvalid()).willReturn(authTimeInvalid)
    }

    @Test
    fun `when no pin no authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(true)
        configureAuthTimeInvalid(true)
        configurePinSet(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
    }

    @Test
    fun `when pin authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(true)
        configureAuthTimeInvalid(true)
        configurePinSet(true)

        val result = sut()

        assertRightEitherIsEqualTo(result, true)
    }

    private fun assertRightEitherIsEqualTo(result: Either<Failure, Any>, rightValue: Boolean) {
        result shouldBeInstanceOf Either::class.java
        result.isRight shouldEqual true
        result.either({}, { it shouldBe rightValue })
    }

    private fun configurePinSet(pinSet: Boolean) {
        given(authenticationRepo.isPinSet()).willReturn(pinSet)
    }

    private fun configureAuthNeeded(need: Boolean) {
        given(authenticationRepo.isAuthenticationNeedSaved()).willReturn(need)
    }

    private fun configureUserTokenPresent(userTokenPresent: Boolean) {
        given(authStateProvider.userTokenPresent()).willReturn(userTokenPresent)
    }

    private fun configureCardOptions(authenticateOnStartup: Boolean) {
        AptoUiSdk.cardOptions = CardOptions(authenticateOnStartup = authenticateOnStartup)
    }

}

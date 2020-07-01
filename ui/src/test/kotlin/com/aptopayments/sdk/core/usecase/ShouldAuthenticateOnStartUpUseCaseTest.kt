package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.UnitTest
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
import org.mockito.Mock

internal class ShouldAuthenticateOnStartUpUseCaseTest : UnitTest() {

    @Mock
    private lateinit var authStateProvider: AuthStateProvider

    @Mock
    private lateinit var authenticationRepo: AuthenticationRepository

    lateinit var sut: ShouldAuthenticateOnStartUpUseCase

    @Before
    fun setUp() {
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
        verify(authenticationRepo).isAuthTimeInvalid()
        verifyNoMoreInteractions(authenticationRepo)
    }

    @Test
    fun `when first login no authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(false)
        configureAuthTimeInvalid(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
        verify(authenticationRepo).isAuthTimeInvalid()
    }

    @Test
    fun `when logged and went to background then authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(true)
        configurePasscodeSet(true)

        val result = sut()

        assertRightEitherIsEqualTo(result, true)
    }

    private fun configureAuthTimeInvalid(authTimeInvalid: Boolean) {
        given(authenticationRepo.isAuthTimeInvalid()).willReturn(authTimeInvalid)
    }

    @Test
    fun `when no passcode no authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(true)
        configureAuthTimeInvalid(true)
        configurePasscodeSet(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
    }

    @Test
    fun `when logged but time passed then authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(false)
        configureAuthTimeInvalid(true)
        configurePasscodeSet(false)

        val result = sut()

        assertRightEitherIsEqualTo(result, false)
    }

    @Test
    fun `when passcode authentication needed`() {
        configureCardOptions(true)
        configureUserTokenPresent(true)
        configureAuthNeeded(true)
        configureAuthTimeInvalid(true)
        configurePasscodeSet(true)

        val result = sut()

        assertRightEitherIsEqualTo(result, true)
    }

    private fun assertRightEitherIsEqualTo(result: Either<Failure, Any>, rightValue: Boolean) {
        result shouldBeInstanceOf Either::class.java
        result.isRight shouldEqual true
        result.either({}, { it shouldBe rightValue })
    }

    private fun configurePasscodeSet(passcodeSet: Boolean) {
        given(authenticationRepo.isPasscodeSet()).willReturn(passcodeSet)
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

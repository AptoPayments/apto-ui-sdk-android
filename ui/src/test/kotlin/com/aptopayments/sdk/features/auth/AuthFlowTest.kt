package com.aptopayments.sdk.features.auth

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.user.PhoneDataPoint
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationFragment
import com.aptopayments.sdk.features.auth.inputemail.InputEmailFragment
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragment
import com.aptopayments.sdk.features.auth.verification.EmailVerificationFragment
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationFragment
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class AuthFlowTest : UnitTest() {

    private lateinit var sut: AuthFlow

    private val mockFragmentFactory: FragmentFactory = mock()

    private lateinit var mockDataPoint: PhoneDataPoint
    private lateinit var countries: List<Country>

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Before
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single<AnalyticsServiceContract> { analyticsManager }
                    single { mockFragmentFactory }
                }
            )
        }
    }

    @Test
    fun `should use the factory to instantiate InputPhoneFragment as first fragment`() {
        // Given
        sut = AuthFlow(TestDataProvider.provideContextConfiguration(), onBack = {}, onFinish = {})
        val tag = "InputPhoneFragment"
        val fragmentPhoneInputDouble = mock<InputPhoneFragment> { on { TAG } doReturn tag }
        countries = TestDataProvider.provideContextConfiguration().projectConfiguration.allowedCountries
        given {
            mockFragmentFactory.inputPhoneFragment(
                allowedCountries = countries,
                tag = tag
            )
        }.willReturn(fragmentPhoneInputDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).inputPhoneFragment(
            allowedCountries = countries,
            tag = tag
        )
    }

    @Test
    fun `on phone verification started to show phone verification fragment`() {

        // Given
        val tag = "PhoneVerificationFragment"
        sut = AuthFlow(TestDataProvider.provideContextConfiguration(), onBack = {}, onFinish = {})
        val fragmentPhoneVerificationDouble = mock<PhoneVerificationFragment> { on { TAG } doReturn tag }
        val verification = Verification("", "phone")
        given {
            mockFragmentFactory.phoneVerificationFragment(verification = verification, tag = tag)
        }.willReturn(fragmentPhoneVerificationDouble)

        // When
        sut.onPhoneVerificationStarted(verification)

        // Then
        verify(mockFragmentFactory).phoneVerificationFragment(verification = verification, tag = tag)
    }

    @Test
    fun `on email verification started to show email verification fragment`() {

        // Given
        val tag = "EmailVerificationFragment"
        sut = AuthFlow(TestDataProvider.provideContextConfigurationEmail(), onBack = {}, onFinish = {})
        val fragmentEmailVerifyDouble = mock<EmailVerificationFragment> { on { TAG } doReturn tag }
        val verification = Verification("", "phone")
        given {
            mockFragmentFactory.emailVerificationFragment(verification = verification, tag = tag)
        }.willReturn(fragmentEmailVerifyDouble)

        // When
        sut.onEmailVerificationStarted(verification)

        // Then
        verify(mockFragmentFactory).emailVerificationFragment(verification = verification, tag = tag)
    }

    @Test
    fun `on email verification passed to show phone input fragment based on output points to input phone fragment`() {

        // Given
        val tag = "InputPhoneFragment"
        sut = AuthFlow(TestDataProvider.provideContextConfigurationEmail(), onBack = {}, onFinish = {})
        countries = TestDataProvider.provideContextConfiguration().projectConfiguration.allowedCountries
        val fragmentPhoneInputDouble = mock<InputPhoneFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.inputPhoneFragment(countries, tag)
        }.willReturn(fragmentPhoneInputDouble)
        mockDataPoint = PhoneDataPoint(
            verification = Verification(
                "", "phone",
                VerificationStatus.FAILED, "", Verification("", "phone")
            )
        )

        // When
        sut.onEmailVerificationPassed(mockDataPoint)

        // Then
        verify(mockFragmentFactory).inputPhoneFragment(countries, tag)
    }

    @Test
    fun `on phone verification passed to show email input fragment based on output points to input email fragment`() {

        // Given
        val tag = "InputEmailFragment"
        sut = AuthFlow(TestDataProvider.provideContextConfigurationEmail(), onBack = {}, onFinish = {})
        countries = TestDataProvider.provideContextConfiguration().projectConfiguration.allowedCountries
        val fragmentEmailDouble = mock<InputEmailFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.inputEmailFragment(tag)
        }.willReturn(fragmentEmailDouble)
        mockDataPoint = PhoneDataPoint(
            verification = Verification(
                "", "email",
                VerificationStatus.FAILED, "", Verification("", "email")
            )
        )

        // When
        sut.onPhoneVerificationPassed(mockDataPoint)

        // Then
        verify(mockFragmentFactory).inputEmailFragment(tag)
    }

    @Test
    fun `on phone verification passed to show verify birthdate fragment based on output points to verify birthdate fragment`() {

        // Given
        val tag = "BirthdateVerificationFragment"
        sut = AuthFlow(TestDataProvider.provideContextConfigurationEmail(), onBack = {}, onFinish = {})
        countries = TestDataProvider.provideContextConfiguration().projectConfiguration.allowedCountries
        val verification = Verification(
            "", "phone",
            VerificationStatus.FAILED, "", Verification("", "birthdate")
        )
        mockDataPoint = PhoneDataPoint(verification = verification)
        val fragmentBirthdayDouble = mock<BirthdateVerificationFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.birthdateVerificationFragment(verification, tag)
        }.willReturn(fragmentBirthdayDouble)

        // When
        sut.onPhoneVerificationPassed(mockDataPoint)

        // Then
        verify(mockFragmentFactory).birthdateVerificationFragment(verification, tag)
    }

    @Test
    fun `on email verification passed to show verify birthdate fragment based on output points to verify birthdate fragment`() {

        // Given
        val tag = "BirthdateVerificationFragment"
        sut = AuthFlow(TestDataProvider.provideContextConfigurationEmail(), onBack = {}, onFinish = {})
        countries = TestDataProvider.provideContextConfiguration().projectConfiguration.allowedCountries
        val verification = Verification(
            "", "email",
            VerificationStatus.FAILED, "", Verification("", "birthdate")
        )
        mockDataPoint = PhoneDataPoint(verification = verification)
        val fragmentBirthdayDouble = mock<BirthdateVerificationFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.birthdateVerificationFragment(verification, tag)
        }.willReturn(fragmentBirthdayDouble)

        // When
        sut.onEmailVerificationPassed(mockDataPoint)

        // Then
        verify(mockFragmentFactory).birthdateVerificationFragment(verification, tag)
    }
}

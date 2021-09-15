package com.aptopayments.sdk.features.verification

import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationContract
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationFragment
import com.aptopayments.sdk.features.auth.verification.VerificationViewModel
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

class PhoneVerificationFragmentTest : UnitTest() {
    // Collaborators
    private val viewModel: VerificationViewModel = mock()
    private val verification = Verification("", "phone")
    private lateinit var sut: PhoneVerificationFragment

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                module {
                    viewModel { viewModel }
                }
            )
        }
        sut = PhoneVerificationFragment.newInstance(verification)
        sut.verification = verification
    }

    @Test
    fun `on fragment presented call view model to fetch verification`() {
        // When
        sut.onPresented()

        // Then
        verify(viewModel, never()).restartVerification { result -> result.isLeft }
    }

    @Test
    fun `on back pressed notify delegate`() {
        // Given
        val delegate = mock<PhoneVerificationContract.Delegate>()
        sut.delegate = delegate

        // When
        sut.onBackPressed()

        // Then
        verify(delegate).onBackFromPhoneVerification()
    }

    @Suppress("USELESS_IS_CHECK")
    @Test
    fun `phone verification fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val sut = FragmentFactoryImpl()
        val tag = "SOME_TAG"

        // When
        val fragment = sut.phoneVerificationFragment(verification, tag)

        // Then
        assert(fragment is PhoneVerificationFragment)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

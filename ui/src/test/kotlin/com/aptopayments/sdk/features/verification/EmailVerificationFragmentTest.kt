package com.aptopayments.sdk.features.verification

import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.verification.EmailVerificationContract
import com.aptopayments.sdk.features.auth.verification.EmailVerificationFragment
import com.aptopayments.sdk.features.auth.verification.VerificationViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

class EmailVerificationFragmentTest : UnitTest() {
    // Collaborators
    private val viewModel: VerificationViewModel = mock()
    private val verification = Verification("", "email")
    private lateinit var sut: EmailVerificationFragment

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                module {
                    viewModel { viewModel }
                }
            )
        }
        sut = EmailVerificationFragment.newInstance(verification)
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
        val delegate = mock<EmailVerificationContract.Delegate>()
        sut.delegate = delegate

        // When
        sut.onBackPressed()

        // Then
        verify(delegate).onBackFromEmailVerification()
    }

    @Suppress("USELESS_IS_CHECK")
    @Test
    fun `email verification fragment return expected fragment and set TAG`() {
        // Given
        val sut = FragmentFactoryImpl()
        val tag = "SOME_TAG"

        // When
        val fragment = sut.emailVerificationFragment(verification, tag)

        // Then
        assert(fragment is EmailVerificationFragment)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

package com.aptopayments.sdk.features.verification

import com.aptopayments.sdk.AndroidTest
import com.aptopayments.core.data.config.UITheme.THEME_1
import com.aptopayments.core.data.user.Verification
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.verification.EmailVerificationContract
import com.aptopayments.sdk.features.auth.verification.EmailVerificationFragmentThemeOne
import com.aptopayments.sdk.features.auth.verification.VerificationViewModel
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals

class EmailVerificationFragmentTest : AndroidTest() {
    // Collaborators
    @Mock
    private lateinit var viewModel: VerificationViewModel
    private val verification = Verification("", "email")
    private lateinit var sut: EmailVerificationFragmentThemeOne

    @Before
    override fun setUp() {
        super.setUp()
        sut = EmailVerificationFragmentThemeOne.newInstance(verification)
        sut.mViewModel = viewModel
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
        val delegate = Mockito.mock(EmailVerificationContract.Delegate::class.java)
        sut.delegate = delegate

        // When
        sut.onBackPressed()

        // Then
        verify(delegate).onBackFromEmailVerification()
    }

    @Test
    fun `email verification fragment for theme1 return expected fragment and set TAG`() {
        // Given
        val sut = FragmentFactoryImpl()
        val tag = "SOME_TAG"

        // When
        val fragment = sut.emailVerificationFragment(THEME_1, verification, tag)

        //Then
        assert(fragment is EmailVerificationFragmentThemeOne)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

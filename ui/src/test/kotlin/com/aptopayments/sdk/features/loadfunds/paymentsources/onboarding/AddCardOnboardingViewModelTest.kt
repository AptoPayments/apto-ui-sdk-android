package com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding.AddCardOnboardingViewModel.Actions
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals

private const val CARD_ID = "crd_1234"

class AddCardOnboardingViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val repository: PaymentSourcesRepository = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    private lateinit var sut: AddCardOnboardingViewModel

    @Before
    fun setUp() {
        sut = AddCardOnboardingViewModel(CARD_ID, repository, aptoPlatform)
    }

    @Test
    fun `when continue clicked then accepted is saved in repo`() {
        sut.onContinueClicked()

        verify(repository).acceptedOnboarding()
    }

    @Test
    fun `when continue clicked then continueEvent is fired`() {
        sut.onContinueClicked()

        assertEquals(Actions.Continue, sut.actions.getOrAwaitValue())
    }
}

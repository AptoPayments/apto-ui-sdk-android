package com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding

import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding.AddCardOnboardingViewModel.Actions
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

private const val CARD_ID = "crd_1234"

@ExtendWith(InstantExecutorExtension::class)
class AddCardOnboardingViewModelTest {

    private val repository: PaymentSourcesRepository = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    private lateinit var sut: AddCardOnboardingViewModel

    @BeforeEach
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

package com.aptopayments.sdk.features.loadfunds.dialog

import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.mockito.kotlin.*
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstantExecutorExtension::class)
class AddFundsSelectorDialogViewModelTest {

    private val analyticsManager: AnalyticsServiceContract = mock()

    private val sut = AddFundsSelectorDialogViewModel(analyticsManager)

    @Test
    fun `when Dialog is shown then correct tracking is made`() {
        verify(analyticsManager).track(Event.AddFundsSelector)
    }

    @Test
    fun `when onAchClicked then correct action is raised`() {
        sut.onAchClicked()

        assertThat(
            sut.action.getOrAwaitValue(),
            instanceOf(AddFundsSelectorDialogViewModel.Actions.AchClicked::class.java)
        )
    }

    @Test
    fun `when onCardClicked then correct action is raised`() {
        sut.onCardClicked()

        assertThat(
            sut.action.getOrAwaitValue(),
            instanceOf(AddFundsSelectorDialogViewModel.Actions.CardClicked::class.java)
        )
    }
}

package com.aptopayments.sdk.features.loadfunds.dialog

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@Suppress("UNCHECKED_CAST")
class AddFundsSelectorDialogViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

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

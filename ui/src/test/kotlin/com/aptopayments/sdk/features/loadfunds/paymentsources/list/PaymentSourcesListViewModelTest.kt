package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListViewModel.Actions
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.TestDispatchers
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.aptopayments.sdk.utils.runBlockingTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class PaymentSourcesListViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val repo: PaymentSourcesRepository = mock()
    private val elementMapper = PaymentSourceElementMapper()
    private val selectedPaymentSource: LiveData<PaymentSource?> = mock()

    private val sut = PaymentSourcesListViewModel(repo, elementMapper, TestDispatchers(coroutineRule.testDispatcher))

    @Test
    fun `when newPaymentSource called then correct action is fired`() {
        sut.newPaymentSource()

        assertEquals(Actions.NewPaymentSource, sut.actions.getOrAwaitValue())
    }

    @Test
    fun `when selectPaymentSource called then is saved in repo`() {
        val source = TestDataProvider.providePaymentSourcesCard()

        sut.selectPaymentSource(source)

        verify(repo).selectPaymentSourceLocally(source)
    }

    @Test
    fun `when selectPaymentSource called then correct action is fired`() {
        sut.newPaymentSource()

        assertEquals(Actions.NewPaymentSource, sut.actions.getOrAwaitValue())
    }

    @Test
    fun `when card is provided then added the trailing element correctly`() = coroutineRule.runBlockingTest {
        val card = TestDataProvider.providePaymentSourcesCard()
        whenever(repo.getPaymentSourceList()).thenReturn(listOf(card).right())
        whenever(repo.selectedPaymentSource).thenReturn(selectedPaymentSource)
        whenever(selectedPaymentSource.value).thenReturn(null)

        sut.onPresented()

        val list = sut.sourceList.getOrAwaitValue()

        assertTrue(list.size == 2)
        assertTrue(list[0].isPreferred)
        assertEquals(list[0].source, card)
        assertEquals(list[0].type, PaymentSourcesListItem.Type.EXISTING)

        assertNull(list[1].source)
        assertEquals(list[1].type, PaymentSourcesListItem.Type.NEW)
    }
}

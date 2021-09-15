package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.CoroutineDispatcherTest
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListViewModel.Actions
import com.aptopayments.sdk.utils.TestDispatchers
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
class PaymentSourcesListViewModelTest : CoroutineDispatcherTest {

    override lateinit var dispatcher: TestCoroutineDispatcher

    private val repo: PaymentSourcesRepository = mock()
    private val elementMapper = PaymentSourceElementMapper()
    private val selectedPaymentSource: StateFlow<PaymentSource?> = mock()

    private lateinit var sut: PaymentSourcesListViewModel

    @BeforeEach
    internal fun setUp() {
        sut = PaymentSourcesListViewModel(repo, elementMapper, TestDispatchers(dispatcher))
    }

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
    fun `when card is provided then added the trailing element correctly`() = dispatcher.runBlockingTest {
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

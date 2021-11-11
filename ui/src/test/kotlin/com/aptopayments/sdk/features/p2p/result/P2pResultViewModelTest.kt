package com.aptopayments.sdk.features.p2p.result

import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.extensions.formatForTransactionDetails
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class P2pResultViewModelTest : UnitTest() {

    private lateinit var sut: P2pResultViewModel

    @Test
    internal fun `given result Processed when created then status is correct`() {
        val response = TestDataProvider.provideP2pTransferResponse(status = PaymentStatus.PROCESSED)
        createSut(response)

        assertEquals(PaymentStatus.PROCESSED, sut.state.status)
    }

    @Test
    internal fun `given result Pending when created then status is correct`() {
        val response = TestDataProvider.provideP2pTransferResponse(status = PaymentStatus.PENDING)
        createSut(response)

        assertEquals(PaymentStatus.PENDING, sut.state.status)
    }

    @Test
    internal fun `given result Processed when created then state is correct`() {
        val response = TestDataProvider.provideP2pTransferResponse()
        createSut(response)

        assertEquals("${response.recipientName.firstName} ${response.recipientName.lastName}", sut.state.name)
        assertEquals(response.amount.toString(), sut.state.amount)
        assertEquals(response.createdAt.formatForTransactionDetails(), sut.state.time)
    }

    @Test
    internal fun `when cta clicked then correct action is emitted`() {
        createSut(TestDataProvider.provideP2pTransferResponse())
        sut.onCtaClicked()

        val action = sut.action.getOrAwaitValue()

        assertTrue(action is P2pResultViewModel.Action.CtaClicked)
    }

    private fun createSut(result: P2pTransferResponse) {
        sut = P2pResultViewModel(result)
    }
}

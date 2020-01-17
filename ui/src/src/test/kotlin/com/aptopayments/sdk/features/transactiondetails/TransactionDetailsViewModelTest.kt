package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.Money
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.data.transaction.*
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.util.*

private const val representation = "$ 10.11"
private const val DOLLAR = "$"
private const val EURO = "€"

internal class TransactionDetailsViewModelTest : UnitTest(){

    @Mock
    lateinit var analyticsManager: AnalyticsServiceContract
    @Mock
    lateinit var transaction: Transaction
    @Mock
    lateinit var store : Store
    @Mock
    lateinit var storeAdress : StoreAddress
    @Mock
    lateinit var country : Country
    @Mock
    lateinit var merchant: Merchant
    @Mock
    lateinit var mcc: MCC
    @Mock
    lateinit var localAmount : Money
    @Mock
    lateinit var nativeBalance : Money

    private lateinit var sut: TransactionDetailsViewModel

    @Before
    fun before() {
        sut = TransactionDetailsViewModel(transaction, analyticsManager)
    }


    @Test
    fun `when viewLoaded then track is called`() {
        sut.viewLoaded()

        verify(analyticsManager).track(Event.TransactionDetail)
    }

    @Test
    fun `when transactionIs Completed then isDeclined is false`() {
        configureState(Transaction.TransactionState.COMPLETE)

        assertFalse(sut.isDeclined)
    }

    @Test
    fun `when transactionIs declined then isDeclined is true`() {
        configureState(Transaction.TransactionState.DECLINED)

        assertTrue(sut.isDeclined)
    }

    private fun configureState(state: Transaction.TransactionState) {
        whenever(transaction.state).thenReturn(state)
    }

    @Test
    fun `createdAt is correctly formatted`() {
        val year = 2020
        whenever(transaction.createdAt).thenReturn(Date(year -1900,1,14, 17,22))

        assertEquals(sut.createdAt, "Feb 14, 2020 5:22 PM")
    }

    @Test
    fun `when storeAdress is not null and not equal the country then address is correct`() {
        configureStore()
        configureAddress()
        configureCountry()
        val country = "Spain"
        whenever(this.country.name).thenReturn(country)
        val address = "Ronda de Sant Pere 52, Barcelona, Spain"
        whenever(storeAdress.toStringRepresentation()).thenReturn(address)

        assertEquals(sut.addressName, address)
    }

    @Test
    fun `when storeAdress is not null and equal the country then address is empty`() {
        configureStore()
        configureAddress()
        configureCountry()
        val country = "Spain"
        whenever(this.country.name).thenReturn(country)
        whenever(storeAdress.toStringRepresentation()).thenReturn(country)

        assertEquals(sut.addressName, "")
    }

    @Test
    fun `when mcc is null then mcc name is null`() {
        configureMerchant()
        whenever(merchant.mcc).thenReturn(null)

        assertNull(sut.mccName)
    }

    @Test
    fun `when transaction have mcc then mccName is correct`() {
        configureMerchant()
        whenever(merchant.mcc).thenReturn(mcc)
        val localizedString = "localized_string"
        whenever(mcc.toLocalizedString()).thenReturn(localizedString)

        assertEquals(sut.mccName, localizedString)
    }

    @Test
    fun `fundingSource name is correctly generated`() {
        val deviceType = Transaction.TransactionDeviceType.ECOMMERCE
        whenever(transaction.deviceType()).thenReturn(deviceType)

        assertEquals(sut.deviceType, deviceType.toLocalizedString())
    }

    @Test
    fun `transactionType is correctly generated`() {
        val transactionType = Transaction.TransactionType.CREDIT
        whenever(transaction.transactionType).thenReturn(transactionType)

        assertEquals(sut.transactionType, transactionType.toLocalizedString())
    }

    @Test
    fun `transactionStatus is correctly generated`() {
        val state = Transaction.TransactionState.COMPLETE
        configureState(state)

        assertEquals(sut.transactionStatus, state.toLocalizedString())
    }

    @Test
    fun `when declineCode is null then declinedDescription name is null`() {
        whenever(transaction.declineCode).thenReturn(null)

        assertNull(sut.declinedDescription)
    }

    @Test
    fun `when transaction have declineCode then declinedDescription name is correct`() {
        val code = DeclineCode.DECLINE_NSF
        whenever(transaction.declineCode).thenReturn(code)

        assertEquals(sut.declinedDescription, code.toLocalizedString())
    }

    @Test
    fun `when transaction have description then transactionDescription is correct`() {
        val description = "Transaction description"
        whenever(transaction.transactionDescription).thenReturn(description)

        assertEquals(sut.transactionDescription, description)
    }

    @Test
    fun `localAmountRepresentation is correctly generated`() {
        whenever(transaction.getLocalAmountRepresentation()).thenReturn(representation)

        assertEquals(sut.localAmountRepresentation, representation)
    }

    @Test
    fun `when nativeBalanceRepresentation is Blank then nativeBalanceRepresentation is empty string`() {
        whenever(transaction.getNativeBalanceRepresentation()).thenReturn("")

        assertEquals(sut.nativeBalanceRepresentation, "")
    }

    @Test
    fun `when local and native currencies are equal then nativeBalanceRepresentation is empty string`() {
        whenever(transaction.getNativeBalanceRepresentation()).thenReturn(representation)
        configureLocalAmountCurrency(DOLLAR)
        configureNativeBalanceCurrency(DOLLAR)

        assertEquals(sut.nativeBalanceRepresentation, "")
    }

    @Test
    fun `when nativeBalanceRepresentation is not blank and currincies are different then nativeBalanceRepresentation is correct`() {
        whenever(transaction.getNativeBalanceRepresentation()).thenReturn(representation)
        configureLocalAmountCurrency(DOLLAR)
        configureNativeBalanceCurrency(EURO)

        assertEquals(sut.nativeBalanceRepresentation, "≈ $representation")
    }

    private fun configureMerchant() {
        whenever(transaction.merchant).thenReturn(merchant)
    }

    private fun configureCountry() {
        whenever(storeAdress.country).thenReturn(country)
    }

    private fun configureAddress() {
        whenever(store.address).thenReturn(storeAdress)
    }

    private fun configureStore() {
        whenever(transaction.store).thenReturn(store)
    }

    private fun configureNativeBalanceCurrency(currency2: String) {
        whenever(transaction.nativeBalance).thenReturn(nativeBalance)
        whenever(nativeBalance.currency).thenReturn(currency2)
    }

    private fun configureLocalAmountCurrency(currency: String) {
        whenever(transaction.localAmount).thenReturn(localAmount)
        whenever(localAmount.currency).thenReturn(currency)
    }
}

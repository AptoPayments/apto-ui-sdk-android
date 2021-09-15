package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.transaction.*
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.util.TimeZone

private const val representation = "$ 10.11"
private const val DOLLAR = "$"
private const val EURO = "€"

internal class TransactionDetailsViewModelTest {

    private val analyticsManager: AnalyticsServiceContract = mock()
    private val transaction: Transaction = mock()
    private val store: Store = mock()
    private val storeAdress: StoreAddress = mock()
    private val country: Country = mock()
    private val merchant: Merchant = mock()
    private val mcc: MCC = mock()
    private val localAmount: Money = mock()
    private val nativeBalance: Money = mock()

    private lateinit var sut: TransactionDetailsViewModel

    @BeforeEach
    fun before() {
        sut = TransactionDetailsViewModel(transaction, analyticsManager)
    }

    @Test
    fun `when init then track is called`() {
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
        val date = ZonedDateTime.of(2020, 2, 14, 17, 22, 0, 0, ZoneOffset.UTC)

        whenever(transaction.createdAt).thenReturn(date)

        assertEquals(sut.createdAt, "Feb 14, 2020 7:22 PM")
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

    companion object {
        private lateinit var timezone: TimeZone

        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            timezone = TimeZone.getDefault()
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+2:00"))
        }

        @AfterAll
        @JvmStatic
        fun afterClass() {
            TimeZone.setDefault(timezone)
        }
    }
}

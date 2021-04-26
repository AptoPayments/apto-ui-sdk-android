package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Merchant
import com.aptopayments.mobile.data.transaction.Store
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.core.extension.iconResource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val LATITUDE = 1.1
private const val LONGITUDE = 2.2
private val ICON = MCC.Icon.OTHER

internal class MapConfigurationFactoryTest {

    private val transaction: Transaction = mock()
    private val store: Store = mock()
    private val merchant: Merchant = mock()
    private val mcc: MCC = mock()

    val sut = MapConfigurationFactory()

    @Test
    fun `when store is null then no configuration`() {
        whenever(transaction.store).thenReturn(null)
        val result = sut.create(transaction)

        assertNull(result)
    }

    @Test
    fun `when latitude is null then no configuration`() {
        whenever(transaction.store).thenReturn(store)
        whenever(store.latitude).thenReturn(null)

        val result = sut.create(transaction)

        assertNull(result)
    }

    @Test
    fun `when longitude is null then no configuration`() {
        whenever(transaction.store).thenReturn(store)
        whenever(store.latitude).thenReturn(LATITUDE)
        whenever(store.longitude).thenReturn(null)

        val result = sut.create(transaction)

        assertNull(result)
    }

    @Test
    fun `when merchant is null then no configuration`() {
        whenever(transaction.store).thenReturn(store)
        whenever(store.latitude).thenReturn(LATITUDE)
        whenever(store.longitude).thenReturn(LONGITUDE)
        whenever(transaction.merchant).thenReturn(null)

        val result = sut.create(transaction)

        assertNull(result)
    }

    @Test
    fun `when mcc is null then no configuration`() {
        whenever(transaction.store).thenReturn(store)
        whenever(store.latitude).thenReturn(LATITUDE)
        whenever(store.longitude).thenReturn(LONGITUDE)
        whenever(transaction.merchant).thenReturn(merchant)
        whenever(merchant.mcc).thenReturn(null)

        val result = sut.create(transaction)

        assertNull(result)
    }

    @Test
    fun `when all data is set then configuration is returned`() {
        whenever(transaction.store).thenReturn(store)
        whenever(store.latitude).thenReturn(LATITUDE)
        whenever(store.longitude).thenReturn(LONGITUDE)
        whenever(transaction.merchant).thenReturn(merchant)
        whenever(merchant.mcc).thenReturn(mcc)
        whenever(mcc.icon).thenReturn(ICON)

        val result = sut.create(transaction)

        assertNotNull(result)
        assertEquals(result.latitude, LATITUDE)
        assertEquals(result.longitude, LONGITUDE)
        assertEquals(result.iconResource, mcc.iconResource)
    }
}

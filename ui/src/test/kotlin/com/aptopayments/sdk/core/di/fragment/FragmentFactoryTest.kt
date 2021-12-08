package com.aptopayments.sdk.core.di.fragment

import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FragmentFactoryTest {

    private lateinit var sut: FragmentFactory

    @BeforeEach
    fun setUp() {
        sut = FragmentFactoryImpl()
    }

    @Test
    fun `transaction list fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
        val tag = "TRANSACTION_LIST_TEST_TAG"

        // When
        val fragment = sut.transactionListFragment(TestDataProvider.provideCardId(), config, tag)

        // Then
        assert(fragment is TransactionListFragment)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

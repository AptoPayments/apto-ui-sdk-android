package com.aptopayments.sdk.core.di.fragment

import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.setpin.ConfirmCardPinFragment
import com.aptopayments.sdk.features.card.setpin.SetCardPinFragment
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragment
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FragmentFactoryTest {

    private lateinit var sut: FragmentFactory

    @Before
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

    @Test
    fun `set pin fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val tag = "SET_PIN_TEST_TAG"

        // When
        val fragment = sut.setPinFragment(tag)

        // Then
        assert(fragment is SetCardPinFragment)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }

    @Test
    fun `confirm pin fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val tag = "CONFIRM_PIN_TEST_TAG"
        val pin = "1234"

        // When
        val fragment = sut.confirmPinFragment(TestDataProvider.provideCardId(), pin, tag)

        // Then
        assert(fragment is ConfirmCardPinFragment)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

package com.aptopayments.sdk.core.di.fragment

import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.setpin.ConfirmPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.SetPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragmentThemeTwo
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import kotlin.test.assertEquals

class FragmentFactoryTest : AndroidTest() {

    private lateinit var sut: FragmentFactory

    @Before
    override fun setUp() {
        super.setUp()
        startKoin {}
        sut = FragmentFactoryImpl()
    }

    @Test
    fun `transaction list fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val cardId = "cardId"
        val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
        val tag = "TRANSACTION_LIST_TEST_TAG"

        // When
        val fragment = sut.transactionListFragment(cardId, config, tag)

        //Then
        assert(fragment is TransactionListFragmentThemeTwo)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }

    @Test
    fun `set pin fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val tag = "SET_PIN_TEST_TAG"

        // When
        val fragment = sut.setPinFragment(tag)

        //Then
        assert(fragment is SetPinFragmentThemeTwo)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }

    @Test
    fun `confirm pin fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val tag = "CONFIRM_PIN_TEST_TAG"
        val pin = "1234"

        // When
        val fragment = sut.confirmPinFragment(pin, tag)

        //Then
        assert(fragment is ConfirmPinFragmentThemeTwo)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

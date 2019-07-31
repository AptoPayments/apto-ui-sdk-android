package com.aptopayments.sdk.core.di.fragment

import com.aptopayments.sdk.AndroidTest
import com.aptopayments.core.data.config.UITheme.THEME_2
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.setpin.ConfirmPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.SetPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragmentThemeTwo
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FragmentFactoryTest : AndroidTest() {

    private lateinit var sut: FragmentFactory

    @Before
    override fun setUp() {
        super.setUp()
        sut = FragmentFactoryImpl()
    }

    @Test
    fun `transaction list fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val cardId = "cardId"
        val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
        val tag = "TRANSACTION_LIST_TEST_TAG"

        // When
        val fragment = sut.transactionListFragment(THEME_2, cardId, config, tag)

        //Then
        assert(fragment is TransactionListFragmentThemeTwo)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }

    @Test
    fun `set pin fragment for theme2 return expected fragment and set TAG`() {
        // Given
        val tag = "SET_PIN_TEST_TAG"

        // When
        val fragment = sut.setPinFragment(uiTheme = THEME_2, tag = tag)

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
        val fragment = sut.confirmPinFragment(uiTheme = THEME_2, pin = pin, tag = tag)

        //Then
        assert(fragment is ConfirmPinFragmentThemeTwo)
        assertEquals(tag, (fragment as BaseFragment).TAG)
    }
}

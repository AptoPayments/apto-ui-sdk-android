package com.aptopayments.sdk.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals

private const val DEAFAULT_SATISFY_HARDWARE_REQUISITES = false
private const val DEFAULT_SHOW_ADD_CARD_BUTTON = false

internal class IAPHelperMockTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    lateinit var sut: IAPHelperMock

    @Before
    fun setUp() {
        sut = IAPHelperMock()
    }

    @Test
    fun `satisfy hardware requirements is always default`() {
        assertEquals(sut.satisfyHardwareRequisites(), DEAFAULT_SATISFY_HARDWARE_REQUISITES)
    }

    @Test
    fun `showAddCardButton is false by default`() {
        assertEquals(sut.showAddCardButton.getOrAwaitValue(), DEFAULT_SHOW_ADD_CARD_BUTTON)
    }
}

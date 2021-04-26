package com.aptopayments.sdk.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        assertEquals(sut.satisfyHardwareRequisites(), false)
    }

    @Test
    fun `showAddCardButton is false by default`() {
        val state = sut.state.getOrAwaitValue()
        assertTrue(state is ProvisioningState.CanNotBeAdded)
    }
}

package com.aptopayments.sdk.repository

import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(InstantExecutorExtension::class)
internal class IAPHelperMockTest {

    lateinit var sut: IAPHelperMock

    @BeforeEach
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

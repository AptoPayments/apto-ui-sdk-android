package com.aptopayments.sdk.repository

import com.aptopayments.sdk.InstantExecutorExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(InstantExecutorExtension::class)
internal class IAPHelperFakeTest {

    lateinit var sut: IAPHelperFake

    @BeforeEach
    fun setUp() {
        sut = IAPHelperFake()
    }

    @Test
    fun `satisfy hardware requirements is always default`() {
        assertEquals(sut.satisfyHardwareRequisites(), false)
    }

    @Test
    fun `showAddCardButton is false by default`() {
        val state = sut.state.value
        assertTrue(state is ProvisioningState.CanNotBeAdded)
    }
}

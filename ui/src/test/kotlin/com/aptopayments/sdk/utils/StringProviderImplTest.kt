package com.aptopayments.sdk.utils

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val TEST_STRING = "TEST"
private const val TEST_STRING_RESOURCE = 1234

class StringProviderImplTest {

    private val appContext: Context = mock()
    private val context: Context = mock()

    private lateinit var sut: StringProviderImpl

    @BeforeEach
    fun setUp() {
        whenever(context.applicationContext).thenReturn(appContext)
        sut = StringProviderImpl(context)
    }

    @Test
    fun `when created then appContext is obtained`() {
        verify(context).applicationContext
    }

    @Test
    fun `when ask for a String then it's obtained from context`() {
        whenever(appContext.getString(TEST_STRING_RESOURCE)).thenReturn(TEST_STRING)

        val testedString = sut.provide(TEST_STRING_RESOURCE)

        assertEquals(testedString, TEST_STRING)
    }
}

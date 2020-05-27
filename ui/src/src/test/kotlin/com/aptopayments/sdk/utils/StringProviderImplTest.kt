package com.aptopayments.sdk.utils

import android.content.Context
import com.aptopayments.sdk.UnitTest
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

private const val TEST_STRING = "TEST"
private const val TEST_STRING_RESOURCE = 1234

class StringProviderImplTest : UnitTest() {

    @Mock
    private lateinit var appContext: Context

    @Mock
    private lateinit var context: Context

    private lateinit var sut: StringProviderImpl

    @Before
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

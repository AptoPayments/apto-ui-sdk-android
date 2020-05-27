package com.aptopayments.sdk.repository

import com.aptopayments.core.data.card.IssueCardAdditionalFields
import com.aptopayments.sdk.UnitTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mock

class IssueCardAdditionalFieldsRepositoryImplTest : UnitTest() {

    @Mock
    private lateinit var additionalFields: IssueCardAdditionalFields

    private val sut = IssueCardAdditionalFieldsRepositoryImpl

    @Test
    fun `when initialized then no additional fields get`() {
        assertNull(sut.fields)
    }

    @Test
    fun `when additional fields set then get returns them`() {
        sut.fields = additionalFields

        assertEquals(additionalFields, sut.fields)
    }

    @After
    fun tearDown() {
        IssueCardAdditionalFieldsRepositoryImpl.fields = null
    }
}

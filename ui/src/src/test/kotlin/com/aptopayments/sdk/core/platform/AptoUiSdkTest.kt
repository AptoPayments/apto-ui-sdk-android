package com.aptopayments.sdk.core.platform

import com.aptopayments.core.data.card.IssueCardAdditionalFields
import com.aptopayments.sdk.repository.IssueCardAdditionalFieldsRepositoryImpl
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.test.assertEquals

class AptoUiSdkTest {

    @Test
    fun `by default DarkTheme is set to false`() {
        assertFalse(AptoUiSdk.cardOptions.darkThemeEnabled())
    }

    @Test
    fun `when setAdditionalFields then they are correctly stored`() {
        val fields = IssueCardAdditionalFields(mapOf())

        AptoUiSdk.setCardIssueAdditional(fields)

        assertEquals(fields, IssueCardAdditionalFieldsRepositoryImpl.fields)
    }

    @After
    fun tearDown() {
        IssueCardAdditionalFieldsRepositoryImpl.fields = null
    }
}

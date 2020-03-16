package com.aptopayments.sdk.utils.deeplinks

import android.content.Context
import com.aptopayments.sdk.R
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import kotlin.test.assertEquals

private const val DEFAULT_SCHEME = "deeplinkscheme"
private const val PROVISIONING_PATH = "provisioning"
private const val PARAMETER_CARD = "cardId"

class InAppProvisioningDeepLinkGeneratorTest : UnitTest() {

    @Mock
    private lateinit var context: Context

    private lateinit var sut: InAppProvisioningDeepLinkGenerator

    @Before
    fun setUp() {
        whenever(context.getString(R.string.apto_deep_link_scheme)).thenReturn(DEFAULT_SCHEME)
    }

    @Test
    fun `when created then correct parameters in URI`() {
        sut = InAppProvisioningDeepLinkGenerator(context, TestDataProvider.provideCardId() )

        val deepLink = sut()

        assertEquals(deepLink, "$DEFAULT_SCHEME://$PROVISIONING_PATH?$PARAMETER_CARD=${TestDataProvider.provideCardId()}")
    }
}

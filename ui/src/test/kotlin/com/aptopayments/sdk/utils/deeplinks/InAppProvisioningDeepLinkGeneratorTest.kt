package com.aptopayments.sdk.utils.deeplinks

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.StringProvider
import com.nhaarman.mockitokotlin2.any
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
    private lateinit var stringProvider: StringProvider

    private lateinit var sut: InAppProvisioningDeepLinkGenerator

    @Before
    fun setUp() {
        whenever(stringProvider.provide(any())).thenReturn(DEFAULT_SCHEME)
    }

    @Test
    fun `when created then correct parameters in URI`() {
        sut = InAppProvisioningDeepLinkGenerator(stringProvider)
        sut.setCardId(TestDataProvider.provideCardId())

        val deepLink = sut()

        assertEquals(
            deepLink,
            "$DEFAULT_SCHEME://$PROVISIONING_PATH?$PARAMETER_CARD=${TestDataProvider.provideCardId()}"
        )
    }
}

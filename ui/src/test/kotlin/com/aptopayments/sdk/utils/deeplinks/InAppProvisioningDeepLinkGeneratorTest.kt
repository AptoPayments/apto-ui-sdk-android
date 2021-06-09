package com.aptopayments.sdk.utils.deeplinks

import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.StringProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val DEFAULT_SCHEME = "deeplinkscheme"
private const val PROVISIONING_PATH = "provisioning"
private const val PARAMETER_CARD = "cardId"

class InAppProvisioningDeepLinkGeneratorTest {

    private val stringProvider: StringProvider = mock()

    private lateinit var sut: InAppProvisioningDeepLinkGenerator

    @BeforeEach
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

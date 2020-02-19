package com.aptopayments.sdk.core.platform

import org.junit.Assert.*
import org.junit.Test

class AptoUiSdkTest {

    @Test
    fun `by default DarkTheme is set to false`() {
        assertFalse(AptoUiSdk.cardOptions.darkThemeEnabled())
    }
}

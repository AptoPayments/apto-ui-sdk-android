package com.aptopayments.sdk.core.data.config

import com.aptopayments.core.data.config.UITheme
import com.aptopayments.sdk.UnitTest
import org.amshove.kluent.shouldBe
import org.junit.Test

class UIThemeTest : UnitTest() {

    @Test fun `Theme one correctly parses`() {
        // Given
        val theme = "theme_1"

        // When
        val uiTheme = UITheme.parseUITheme(theme)

        // Then
        uiTheme shouldBe UITheme.THEME_1
    }

    @Test fun `Theme two correctly parses`() {
        // Given
        val theme = "theme_2"

        // When
        val uiTheme = UITheme.parseUITheme(theme)

        // Then
        uiTheme shouldBe UITheme.THEME_2
    }

    @Test fun `Incorrect theme defaults to theme one`() {
        // Given
        val theme = "incorrect"

        // When
        val uiTheme = UITheme.parseUITheme(theme)

        // Then
        uiTheme shouldBe UITheme.THEME_1
    }

}

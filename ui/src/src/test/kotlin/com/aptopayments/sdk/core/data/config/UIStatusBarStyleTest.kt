package com.aptopayments.sdk.core.data.config

import com.aptopayments.core.data.config.UIStatusBarStyle
import com.aptopayments.sdk.UnitTest
import org.amshove.kluent.shouldBe
import org.junit.Test

class UIStatusBarStyleTest : UnitTest() {

    @Test fun `Light style correctly parses`() {
        // Given
        val style = "light"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        uiStatusBarStyle shouldBe UIStatusBarStyle.LIGHT
    }

    @Test fun `Dark style correctly parses`() {
        // Given
        val style = "dark"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        uiStatusBarStyle shouldBe UIStatusBarStyle.DARK
    }

    @Test fun `Auto style correctly parses`() {
        // Given
        val style = "auto"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        uiStatusBarStyle shouldBe UIStatusBarStyle.AUTO
    }

    @Test fun `Incorrect style defaults to auto`() {
        // Given
        val style = "incorrect"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        uiStatusBarStyle shouldBe UIStatusBarStyle.AUTO
    }

}

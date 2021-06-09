package com.aptopayments.sdk.core.data.config

import com.aptopayments.mobile.data.config.UIStatusBarStyle
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class UIStatusBarStyleTest {

    @Test
    fun `Light style correctly parses`() {
        // Given
        val style = "light"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        assertSame(UIStatusBarStyle.LIGHT, uiStatusBarStyle)
    }

    @Test
    fun `Dark style correctly parses`() {
        // Given
        val style = "dark"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        assertSame(UIStatusBarStyle.DARK, uiStatusBarStyle)
    }

    @Test
    fun `Auto style correctly parses`() {
        // Given
        val style = "auto"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        assertSame(UIStatusBarStyle.AUTO, uiStatusBarStyle)
    }

    @Test
    fun `Incorrect style defaults to auto`() {
        // Given
        val style = "incorrect"

        // When
        val uiStatusBarStyle = UIStatusBarStyle.parseStatusBarStyle(style)

        // Then
        assertSame(UIStatusBarStyle.AUTO, uiStatusBarStyle)
    }
}

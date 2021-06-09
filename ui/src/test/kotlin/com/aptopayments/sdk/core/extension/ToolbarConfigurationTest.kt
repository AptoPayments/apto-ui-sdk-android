package com.aptopayments.sdk.core.extension

import android.graphics.Color
import com.aptopayments.mobile.repository.config.remote.entities.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

private const val TITLE = "Title"

internal class ToolbarConfigurationTest {

    private val builder = ToolbarConfiguration.Builder()

    @Test
    fun `when nothing set then default parameters are correct`() {
        val sut = builder.build()

        assertTrue(sut.backButtonMode is BackButtonMode.Back)
        assertEquals("", sut.title)
        assertNull(sut.backgroundColor)
        assertNull(sut.titleTextColor)
    }

    @Test
    fun `when backButtonMode set then correctly built`() {
        builder.backButtonMode(BackButtonMode.Close())

        val sut = builder.build()

        assertTrue(sut.backButtonMode is BackButtonMode.Close)
    }

    @Test
    fun `when title set then correctly built`() {
        builder.title(TITLE)

        val sut = builder.build()

        assertEquals(TITLE, sut.title)
    }

    @Test
    fun `when titleTextColor set then correctly built`() {
        builder.titleTextColor(Color.WHITE)

        val sut = builder.build()

        assertEquals(Color.WHITE, sut.titleTextColor)
    }

    @Test
    fun `when backgroundColor set then correctly built`() {
        builder.backgroundColor(Color.WHITE)

        val sut = builder.build()

        assertEquals(Color.WHITE, sut.backgroundColor)
    }

    @Test
    fun `when setPrimaryColors set then correct colors set`() {
        builder.setPrimaryColors()
        val backgroundColor = getColor(DEFAULT_UI_NAVIGATION_PRIMARY_COLOR)
        val titleTextColor = getColor(DEFAULT_TEXT_TOP_BAR_PRIMARY_COLOR)

        val sut = builder.build()

        assertEquals(titleTextColor, sut.titleTextColor)
        assertEquals(backgroundColor, sut.backgroundColor)
    }

    @Test
    fun `when setSecondaryColors set then correct colors set`() {
        builder.setSecondaryColors()
        val backgroundColor = getColor(DEFAULT_UI_NAVIGATION_SECONDARY_COLOR)
        val titleTextColor = getColor(DEFAULT_TEXT_TOP_BAR_SECONDARY_COLOR)

        val sut = builder.build()

        assertEquals(titleTextColor, sut.titleTextColor)
        assertEquals(backgroundColor, sut.backgroundColor)
    }

    @Test
    fun `when setSecondaryTertiaryColors set then correct colors set`() {
        builder.setSecondaryTertiaryColors()
        val backgroundColor = getColor(DEFAULT_UI_NAVIGATION_SECONDARY_COLOR)
        val titleTextColor = getColor(DEFAULT_ICON_TERTIARY_COLOR)

        val sut = builder.build()

        assertEquals(titleTextColor, sut.titleTextColor)
        assertEquals(backgroundColor, sut.backgroundColor)
    }

    private fun getColor(color: String) = Color.parseColor("#$color")
}

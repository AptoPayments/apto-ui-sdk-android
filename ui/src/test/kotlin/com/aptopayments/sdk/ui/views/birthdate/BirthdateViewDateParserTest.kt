package com.aptopayments.sdk.ui.views.birthdate

import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val YEAR = "2020"
private const val MONTH = "04"
private const val DAY = "01"

internal class BirthdateViewDateParserTest {

    private val sut = BirthdateViewDateParser()

    @Test
    fun `given correct date then LocalDate returned`() {
        val date = LocalDate.of(YEAR.toInt(), MONTH.toInt(), DAY.toInt())

        val testedDate = sut.parse(YEAR, MONTH, DAY)

        assertEquals(date, testedDate)
    }

    @Test
    fun `given out of range day high month then null returned`() {
        val testedDate = sut.parse(YEAR, "13", DAY)

        assertNull(testedDate)
    }

    @Test
    fun `given out of range day low month then null returned`() {
        val testedDate = sut.parse(YEAR, "0", DAY)

        assertNull(testedDate)
    }

    @Test
    fun `given out of range day high then null returned`() {
        val testedDate = sut.parse(YEAR, MONTH, "55")

        assertNull(testedDate)
    }

    @Test
    fun `given out of range day low day then null returned`() {
        val testedDate = sut.parse(YEAR, MONTH, "0")

        assertNull(testedDate)
    }

    @Test
    fun `given empty day then null returned`() {
        val testedDate = sut.parse(YEAR, MONTH, "")

        assertNull(testedDate)
    }

    @Test
    fun `given empty Month then null returned`() {
        val testedDate = sut.parse(YEAR, "", DAY)

        assertNull(testedDate)
    }

    @Test
    fun `given empty Year then null returned`() {
        val testedDate = sut.parse("", MONTH, DAY)

        assertNull(testedDate)
    }
}

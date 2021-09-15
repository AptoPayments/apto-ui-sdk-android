package com.aptopayments.sdk.utils.extensions

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import java.util.*

private val TUESDAY = LocalDate.of(2021, 6, 1)
private val WEDNESDAY = LocalDate.of(2021, 6, 2)
private val THURSDAY = LocalDate.of(2021, 6, 3)
private val FRIDAY = LocalDate.of(2021, 6, 4)
private val SATURDAY = LocalDate.of(2021, 6, 5)
private val SUNDAY = LocalDate.of(2021, 6, 6)
private val NEXT_MONDAY = LocalDate.of(2021, 6, 7)
private val NEXT_TUESDAY = LocalDate.of(2021, 6, 8)

internal class LocalDateKtTest {

    @Nested
    inner class NonWeekendDaysUntil {
        @Test
        fun `when dates are equal then 0 returned`() {
            val first = TUESDAY
            val second = first

            assertEquals(0, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and Wednesday then 1 returned`() {
            val first = TUESDAY
            val second = WEDNESDAY

            assertEquals(1, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and Thursday then 2 returned`() {
            val first = TUESDAY
            val second = THURSDAY

            assertEquals(2, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and Friday then 3 returned`() {
            val first = TUESDAY
            val second = FRIDAY

            assertEquals(3, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and Saturday then 3 returned`() {
            val first = TUESDAY
            val second = SATURDAY

            assertEquals(3, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and Sunday then 3 returned`() {
            val first = TUESDAY
            val second = SUNDAY

            assertEquals(3, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and NextMonday then 4 returned`() {
            val first = TUESDAY
            val second = NEXT_MONDAY

            assertEquals(4, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and NextTuesday then 5 returned`() {
            val first = TUESDAY
            val second = NEXT_TUESDAY

            assertEquals(5, first.nonWeekendDaysUntil(second))
        }

        @Test
        fun `when Tuesday and NextTuesday INVERSED then 5 returned`() {
            val first = NEXT_TUESDAY
            val second = TUESDAY

            assertEquals(5, first.nonWeekendDaysUntil(second))
        }
    }

    @Nested
    inner class PlusWorkingDays {
        @Test
        fun `given any day when plusWorkingDays(0) then same day returned`() {
            val first = TUESDAY

            assertEquals(first, first.plusWorkingDays(0))
        }

        @Test
        fun `given a tuesday when plusWorkingDays(1) then wednesday returned`() {
            val result = TUESDAY.plusWorkingDays(1)

            assertEquals(WEDNESDAY, result)
        }

        @Test
        fun `given a tuesday when plusWorkingDays(2) then thursday returned`() {
            val result = TUESDAY.plusWorkingDays(2)

            assertEquals(THURSDAY, result)
        }

        @Test
        fun `given a friday when plusWorkingDays(1) then monday returned`() {
            val result = FRIDAY.plusWorkingDays(1)

            assertEquals(NEXT_MONDAY, result)
        }

        @Test
        fun `given a friday when plusWorkingDays(2) then tuesday returned`() {
            val result = FRIDAY.plusWorkingDays(2)

            assertEquals(NEXT_TUESDAY, result)
        }
    }
}

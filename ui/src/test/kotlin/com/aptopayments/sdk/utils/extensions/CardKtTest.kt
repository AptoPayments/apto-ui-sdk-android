package com.aptopayments.sdk.utils.extensions

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.sdk.core.data.TestDataProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

private const val MAX_DAYS_AFTER_ISSUED = 15
private val ISSUED_DAY = LocalDate.of(2021, 6, 16)
private val MAX_DAY = ISSUED_DAY.plusWorkingDays(MAX_DAYS_AFTER_ISSUED)

internal class CardKtTest {

    @Nested
    inner class GivenACardWithOrderedStatus {

        @Test
        internal fun `given card was issued same day then should show`() {
            val card = givenACard(Card.OrderedStatus.ORDERED)

            val result = card.shouldShowFakeShippingStatus(ISSUED_DAY)

            assertTrue(result)
        }

        @Test
        internal fun `given a card one day old then should show`() {
            val card = givenACard(Card.OrderedStatus.ORDERED)

            val result = card.shouldShowFakeShippingStatus(ISSUED_DAY.plusDays(1))

            assertTrue(result)
        }

        @Test
        internal fun `given card was issued MAX_DAYS then should not show`() {
            val card = givenACard(Card.OrderedStatus.ORDERED)

            val result = card.shouldShowFakeShippingStatus(MAX_DAY)

            assertTrue(result)
        }

        @Test
        internal fun `given card was issued MAX_DAYS plus one then should not show`() {
            val card = givenACard(Card.OrderedStatus.ORDERED)

            val result = card.shouldShowFakeShippingStatus(MAX_DAY.plusDays(1))

            assertFalse(result)
        }

        @Test
        internal fun `given card with null issued_date then should not show`() {
            val card = TestDataProvider.provideCard(orderedStatus = Card.OrderedStatus.ORDERED)

            val result = card.shouldShowFakeShippingStatus(MAX_DAY.plusDays(1))

            assertFalse(result)
        }
    }

    private fun givenACard(status: Card.OrderedStatus) = TestDataProvider.provideCard(
        orderedStatus = status,
        issuedAt = ZonedDateTime.of(ISSUED_DAY, LocalTime.now(), ZoneId.systemDefault())
    )

    @Nested
    inner class GivenACardWithRECEIVEDStatus {

        @Test
        internal fun `given card was issued today then should show`() {
            val card = givenACard(Card.OrderedStatus.RECEIVED)

            val result = card.shouldShowFakeShippingStatus(ISSUED_DAY)

            assertFalse(result)
        }

        @Test
        internal fun `given card was issued MAX_DAYS plus one then should not show`() {
            val card = givenACard(Card.OrderedStatus.RECEIVED)

            val result = card.shouldShowFakeShippingStatus(MAX_DAY)

            assertFalse(result)
        }
    }
}

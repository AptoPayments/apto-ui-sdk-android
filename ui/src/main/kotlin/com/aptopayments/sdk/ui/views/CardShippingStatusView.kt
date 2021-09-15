package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.extension.replaceFirstValue
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.utils.extensions.formatMonthDay
import com.aptopayments.sdk.utils.extensions.nonWeekendDaysUntilToday
import com.aptopayments.sdk.utils.extensions.plusWorkingDays
import kotlinx.android.synthetic.main.view_section_option_subtitle.view.*
import org.threeten.bp.LocalDate

private const val MIN_DAYS_TO_CARD_ARRIVAL = 7
private const val MAX_DAYS_TO_CARD_ARRIVAL = 10

private const val STEP_1 = 1
private const val STEP_2 = 2
private const val STEP_3 = 3
private const val STEP_4 = 4
private const val STEP_5 = 5

internal class CardShippingStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SectionOptionWithSubtitleView(context, attrs, defStyleAttr) {

    var card: Card? = null
        set(value) {
            field = value
            onCardUpdated()
        }

    init {
        option_icon.hide()
    }

    private fun onCardUpdated() {
        card?.issuedAt?.let { issued ->
            val currentStep = getStep(issued.toLocalDate().nonWeekendDaysUntilToday())
            this.optionTitle = getTitleFor(currentStep)
            this.optionDescription = getSubtTitleFor(currentStep, issued.toLocalDate())
        }
    }

    private fun getStep(weekdays: Int) = when (weekdays) {
        in 0..1 -> STEP_1
        in 2..3 -> STEP_2
        in 4..7 -> STEP_3
        in 8..13 -> STEP_4
        else -> STEP_5
    }

    private fun getTitleFor(step: Int): String {
        return when (step) {
            STEP_1 -> "card_settings_shipping_first_title"
            STEP_2 -> "card_settings_shipping_second_title"
            STEP_3 -> "card_settings_shipping_third_title"
            STEP_4 -> "card_settings_shipping_fourth_title"
            else -> "card_settings_shipping_fifth_title"
        }.localized()
    }

    private fun getSubtTitleFor(step: Int, issuedAt: LocalDate): String {
        return when (step) {
            STEP_1 -> setDates("card_settings_shipping_first_description", issuedAt)
            STEP_2 -> setDates("card_settings_shipping_second_description", issuedAt)
            STEP_3 -> setDates("card_settings_shipping_third_description", issuedAt)
            STEP_4 -> setDates("card_settings_shipping_fourth_description", issuedAt)
            else -> setDates("card_settings_shipping_fifth_description", issuedAt)
        }
    }

    private fun setDates(copy: String, issuedAt: LocalDate): String {
        val firstDate = issuedAt.plusWorkingDays(MIN_DAYS_TO_CARD_ARRIVAL).formatMonthDay()
        val secondDate = issuedAt.plusWorkingDays(MAX_DAYS_TO_CARD_ARRIVAL).formatMonthDay()
        return copy.localized().replaceFirstValue(firstDate).replaceFirstValue(secondDate)
    }
}

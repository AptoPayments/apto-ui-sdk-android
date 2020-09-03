package com.aptopayments.sdk.features.loadfunds.paymentsources

import androidx.annotation.IntegerRes
import com.aptopayments.mobile.data.paymentsources.BankAccount
import com.aptopayments.mobile.data.paymentsources.Card
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork

internal data class PaymentSourceElement(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val showFourDots: Boolean = false,
    @IntegerRes val logo: Int
) {
    fun isDefined() = id.isNotEmpty()
}

internal class PaymentSourceElementMapper() {
    fun map(elem: PaymentSource): PaymentSourceElement {
        return when (elem) {
            is Card -> mapCard(elem)
            is BankAccount -> throw RuntimeException() // TODO when UI defined
            else -> throw RuntimeException()
        }
    }

    fun getUnsetElement(): PaymentSourceElement {
        return PaymentSourceElement(
            title = "load_funds_add_money_no_payment_method".localized(),
            showFourDots = false,
            logo = CardNetwork.UNKNOWN.icon
        )
    }

    private fun mapCard(elem: Card): PaymentSourceElement {
        val subtitle = "load_funds.payment_methods.existing_card_element.subtitle".localized()
            .replace("<<NAME>>", elem.network.toString().toLowerCase().capitalize())
        val networkLogo = CardNetwork.fromString(elem.network.name).icon
        return PaymentSourceElement(
            id = elem.id,
            title = elem.lastFour,
            subtitle = subtitle,
            showFourDots = true,
            logo = networkLogo
        )
    }
}

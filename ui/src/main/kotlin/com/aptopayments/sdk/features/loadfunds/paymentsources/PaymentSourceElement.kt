package com.aptopayments.sdk.features.loadfunds.paymentsources

import androidx.annotation.IntegerRes
import com.aptopayments.mobile.data.paymentsources.Card
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.utils.extensions.toCapitalized

internal data class PaymentSourceElement(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val showFourDots: Boolean = false,
    @IntegerRes val logo: Int,
    val isPreferred: Boolean = false
) {

    companion object {
        fun unsetElement(): PaymentSourceElement {
            return PaymentSourceElement(
                title = "load_funds_add_money_no_payment_method".localized(),
                showFourDots = false,
                logo = CardNetwork.UNKNOWN.icon
            )
        }

        fun genericElement(title: String): PaymentSourceElement {
            return PaymentSourceElement(
                title = title,
                showFourDots = false,
                logo = CardNetwork.UNKNOWN.icon
            )
        }
    }
}

internal class PaymentSourceElementMapper {

    fun map(elem: PaymentSource): PaymentSourceElement {
        return when (elem) {
            is Card -> mapCard(elem)
            else -> throw RuntimeException()
        }
    }

    private fun mapCard(elem: Card): PaymentSourceElement {
        val subtitle = "load_funds.payment_methods.existing_card_element.subtitle".localized()
            .replace("<<NAME>>", elem.network.toString().toCapitalized())
        val networkLogo = CardNetwork.fromString(elem.network.name).icon
        return PaymentSourceElement(
            id = elem.id,
            title = elem.lastFour,
            subtitle = subtitle,
            showFourDots = true,
            logo = networkLogo,
            isPreferred = elem.isPreferred
        )
    }
}

package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.ViewPaymentSourceElementFullBinding
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement

internal class PaymentSourceFullView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding = ViewPaymentSourceElementFullBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        customizeUi()
    }

    private fun customizeUi() {
        with(themeManager()) {
            customizeCardTitle(binding.paymentMethodTitle)
            customizeCardSubtitle(binding.paymentMethodDescription)
        }
    }

    fun setElement(paymentSourceElement: PaymentSourceElement) {
        binding.element = paymentSourceElement
        binding.executePendingBindings()
    }
}

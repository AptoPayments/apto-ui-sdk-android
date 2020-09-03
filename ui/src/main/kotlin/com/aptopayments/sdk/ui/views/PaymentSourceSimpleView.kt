package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.ViewPaymentSourceElementSimpleBinding
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement

internal class PaymentSourceSimpleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding = ViewPaymentSourceElementSimpleBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        customizeUi()
    }

    private fun customizeUi() {
        themeManager().customizeCardTitle(binding.paymentMethodTitle)
    }

    fun setElement(element: PaymentSourceElement?) {
        binding.element = element
        binding.executePendingBindings()
    }
}

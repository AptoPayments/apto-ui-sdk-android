package com.aptopayments.sdk.features.card.statements

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract

internal class PdfRendererFragmentDouble(override var delegate: PdfRendererContract.Delegate?) : BaseFragment(),
    PdfRendererContract.View {
    override fun layoutId() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}

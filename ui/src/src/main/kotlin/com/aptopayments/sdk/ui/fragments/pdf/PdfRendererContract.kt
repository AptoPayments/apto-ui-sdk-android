package com.aptopayments.sdk.ui.fragments.pdf

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface PdfRendererContract {

    interface Delegate : FragmentDelegate {
        fun onPdfBackPressed()
    }

    interface View {
        var delegate: Delegate?
    }
}

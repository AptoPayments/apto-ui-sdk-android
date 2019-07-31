package com.aptopayments.sdk.features.issuecard

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface IssueCardErrorContract {
    interface View {
        var delegate: Delegate?
    }

    interface Delegate : FragmentDelegate {
        fun onRetryIssueCard()
    }
}

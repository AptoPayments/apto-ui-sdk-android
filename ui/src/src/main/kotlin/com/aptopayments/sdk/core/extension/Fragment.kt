package com.aptopayments.sdk.core.extension

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.aptopayments.sdk.core.platform.BaseFragment

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
        beginTransaction().func().commit()

internal fun BaseFragment.close() = fragmentManager?.popBackStack()

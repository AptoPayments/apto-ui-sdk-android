package com.aptopayments.sdk.core.platform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

internal abstract class BaseViewBindingFragment<T : ViewBinding> : BaseFragment() {

    protected var _binding: T? = null

    protected val binding: T
        get() = requireNotNull(_binding)

    final override fun layoutId(): Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = getViewBinding(inflater, container)
        return requireNotNull(_binding).root
    }

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

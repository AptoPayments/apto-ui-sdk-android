package com.aptopayments.sdk.core.platform.flow

import androidx.fragment.app.FragmentManager
import com.aptopayments.sdk.core.platform.BaseFragment

internal class FragmentWrapper(
    fragmentManager: FragmentManager?,
    fragment: BaseFragment,
    val tag: String
) : FlowPresentable {
    var fragmentManager: FragmentManager? = fragmentManager
        set(newValue) {
            field = newValue
            if (newValue == null) fragment = null
        }
    private var fragment: BaseFragment? = fragment
    private val containedFragment: BaseFragment?
        get() = fragment ?: fragmentManager?.findFragmentByTag(tag) as? BaseFragment

    override fun startFragment(): BaseFragment? {
        return containedFragment
    }

    override fun lastFragment(): BaseFragment? {
        return containedFragment
    }

    override fun clearChildElements() {
        containedFragment?.clearChildElements()
    }

    override fun removeFromStack(animated: Boolean) {
        containedFragment?.removeFromStack(animated)
    }

    override fun onPresented() {
        containedFragment?.onPresented()
    }
}

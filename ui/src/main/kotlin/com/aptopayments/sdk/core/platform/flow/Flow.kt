package com.aptopayments.sdk.core.platform.flow

import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_CLOSE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.extension.inTransaction
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.ViewUtils
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import com.aptopayments.sdk.utils.extensions.showCustomSnackbar
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.ref.WeakReference
import java.lang.reflect.Modifier

internal interface FlowPresentable {
    fun startFragment(): BaseFragment?
    fun lastFragment(): BaseFragment?
    fun clearChildElements()
    fun removeFromStack(animated: Boolean)
    fun onPresented()
}

internal abstract class Flow : FlowPresentable, KoinComponent {

    val fragmentFactory: FragmentFactory by inject()

    private var fragmentContainer: Int = 0
    private var activity: WeakReference<AppCompatActivity?> = WeakReference(null)
    private val fragmentManager: FragmentManager?
        get() = rootActivity()?.supportFragmentManager
    private var parentFlow: WeakReference<Flow?> = WeakReference(null)
    private var childItems = mutableListOf<FlowPresentable>()

    abstract fun init(onInitComplete: (Either<Failure, Unit>) -> Unit)
    abstract fun restoreState()

    override fun clearChildElements() {
        childItems.forEach { it.clearChildElements() }
        childItems.clear()
    }

    @VisibleForTesting(otherwise = Modifier.PROTECTED)
    fun setStartElement(element: FlowPresentable) {
        if (element is Flow) element.parentFlow = WeakReference(this)
        addChild(element)
    }

    fun onRestoreInstanceState() {
        restoreState()
        childItems.forEach { if (it is Flow) it.onRestoreInstanceState() }
    }

    override fun startFragment(): BaseFragment? = this.childItems.firstOrNull()?.startFragment()

    override fun lastFragment(): BaseFragment? = this.childItems.lastOrNull()?.lastFragment()

    protected fun fragmentWithTag(tag: String): BaseFragment? {
        childItems.forEach {
            if (it is FragmentWrapper && it.tag == tag) return it.startFragment()
        }
        return null
    }

    protected fun fragmentDialogWithTag(tag: String): BaseDialogFragment? {
        return fragmentManager?.findFragmentByTag(tag) as? BaseDialogFragment
    }

    open fun attachTo(activity: AppCompatActivity, fragmentContainer: Int) {
        this.activity = WeakReference(activity)
        this.fragmentContainer = fragmentContainer
        updateChildFragmentManager(fragmentManager)
    }

    open fun detachFromActivity() {
        childItems.forEach {
            when (it) {
                is Flow -> it.detachFromActivity()
                is FragmentWrapper -> it.fragmentManager = null
            }
        }
        this.activity.clear()
        this.fragmentContainer = 0
    }

    open fun start(animated: Boolean = true) {
        startFragment()?.let { startFragment ->
            push(fragment = startFragment, animated = animated, addToChildItems = false)
        }
    }

    override fun removeFromStack(animated: Boolean) {
        childItems.forEach { it.removeFromStack(animated) }
        childItems.filterIsInstance<FragmentWrapper>().forEach { childItem ->
            childItem.lastFragment()?.let {
                fragmentManager?.inTransaction {
                    if (animated) setTransition(TRANSIT_FRAGMENT_CLOSE)
                    remove(it)
                }
            }
        }
    }

    override fun onPresented() {
        // do nothing
    }

    protected fun push(fragment: BaseFragment, animated: Boolean = true, addToChildItems: Boolean = true) {
        if (addToChildItems) addChild(fragment)
        fragmentManager?.beginTransaction()?.apply {
            if (animated) {
                setTransition(TRANSIT_FRAGMENT_OPEN)
            } else {
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            }
            add(fragmentContainer(), fragment, fragment.TAG).commit()
            runOnCommit { fragment.onPresented() }
        }
    }

    protected fun push(baseDialogFragment: BaseDialogFragment) {
        fragmentManager?.let { baseDialogFragment.show(it, baseDialogFragment.TAG) }
    }

    protected fun push(flow: Flow, animated: Boolean = true) {
        addChild(flow)
        flow.parentFlow = WeakReference(this)
        flow.start(animated)
    }

    private fun addChild(element: FlowPresentable) {
        var child = element
        if (element is BaseFragment) {
            val tag = element.TAG
            child = FragmentWrapper(fragmentManager, element, tag)
        }
        childItems.add(child)
    }

    protected fun popFragment() {
        childItems = childItems.dropLast(1).toMutableList()
        childItems.lastOrNull()?.onPresented()
        fragmentManager?.fragments?.lastOrNull()?.let { lastFragment ->
            fragmentManager?.beginTransaction()?.apply {
                setTransition(TRANSIT_FRAGMENT_CLOSE).remove(lastFragment).commit()
            }
        }
    }

    protected fun popDialogFragmentWithTag(tag: String) {
        childItems.lastOrNull()?.onPresented()
        fragmentDialogWithTag(tag)?.dismiss()
    }

    protected fun popFlow(animated: Boolean) {
        (childItems.lastOrNull() as? Flow)?.let {
            it.removeFromStack(animated)
            childItems = childItems.dropLast(1).toMutableList()
            childItems.lastOrNull()?.onPresented()
        }
    }

    protected fun popAllFragments(onComplete: (() -> Unit)?) {
        fragmentManager?.beginTransaction()?.apply {
            fragmentManager?.fragments?.forEach { remove(it) }
            commit()
            runOnCommit { onComplete?.invoke() }
        }
    }

    protected fun showLoading() = (rootActivity() as? BaseActivity)?.showLoading()

    protected fun hideLoading() = (rootActivity() as? BaseActivity)?.hideLoading()

    protected fun handleFailure(failure: Failure?) {
        hideLoading()
        rootActivity()?.let {
            when (failure) {
                is Failure.ServerError -> notify(failure.errorMessage())
            }
        }
    }

    protected fun confirm(
        title: String,
        text: String,
        confirm: String,
        cancel: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        rootActivity()?.let {
            val alertDialogBuilder = ViewUtils.getAlertDialogBuilder(
                it,
                confirm, cancel, { onConfirm() }, { onCancel() }
            )
            themeManager().getAlertDialog(alertDialogBuilder, title, text).show()
        }
    }

    protected fun notify(
        title: String,
        message: String,
        messageType: SnackbarMessageType = SnackbarMessageType.ERROR
    ) {
        rootActivity()?.showCustomSnackbar(title = title, message = message, messageType = messageType)
    }

    protected fun notify(
        message: String,
        messageType: SnackbarMessageType = SnackbarMessageType.ERROR
    ) {
        rootActivity()?.showCustomSnackbar(message = message, messageType = messageType)
    }

    protected fun rootActivity(): AppCompatActivity? {
        return activity.get() ?: parentFlow.get()?.rootActivity()
    }

    private fun fragmentContainer(): Int {
        if (fragmentContainer != 0) return fragmentContainer
        parentFlow.get()?.let { return it.fragmentContainer() }
        return 0
    }

    private fun updateChildFragmentManager(manager: FragmentManager?) {
        childItems.forEach {
            when (it) {
                is Flow -> it.updateChildFragmentManager(manager)
                is FragmentWrapper -> it.fragmentManager = manager
            }
        }
    }
}

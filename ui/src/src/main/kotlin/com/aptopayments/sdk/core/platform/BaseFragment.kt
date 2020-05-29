package com.aptopayments.sdk.core.platform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.MessageBanner
import com.aptopayments.sdk.utils.MessageBanner.MessageType.ERROR
import com.aptopayments.sdk.utils.ViewUtils
import org.koin.core.KoinComponent
import org.koin.core.inject

private const val TAG_KEY = "APTO_TAG_KEY"

internal abstract class BaseFragment : Fragment(), FlowPresentable, KoinComponent {

    abstract fun layoutId(): Int
    abstract fun backgroundColor(): Int

    private val analytics: AnalyticsServiceContract by inject()
    val aptoPlatformProtocol: AptoPlatformProtocol by inject()
    lateinit var TAG: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpArguments()
        if (savedInstanceState != null) {
            TAG = savedInstanceState.getString(TAG_KEY)!!
        }
        setupViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setBackground()
        setupListeners()
        viewLoaded()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TAG_KEY, TAG)
    }

    abstract fun setupViewModel()

    abstract fun setupUI()

    open fun setUpArguments() {}

    open fun setupListeners() {}

    protected open fun viewLoaded() {}

    open fun onBackPressed() {}

    internal fun showLoading() = (activity as? BaseActivity)?.showLoading()

    internal fun hideLoading() = (activity as? BaseActivity)?.hideLoading()

    internal fun hideKeyboard() = (activity as? BaseActivity)?.hideKeyboard()

    internal fun showKeyboard() = (activity as? BaseActivity)?.showKeyboard()

    internal fun checkPermission(permission: String) = (activity as? BaseActivity)?.checkPermission(permission)

    internal fun requestPermission(permission: String, onResult: (Boolean) -> Unit) =
        (activity as? BaseActivity)?.requestPermission(permission, onResult)

    internal fun firstTimeCreated(savedInstanceState: Bundle?) = savedInstanceState == null

    internal fun notify(message: String, type: MessageBanner.MessageType = ERROR) =
        notify(title = null, message = message, type = type)

    internal fun notify(title: String?, message: String, type: MessageBanner.MessageType = ERROR) =
        activity?.let { MessageBanner().showBanner(it, title = title, message = message, messageType = type) }

    protected fun confirm(
        title: String,
        text: String,
        confirm: String,
        cancel: String,
        onConfirm: (Unit) -> Unit,
        onCancel: (Unit) -> Unit
    ) {
        activity?.let {
            val alertDialogBuilder = ViewUtils.getAlertDialogBuilder(it,
                confirm, cancel, { onConfirm(Unit) }, { onCancel(Unit) })
            themeManager().getAlertDialog(alertDialogBuilder, title, text).show()
        }
    }

    protected open fun handleFailure(failure: Failure?) {
        when (failure) {
            is Failure.ServerError -> {
                notify("failure_server_error".localized())
                analytics.track(Event.UnknownServerError, failure.toJSonObject())
            }
            is Failure.UserSessionExpired -> {
                aptoPlatformProtocol.logout()
                notify("session_expired_error".localized())
            }
        }
    }

    override fun startFragment() = this
    override fun lastFragment() = this
    override fun clearChildElements() {}
    override fun removeFromStack(animated: Boolean) {}
    override fun onPresented() {}

    fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun setBackground() {
        view?.setBackgroundColor(backgroundColor())
    }

    fun customizePrimaryNavigationStatusBar() {
        activity?.window?.let { themeManager().customizeStatusBar(it) }
    }

    fun customizeSecondaryNavigationStatusBar() {
        activity?.window?.let { themeManager().customizeSecondaryNavigationStatusBar(it) }
    }
}

package com.aptopayments.sdk.core.platform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.BuildConfig
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.MessageBanner
import com.aptopayments.sdk.utils.MessageBanner.MessageType.ERROR
import com.aptopayments.sdk.utils.ViewUtils
import org.koin.android.ext.android.inject

private const val TAG_KEY = "APTO_TAG_KEY"

internal abstract class BaseFragment : Fragment(), FlowPresentable {

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
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        activity?.let {
            val alertDialogBuilder = ViewUtils.getAlertDialogBuilder(
                it,
                confirm, cancel, { onConfirm.invoke() }, { onCancel.invoke() }
            )
            themeManager().getAlertDialog(alertDialogBuilder, title, text).show()
        }
    }

    protected open fun handleFailure(failure: Failure?) {
        when (failure) {
            is Failure.FeatureFailure -> {
                if (failure.errorMessage().isEmpty()) {
                    notify("failure_server_error".localized())
                } else {
                    notify(failure.titleKey.localized(), failure.errorMessage().localized())
                }
            }
            is Failure.ServerError -> {
                notify("failure_server_error".localized())
                trackServerError(failure)
            }
            is Failure.UserSessionExpired -> {
                aptoPlatformProtocol.logout()
                notify("session_expired_error".localized())
            }
            is Failure.RateLimitFailure -> {
                notify("failure_server_error".localized(), "error_transport_rate_limit".localized())
            }
        }
    }

    private fun trackServerError(failure: Failure.ServerError) {
        if (!BuildConfig.DEBUG) {
            analytics.track(Event.UnknownServerError, failure.toJSonObject())
        }
    }

    override fun startFragment() = this
    override fun lastFragment() = this
    override fun clearChildElements() {
        // do nothing
    }
    override fun removeFromStack(animated: Boolean) {
        // do nothing
    }
    override fun onPresented() {
        // do nothing
    }

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

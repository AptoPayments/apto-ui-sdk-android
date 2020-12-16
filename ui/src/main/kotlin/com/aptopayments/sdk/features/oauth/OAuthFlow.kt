package com.aptopayments.sdk.features.oauth

import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.repository.oauth.remote.OAUTH_FINISHED_URL
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectContract
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserContract

private const val OAUTH_CONNECT_TAG = "OAuthConnectFragment"
private const val WEB_BROWSER_TAG = "WebBrowserFragment"

internal class OAuthFlow(
    val config: OAuthConfig,
    val onBack: () -> Unit,
    val onFinish: (oauthAttempt: OAuthAttempt) -> Unit
) : Flow(), OAuthConnectContract.Delegate, WebBrowserContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.oauthConnectFragment(config, OAUTH_CONNECT_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(OAUTH_CONNECT_TAG) as? OAuthConnectContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(WEB_BROWSER_TAG) as? WebBrowserContract.View)?.let {
            it.delegate = this
        }
    }

    //
    // OAuthConnect
    //
    override fun onBackFromOAuthConnect() = onBack.invoke()

    override fun show(url: String) {
        val fragment = fragmentFactory.webBrowserFragment(url = url, tag = WEB_BROWSER_TAG)
        fragment.delegate = this
        push(fragment = fragment as BaseFragment)
    }

    override fun onOAuthSuccess(oauthAttempt: OAuthAttempt) = onFinish(oauthAttempt)

    //
    // WebBrowserContract handling
    //

    override fun onCloseWebBrowser() {
        popFragment()
        (startFragment() as? OAuthConnectContract.View)?.reloadStatus()
    }

    override fun shouldStopLoadingAndClose(url: String) = url.startsWith(OAUTH_FINISHED_URL, ignoreCase = true)
}

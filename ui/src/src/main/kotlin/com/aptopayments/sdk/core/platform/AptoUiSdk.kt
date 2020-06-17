package com.aptopayments.sdk.core.platform

import android.app.Activity
import android.app.Application
import android.content.pm.PackageInfo
import androidx.fragment.app.FragmentActivity
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformDelegate
import com.aptopayments.core.platform.AptoSdkEnvironment
import com.aptopayments.sdk.BuildConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.repository.IssueCardAdditionalFieldsRepositoryImpl
import com.aptopayments.sdk.utils.FontsUtil
import org.koin.core.module.Module
import java.lang.ref.WeakReference

interface AptoUiSdkProtocol {
    var cardOptions: CardOptions

    fun initializeWithApiKey(
        application: Application,
        apiKey: String,
        environment: AptoSdkEnvironment = AptoSdkEnvironment.PRD,
        extraModules: List<((MutableList<Any>) -> Any)> = listOf()
    )

    fun initialize(application: Application, extraModules: List<((MutableList<Any>) -> Any)> = listOf())

    fun setApiKey(apiKey: String, environment: AptoSdkEnvironment)

    fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    fun setCardIssueAdditional(fields: Map<String, Any>?)
    fun userTokenPresent(): Boolean
    fun getAppVersion(activity: FragmentActivity?): String
    fun registerFirebaseToken(firebaseToken: String)
    fun logout()
}

object AptoUiSdk : AptoUiSdkProtocol {

    internal var cardFlow: WeakReference<CardFlow>? = null

    override var cardOptions: CardOptions = CardOptions()

    override fun initializeWithApiKey(
        application: Application,
        apiKey: String,
        environment: AptoSdkEnvironment,
        extraModules: List<((MutableList<Any>) -> Any)>
    ) {
        initialize(application, extraModules)
        setApiKey(apiKey, environment)
    }

    override fun initialize(application: Application, extraModules: List<(MutableList<Any>) -> Any>) {
        val list = getModuleList(extraModules)
        AptoPlatform.setUiModules(list)
        AptoPlatform.initialize(application)
    }

    override fun setApiKey(apiKey: String, environment: AptoSdkEnvironment) {
        AptoPlatform.setApiKey(apiKey, environment)
    }

    fun setDelegate(delegate: AptoPlatformDelegate) {
        AptoPlatform.delegate = delegate
    }

    override fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        this.cardOptions = cardOptions
        with(cardOptions.fontOptions) {
            FontsUtil.overrideFonts(
                regularFont = regularFont,
                mediumFont = mediumFont,
                semiBoldFont = semiBoldFont,
                boldFont = boldFont
            )
        }

        val flow = CardFlow()
        cardFlow = WeakReference(flow)
        flow.init { result ->
            result.either({
                onError?.invoke(it)
            }) {
                from.startActivity(CardActivity.callingIntent(from))
                from.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                onSuccess?.invoke()
            }
        }
    }

    override fun setCardIssueAdditional(fields: Map<String, Any>?) {
        IssueCardAdditionalFieldsRepositoryImpl.fields = fields
    }

    override fun userTokenPresent(): Boolean = AptoPlatform.userTokenPresent()

    override fun registerFirebaseToken(firebaseToken: String) {
        AptoPlatform.registerFirebaseToken(firebaseToken)
    }

    override fun getAppVersion(activity: FragmentActivity?): String {
        return activity?.let {
            val packageInfo: PackageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            "${packageInfo.versionName} - (${BuildConfig.VERSION_NAME})"
        } ?: ""
    }

    override fun logout() {
        AptoPlatform.logout()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getModuleList(extraModules: List<(MutableList<Any>) -> Any>): MutableList<Module> {
        val list = mutableListOf(applicationModule, useCaseModule, viewModelModule)
        extraModules.forEach { it.invoke(list as MutableList<Any>) }

        return list
    }
}

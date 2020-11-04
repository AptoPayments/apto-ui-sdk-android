package com.aptopayments.sdk.core.platform

import android.app.Activity
import android.app.Application
import android.content.pm.PackageInfo
import androidx.fragment.app.FragmentActivity
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformDelegate
import com.aptopayments.mobile.platform.AptoSdkEnvironment
import com.aptopayments.sdk.BuildConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.repository.*
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

    /**
     * startCardFlow allows starting the UI-SDK
     *
     * @param from, Activity, The activity from where you are starting the SDK
     * @param cardOptions [CardOptions] The UI SDK has multiple features that can be enabled / disabled.
     * This parameter is used to enable / disable card management features and can be used to define the card theme and fonts.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    /**
     * startManageCard with a certain Card Id
     *
     * @param from, Activity, The activity from where you are starting the SDK
     * @param cardOptions [CardOptions] The UI SDK has multiple features that can be enabled / disabled.
     * This parameter is used to enable / disable card management features and can be used to define the card theme and fonts.
     * @param cardId String, Card id that will be opened.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startManageCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        cardId: String,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    fun startCardApplicationFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    @Deprecated(message = "Use setCard(metadata) or setUser(metadata) instead.")
    fun setCardIssueAdditional(fields: Map<String, Any>?)

    /**
     * @param metadata Card metadata, an arbitrary string up to 256 characters, that will be stored along with the card.
     */
    fun setCardMetadata(metadata: String)

    /**
     * @param metadata user metadata, an arbitrary string up to 256 characters, that will be stored along with the user.
     */
    fun setUserMetadata(metadata: String)
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
        startFlow(from, cardOptions, onSuccess, onError)
    }

    override fun startManageCardFlow(
        from: Activity,
        cardOptions: CardOptions,
        cardId: String,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        AptoPlatform.koin.get<ManageCardIdRepository>().data = cardId
        startFlow(from, cardOptions, onSuccess = onSuccess, onError = onError)
    }

    override fun startCardApplicationFlow(
        from: Activity,
        cardOptions: CardOptions,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        AptoPlatform.koin.get<ForceIssueCardRepository>().data = true
        startFlow(from, cardOptions, onSuccess = onSuccess, onError = onError)
    }

    private fun startFlow(
        from: Activity,
        cardOptions: CardOptions,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        this.cardOptions = cardOptions
        overrideFonts(cardOptions)
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

    private fun overrideFonts(cardOptions: CardOptions) {
        with(cardOptions.fontOptions) {
            FontsUtil.overrideFonts(
                regularFont = regularFont,
                mediumFont = mediumFont,
                semiBoldFont = semiBoldFont,
                boldFont = boldFont
            )
        }
    }

    override fun setCardIssueAdditional(fields: Map<String, Any>?) {
        IssueCardAdditionalFieldsRepositoryImpl.fields = fields
    }

    override fun setCardMetadata(metadata: String) {
        AptoPlatform.koin.get<CardMetadataRepository>().data = metadata
    }

    override fun setUserMetadata(metadata: String) {
        AptoPlatform.koin.get<UserMetadataRepository>().data = metadata
    }

    override fun userTokenPresent(): Boolean = AptoPlatform.userTokenPresent()

    override fun registerFirebaseToken(firebaseToken: String) {
        AptoPlatform.registerFirebaseToken(firebaseToken)
    }

    override fun getAppVersion(activity: FragmentActivity?): String {
        return activity?.let {
            val packageInfo: PackageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            "${packageInfo.versionName} - (${BuildConfig.LIBRARY_VERSION_NAME})"
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

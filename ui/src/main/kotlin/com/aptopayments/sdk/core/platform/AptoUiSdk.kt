package com.aptopayments.sdk.core.platform

import android.app.Activity
import android.app.Application
import android.content.pm.PackageInfo
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ProcessLifecycleOwner
import com.aptopayments.mobile.di.ExtraModule
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformDelegate
import com.aptopayments.mobile.platform.AptoPlatformWebTokenProvider
import com.aptopayments.mobile.platform.AptoSdkEnvironment
import com.aptopayments.sdk.BuildConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.core.usecase.SaveFlowConfigurationDataUseCase
import com.aptopayments.sdk.core.usecase.SaveFlowConfigurationDataUseCase.Params
import com.aptopayments.sdk.data.InitializationData
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.utils.FontsUtil
import org.koin.core.module.Module
import org.koin.dsl.module
import java.lang.ref.WeakReference

interface AptoUiSdkProtocol {
    var cardOptions: CardOptions

    fun initializeWithApiKey(
        application: Application,
        apiKey: String,
        environment: AptoSdkEnvironment = AptoSdkEnvironment.PRD,
        extraModules: List<ExtraModule> = listOf()
    )

    fun initialize(application: Application, extraModules: List<ExtraModule> = listOf())

    fun setApiKey(apiKey: String, environment: AptoSdkEnvironment)

    /**
     * startCardFlow allows starting the UI-SDK
     *
     * @param from, Activity, The activity from where you are starting the SDK
     * @param cardOptions [CardOptions] The UI SDK has multiple features that can be enabled / disabled.
     * This parameter is used to enable / disable card management features and can be used to define the card theme and fonts.
     * @param initializationData [InitializationData] This data contains information that will be stored with the user and the card when creating them.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        initializationData: InitializationData? = null,
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
     * @param initializationData This data contains information that will be stored with the user and the card when creating them.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startManageCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        cardId: String,
        initializationData: InitializationData? = null,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    /**
     * Launch the Card issuance flow.
     *
     * @param from, Activity, The activity from where you are starting the SDK
     * @param cardOptions [CardOptions] The UI SDK has multiple features that can be enabled / disabled.
     * This parameter is used to enable / disable card management features and can be used to define the card theme and fonts.
     * @param initializationData This data contains information that will be stored with the user and the card when creating them.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startCardApplicationFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        initializationData: InitializationData? = null,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    fun userTokenPresent(): Boolean
    fun getAppVersion(activity: FragmentActivity?): String
    fun registerFirebaseToken(firebaseToken: String)
    fun logout()
}

object AptoUiSdk : AptoUiSdkProtocol {

    private val saveInitializationDataUseCase: SaveFlowConfigurationDataUseCase by lazy { AptoPlatform.koin.get() }

    internal var cardFlow: WeakReference<CardFlow>? = null

    override var cardOptions: CardOptions = CardOptions()

    override fun initializeWithApiKey(
        application: Application,
        apiKey: String,
        environment: AptoSdkEnvironment,
        extraModules: List<ExtraModule>
    ) {
        initialize(application, extraModules)
        setApiKey(apiKey, environment)
    }

    override fun initialize(application: Application, extraModules: List<ExtraModule>) {
        val list = getModuleList(extraModules)
        AptoPlatform.setUiModules(list)
        AptoPlatform.initialize(application)
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    override fun setApiKey(apiKey: String, environment: AptoSdkEnvironment) {
        AptoPlatform.setApiKey(apiKey, environment)
    }

    fun setDelegate(delegate: AptoPlatformDelegate) {
        AptoPlatform.delegate = delegate
    }

    fun setWebTokenProvider(provider: AptoPlatformWebTokenProvider) {
        AptoPlatform.webTokenProvider = provider
    }

    override fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions,
        initializationData: InitializationData?,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        saveInitializationDataUseCase(Params(initializationData = initializationData))
        startFlow(from, cardOptions, onSuccess, onError)
    }

    override fun startManageCardFlow(
        from: Activity,
        cardOptions: CardOptions,
        cardId: String,
        initializationData: InitializationData?,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        saveInitializationDataUseCase(Params(initializationData = initializationData, manageCardId = cardId))
        startFlow(from, cardOptions, onSuccess = onSuccess, onError = onError)
    }

    override fun startCardApplicationFlow(
        from: Activity,
        cardOptions: CardOptions,
        initializationData: InitializationData?,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        saveInitializationDataUseCase(
            Params(
                initializationData = initializationData,
                forceApplyToCard = true
            )
        )
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
        val flow = AptoPlatform.koin.get<CardFlow>()
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

    private fun getModuleList(extraModules: List<ExtraModule>): MutableList<Module> {
        val list = mutableListOf(applicationModule, useCaseModule, viewModelModule)
        val extra = extraModules.filter { it.module is Module }.map { it.module as Module }
        list.addAll(extra)
        return list
    }
}

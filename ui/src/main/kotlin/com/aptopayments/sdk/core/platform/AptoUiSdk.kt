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
import com.aptopayments.sdk.core.usecase.SaveInitializationDataUseCase
import com.aptopayments.sdk.core.usecase.SaveInitializationDataUseCase.InitializationData
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
     * @param userMetadata: A string up to 256 characters that will be attached to the user after signing up.
     * @param cardMetadata: A string up to 256 characters that will be attached to the card after issuance.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startManageCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        cardId: String,
        userMetadata: String? = null,
        cardMetadata: String? = null,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    /**
     * Launch the Card issuance flow.
     *
     * @param from, Activity, The activity from where you are starting the SDK
     * @param cardOptions [CardOptions] The UI SDK has multiple features that can be enabled / disabled.
     * This parameter is used to enable / disable card management features and can be used to define the card theme and fonts.
     * @param userMetadata: A string up to 256 characters that will be attached to the user after signing up.
     * @param cardMetadata: A string up to 256 characters that will be attached to the card after issuance.
     * @param onSuccess This is the callback closure called once the Apto UI SDK has been initialized.
     * @param onError This is the callback closure called if there was a failure during the SDK initialization process.
     */
    fun startCardApplicationFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        userMetadata: String? = null,
        cardMetadata: String? = null,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    @Deprecated(message = "Use the cardMetadata parameter when launching the SDK.")
    fun setCardIssueAdditional(fields: Map<String, Any>?)

    fun userTokenPresent(): Boolean
    fun getAppVersion(activity: FragmentActivity?): String
    fun registerFirebaseToken(firebaseToken: String)
    fun logout()
}

object AptoUiSdk : AptoUiSdkProtocol {

    private val saveInitializationDataUseCase: SaveInitializationDataUseCase by lazy { AptoPlatform.koin.get() }

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
        saveInitializationDataUseCase.run(null)
    }

    override fun startManageCardFlow(
        from: Activity,
        cardOptions: CardOptions,
        cardId: String,
        userMetadata: String?,
        cardMetadata: String?,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        saveInitializationDataUseCase(
            InitializationData(
                userMetadata = userMetadata,
                cardMetadata = cardMetadata
            )
        )
        startFlow(from, cardOptions, onSuccess = onSuccess, onError = onError)
    }

    override fun startCardApplicationFlow(
        from: Activity,
        cardOptions: CardOptions,
        userMetadata: String?,
        cardMetadata: String?,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        saveInitializationDataUseCase(
            InitializationData(
                userMetadata = userMetadata,
                cardMetadata = cardMetadata,
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

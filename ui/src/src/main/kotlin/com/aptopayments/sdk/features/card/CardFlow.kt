package com.aptopayments.sdk.features.card

import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.network.NetworkHandler
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.repository.UserSessionRepository
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.AuthFlow
import com.aptopayments.sdk.features.managecard.ManageCardFlow
import com.aptopayments.sdk.features.newcard.NewCardFlow
import com.aptopayments.sdk.features.selectcountry.CardProductSelectorFlow
import java.lang.reflect.Modifier
import javax.inject.Inject

private const val NO_NETWORK_TAG = "NoNetworkFragment"
private const val MAINTENANCE_TAG = "MaintenanceFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardFlow : Flow() {

    @Inject lateinit var userSessionRepository: UserSessionRepository
    @Inject lateinit var networkHandler: NetworkHandler
    @Inject lateinit var analyticsManager: AnalyticsServiceContract
    private lateinit var contextConfiguration: ContextConfiguration
    private var noNetworkFragmentShown = false
    private var maintenanceFragmentShown = false
    private var initialized = false

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        appComponent.inject(this)
        subscribeToNetworkAvailabilityEvent()
        subscribeToMaintenanceModeEvent()
        if (networkHandler.isConnected != true) {
            val fragment = fragmentFactory.noNetworkFragment(UIConfig.uiTheme, NO_NETWORK_TAG)
            setStartElement(fragment as BaseFragment)
            noNetworkFragmentShown = true
            onInitComplete(Either.Right(Unit))
            return
        }
        initialFlow { result ->
            result.either({ onInitComplete(Either.Left(it)) }, { flow ->
                setStartElement(element = flow)
                initialized = true
                onInitComplete(Either.Right(Unit))
            })
        }
    }

    override fun restoreState() = Unit

    private fun initialFlow(onInitComplete: (Either<Failure, Flow>) -> Unit) {
        AptoPlatform.fetchContextConfiguration(false) { result ->
            result.either({ onInitComplete(Either.Left(it)) }, {
                this.contextConfiguration = it
                val trackerAccessToken = contextConfiguration.projectConfiguration.trackerAccessToken
                if (contextConfiguration.projectConfiguration.isTrackerActive == true) {
                    trackerAccessToken?.let {
                        if (it.isNotEmpty()) analyticsManager.initialize(it)
                    }
                }
                if (!userSessionRepository.userSession.isValid()) {
                    initAuthFlow (onInitComplete)
                } else {
                    initNewOrExistingCardFlow { initResult ->
                        initResult.either({ failure ->
                            when (failure) {
                                is Failure.UserSessionExpired -> {
                                    initAuthFlow (onInitComplete)
                                }
                                else -> {onInitComplete(Either.Left(failure))}
                            }
                        }, { flow -> onInitComplete (Either.Right(flow)) })
                    }
                }
            })
        }
    }

    private fun initNewOrExistingCardFlow(onComplete: (Either<Failure, Flow>) -> Unit) {
        AptoPlatform.fetchCards { result ->
            result.either({ onComplete(Either.Left(it)) }) { cards ->
                cards.firstOrNull { it.state != Card.CardState.CANCELLED }?.let { card ->
                    initManageCardFlow(cardId = card.accountID, onComplete = onComplete)
                } ?: initCardProductSelectorFlow(onComplete)
            }
        }
    }

    private fun showNewOrExistingCardFlow(onComplete: (Either<Failure, Unit>) -> Unit) {
        showLoading()
        initNewOrExistingCardFlow { initResult ->
            hideLoading()
            initResult.either({ onComplete(Either.Left(it))} ) { flow ->
                push(flow = flow, animated = true)
            }
        }
    }

    //
    // Auth Flow
    //
    private fun initAuthFlow(onComplete: (Either<Failure, Flow>) -> Unit) {
        val flow = AuthFlow(
                contextConfiguration = contextConfiguration,
                onBack = { rootActivity()?.finish() },
                onFinish= {
                    showNewOrExistingCardFlow {
                        result -> result.either(::handleFailure) {} }
                }
        )
        flow.init { initResult ->
            initResult.either({ onComplete(Either.Left(it)) }) {
                onComplete(Either.Right(flow))
            }
        }
    }

    //
    // Card Product Selector Flow
    //
    private fun initCardProductSelectorFlow(onComplete: (Either<Failure, Flow>) -> Unit) {
        val flow = CardProductSelectorFlow(
                onBack = { rootActivity()?.finish() },
                onFinish = { cardProductId -> initNewCardFlow(cardProductId) }
        )
        flow.init { initResult ->
            initResult.either({ onComplete(Either.Left(it)) }) {
                onComplete(Either.Right(flow))
            }
        }
    }

    //
    // New Card Flow
    //
    private fun initNewCardFlow(cardProductId: String) {
        val flow = NewCardFlow(
                cardProductId = cardProductId,
                onBack = { rootActivity()?.finish() },
                onFinish = { cardId -> showManageCardFlow(cardId) }
        )
        showLoading()
        flow.init { initResult ->
            hideLoading()
            initResult.either({ Either.Left(it) }) {
                push(flow = flow)
            }
        }
    }

    override fun attachTo(activity: AppCompatActivity, fragmentContainer: Int) {
        super.attachTo(activity, fragmentContainer)
        subscribeToSessionInvalidEvent()
    }

    override fun detachFromActivity() {
        userSessionRepository.unsubscribeSessionInvalidListener(this)
        networkHandler.unsubscribeNetworkReachabilityListener(this)
        super.detachFromActivity()
    }

    private fun subscribeToSessionInvalidEvent() {
        userSessionRepository.subscribeSessionInvalidListener(this) {
            initAuthFlow { initResult ->
                initResult.either(::handleFailure) { flow ->
                    clearChildElements()
                    popAllFragments(animated = true) { push(flow) }
                }
            }
        }
    }

    private fun subscribeToNetworkAvailabilityEvent() {
        networkHandler.subscribeNetworkReachabilityListener(this) { available ->
            if (!available && !noNetworkFragmentShown) {
                val fragment = fragmentFactory.noNetworkFragment(UIConfig.uiTheme, NO_NETWORK_TAG)
                push(fragment as BaseFragment)
                noNetworkFragmentShown = true
            }
            else if (available && noNetworkFragmentShown) {
                popFragment()
                noNetworkFragmentShown = false
                if (!initialized) {
                    initialFlow { result ->
                        result.either(::handleFailure) { flow ->
                            push(flow, animated = false)
                            initialized = true
                            Unit
                        }
                    }
                }
            }
        }
    }

    private fun subscribeToMaintenanceModeEvent() {
        networkHandler.subscribeMaintenanceListener(this) { available ->
            if (!available && !maintenanceFragmentShown) {
                val fragment = fragmentFactory.maintenanceFragment(UIConfig.uiTheme, MAINTENANCE_TAG)
                push(fragment as BaseFragment)
                maintenanceFragmentShown = true
            }
            else if (available && maintenanceFragmentShown) {
                popFragment()
                maintenanceFragmentShown = false
            }
        }
    }

    //
    // Manage Card Flow
    //
    private fun showManageCardFlow(cardId: String) {
        initManageCardFlow(cardId = cardId) { initResult ->
            initResult.either({::handleFailure}) { flow ->
                clearChildElements()
                push(flow = flow)
            }
        }
    }

    private fun initManageCardFlow(cardId: String, onComplete: (Either<Failure, Flow>) -> Unit) {
        val flow = ManageCardFlow(
                cardId = cardId,
                contextConfiguration = contextConfiguration,
                onClose = { rootActivity()?.finish() }
        )
        flow.init { initResult ->
            initResult.either({ onComplete (Either.Left(it)) }) {
                onComplete(Either.Right(flow))
            }
        }
    }
}

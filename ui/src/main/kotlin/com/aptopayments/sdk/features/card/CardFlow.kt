package com.aptopayments.sdk.features.card

import androidx.appcompat.app.AppCompatActivity
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.getOrElse
import com.aptopayments.mobile.network.NetworkHandler
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.usecase.ShouldCreatePasscodeUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.auth.AuthFlow
import com.aptopayments.sdk.features.managecard.ManageCardFlow
import com.aptopayments.sdk.features.newcard.NewCardFlow
import com.aptopayments.sdk.features.passcode.CreatePasscodeFlow
import com.aptopayments.sdk.features.passcode.PasscodeMode
import com.aptopayments.sdk.features.selectcountry.CardProductSelectorFlow
import com.aptopayments.sdk.repository.StatementRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

private const val NO_NETWORK_TAG = "NoNetworkFragment"
private const val MAINTENANCE_TAG = "MaintenanceFragment"

internal class CardFlow : Flow(), KoinComponent {

    val aptoPlatformProtocol: AptoPlatformProtocol by inject()
    private val networkHandler: NetworkHandler by inject()
    val analyticsManager: AnalyticsServiceContract by inject()
    private val statementRepository: StatementRepository by inject()
    private val shouldCreatePasscodeUseCase: ShouldCreatePasscodeUseCase by inject()

    private lateinit var contextConfiguration: ContextConfiguration
    private var noNetworkFragmentShown = false
    private var maintenanceFragmentShown = false
    private var initialized = false

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        subscribeToNetworkAvailabilityEvent()
        subscribeToMaintenanceModeEvent()
        if (!networkHandler.isConnected) {
            val fragment = fragmentFactory.noNetworkFragment(NO_NETWORK_TAG)
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
            result.either({ onInitComplete(Either.Left(it)) }, { configuration ->
                this.contextConfiguration = configuration
                val trackerAccessToken = contextConfiguration.projectConfiguration.trackerAccessToken
                if (contextConfiguration.projectConfiguration.isTrackerActive == true) {
                    trackerAccessToken?.let { token ->
                        if (token.isNotEmpty()) analyticsManager.initialize(token)
                    }
                }
                if (!aptoPlatformProtocol.userTokenPresent()) {
                    initAuthFlow(onInitComplete)
                } else {
                    initNewOrExistingCardFlow { initResult ->
                        initResult.either({ failure ->
                            when (failure) {
                                is Failure.UserSessionExpired -> initAuthFlow(onInitComplete)
                                else -> onInitComplete(Either.Left(failure))
                            }
                        }, { flow -> onInitComplete(Either.Right(flow)) })
                    }
                }
            })
        }
    }

    private fun initNewOrExistingCardFlow(onComplete: (Either<Failure, Flow>) -> Unit) {
        AptoPlatform.fetchCards { result ->
            result.either({ onComplete(Either.Left(it)) }) { cards ->
                cards.firstOrNull { it.state != Card.CardState.CANCELLED }?.let { card ->
                    initSetLoginPinFlow(cardId = card.accountID, onInitComplete = onComplete)
                } ?: initCardProductSelectorFlow(onComplete)
            }
        }
    }

    private fun showNewOrExistingCardFlow() {
        showLoading()
        initNewOrExistingCardFlow { initResult ->
            hideLoading()
            initResult.either(::handleFailure) { flow -> push(flow = flow, animated = true) }
        }
    }

    //
    // Auth Flow
    //
    private fun initAuthFlow(onInitComplete: (Either<Failure, Flow>) -> Unit) {
        val flow = AuthFlow(
            contextConfiguration = contextConfiguration,
            onBack = { rootActivity()?.finish() },
            onFinish = { showNewOrExistingCardFlow() }
        )
        flow.init { initResult ->
            initResult.either({ onInitComplete(Either.Left(it)) }, { onInitComplete(Either.Right(flow)) })
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
            onFinish = { cardId -> showSetLoginPinFlow(cardId) }
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
        aptoPlatformProtocol.unsubscribeSessionInvalidListener(this)
        networkHandler.unsubscribeNetworkReachabilityListener(this)
        super.detachFromActivity()
    }

    private fun subscribeToSessionInvalidEvent() {
        aptoPlatformProtocol.subscribeSessionInvalidListener(this) {
            initAuthFlow { initResult ->
                initResult.either(::handleFailure) { flow ->
                    clearChildElements()
                    popAllFragments { push(flow) }
                    statementRepository.clearCache()
                }
            }
        }
    }

    private fun subscribeToNetworkAvailabilityEvent() {
        networkHandler.subscribeNetworkReachabilityListener(this) { available ->
            if (!available && !noNetworkFragmentShown) {
                val fragment = fragmentFactory.noNetworkFragment(NO_NETWORK_TAG)
                push(fragment as BaseFragment)
                noNetworkFragmentShown = true
            } else if (available && noNetworkFragmentShown) {
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

    private fun subscribeToMaintenanceModeEvent() = networkHandler.subscribeMaintenanceListener(this) { available ->
        if (!available && !maintenanceFragmentShown) {
            val fragment = fragmentFactory.maintenanceFragment(MAINTENANCE_TAG)
            push(fragment as BaseFragment)
            maintenanceFragmentShown = true
        } else if (available && maintenanceFragmentShown) {
            popFragment()
            maintenanceFragmentShown = false
        }
    }

    //
    // Manage Card Flow
    //
    private fun showSetLoginPinFlow(cardId: String) {
        initSetLoginPinFlow(cardId) { initResult ->
            initResult.either({ ::handleFailure }) { flow ->
                clearChildElements()
                push(flow = flow)
            }
        }
    }

    private fun initSetLoginPinFlow(cardId: String, onInitComplete: (Either<Failure, Flow>) -> Unit) {
        val shouldCreate = shouldCreatePasscodeUseCase().getOrElse { false }
        if (shouldCreate) {
            val flow = CreatePasscodeFlow(mode = PasscodeMode.CREATE, onFinish = { showManageCardFlow(cardId) })
            flow.init { initResult ->
                initResult.either({ onInitComplete(Either.Left(it)) }) { onInitComplete(Either.Right(flow)) }
            }
        } else {
            initManageCardFlow(cardId, onInitComplete)
        }
    }

    private fun showManageCardFlow(cardId: String) {
        initManageCardFlow(cardId = cardId) { initResult ->
            initResult.either({ ::handleFailure }) { flow ->
                clearChildElements()
                push(flow = flow)
            }
        }
    }

    private fun initManageCardFlow(cardId: String, onInitComplete: (Either<Failure, Flow>) -> Unit) {
        val flow = ManageCardFlow(cardId, contextConfiguration, onClose = { rootActivity()?.finish() })
        flow.init { initResult ->
            initResult.either({ onInitComplete(Either.Left(it)) }) {
                onInitComplete(Either.Right(flow))
            }
        }
    }
}

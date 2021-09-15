package com.aptopayments.sdk.repository

import androidx.fragment.app.FragmentActivity
import com.aptopayments.mobile.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface IAPHelper {
    val state: StateFlow<ProvisioningState>
    fun satisfyHardwareRequisites(): Boolean
    suspend fun initProcess()
    fun registerDataChanged()
    fun unregisterDataChanged()
    suspend fun startInAppProvisioningFlow(activity: FragmentActivity)
    fun onActivityResult(requestCode: Int, result: Boolean, scope: CoroutineScope): Boolean
}

internal class IAPHelperFake : IAPHelper {
    override val state = MutableStateFlow<ProvisioningState>(ProvisioningState.CanNotBeAdded())
    override fun satisfyHardwareRequisites() = false
    override suspend fun initProcess() {
        // Do Nothing
    }
    override fun registerDataChanged() {
        // Do Nothing
    }
    override fun unregisterDataChanged() {
        // Do Nothing
    }
    override suspend fun startInAppProvisioningFlow(activity: FragmentActivity) {
        // Do Nothing
    }
    override fun onActivityResult(requestCode: Int, result: Boolean, scope: CoroutineScope) = false
}

sealed class ProvisioningState(open val failure: Failure? = null) {
    object Idle : ProvisioningState()
    object Loading : ProvisioningState()
    object AlreadyAdded : ProvisioningState()
    class CanBeAdded(override val failure: Failure? = null) : ProvisioningState()
    class CanNotBeAdded(override val failure: Failure? = null) : ProvisioningState(failure)
}

class UnableToProvisionCard : Failure.FeatureFailure()
class CantFetchProvisioningDataException : RuntimeException()
class TokenizeException : RuntimeException()
class IncorrectStateException : RuntimeException()

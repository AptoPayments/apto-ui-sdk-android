package com.aptopayments.sdk.repository

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.exception.Failure
import kotlinx.coroutines.CoroutineScope

interface IAPHelper {
    val state: LiveData<ProvisioningState>
    fun satisfyHardwareRequisites(): Boolean
    suspend fun initProcess()
    fun registerDataChanged()
    fun unregisterDataChanged()
    suspend fun startInAppProvisioningFlow(activity: FragmentActivity)
    fun onActivityResult(requestCode: Int, result: Boolean, scope: CoroutineScope): Boolean
}

internal class IAPHelperMock : IAPHelper {
    override val state = MutableLiveData<ProvisioningState>(ProvisioningState.CanNotBeAdded())
    override fun satisfyHardwareRequisites() = false
    override suspend fun initProcess() {}
    override fun registerDataChanged() {}
    override fun unregisterDataChanged() {}
    override suspend fun startInAppProvisioningFlow(activity: FragmentActivity) {}
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

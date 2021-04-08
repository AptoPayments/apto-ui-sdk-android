package com.aptopayments.sdk.repository

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.exception.Failure

interface IAPHelper {
    val failure: LiveData<Failure>
    val showAddCardButton: LiveData<Boolean>
    fun satisfyHardwareRequisites(): Boolean
    suspend fun initProcess()
    fun registerDataChanged()
    fun unregisterDataChanged()
    fun createWallet(activity: FragmentActivity, requestCode: Int)
    fun setGooglePayAsDefaultPaymentApp(activity: FragmentActivity, requestCode: Int)
    suspend fun onGooglePaySetAsDefault()
    suspend fun startInAppProvisioningFlow(activity: FragmentActivity, requestCode: Int)
    suspend fun onWalletCreated()
}

internal class IAPHelperMock : IAPHelper {
    override val failure: LiveData<Failure> = MutableLiveData()
    override val showAddCardButton = MutableLiveData(false) as LiveData<Boolean>
    override fun satisfyHardwareRequisites() = false
    override suspend fun initProcess() {}
    override fun registerDataChanged() {}
    override fun unregisterDataChanged() {}
    override fun createWallet(activity: FragmentActivity, requestCode: Int) {}
    override fun setGooglePayAsDefaultPaymentApp(activity: FragmentActivity, requestCode: Int) {}
    override suspend fun onGooglePaySetAsDefault() {}
    override suspend fun startInAppProvisioningFlow(activity: FragmentActivity, requestCode: Int) {}
    override suspend fun onWalletCreated() {}
}

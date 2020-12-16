package com.aptopayments.sdk.features.inputdata

import com.aptopayments.mobile.data.user.*
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationCollectUserData
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressContract
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateContract
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailContract
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdContract
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameSurnameContract
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneContract
import org.koin.core.inject

private const val COLLECT_NAME_TAG = "CollectNameFragment"
private const val COLLECT_EMAIL_TAG = "CollectEmailFragment"
private const val COLLECT_ID_DOCUMENT_TAG = "CollectIdDocumentFragment"
private const val COLLECT_ADDRESS_TAG = "CollectAddressFragment"
private const val COLLECT_BIRTHDATE_TAG = "CollectBirthdateFragment"
private const val COLLECT_PHONE_TAG = "CollectPhoneFragment"

internal class CollectUserDataFlow(
    private val actionConfiguration: WorkflowActionConfigurationCollectUserData,
    private val onBack: () -> Unit,
    private val onFinish: () -> Unit
) : Flow(),
    CollectUserNameSurnameContract.Delegate,
    CollectUserEmailContract.Delegate,
    CollectUserIdContract.Delegate,
    CollectUserAddressContract.Delegate,
    CollectUserBirthdateContract.Delegate,
    CollectUserPhoneContract.Delegate {

    private val aptoPlatform: AptoPlatformProtocol by inject()
    private var step = 0
    private val dataPointMap = mutableMapOf<DataPoint.Type, DataPoint>()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = getFragmentForStep(step)
        if (fragment != null) {
            setStartElement(fragment)
            onInitComplete(Unit.right())
        } else {
            onInitComplete(Either.Left(NoConfigurationStep()))
        }
    }

    private fun hasMoreSteps() = step < (actionConfiguration.dataPoints.size - 1)

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun getFragmentForStep(step: Int): FlowPresentable? {
        val currentDataPoint = actionConfiguration.dataPoints[step]
        val fragment = when (currentDataPoint.type) {
            DataPoint.Type.NAME -> getNameFragment()
            DataPoint.Type.EMAIL -> getEmailFragment()
            DataPoint.Type.ID_DOCUMENT -> getIdFragment(currentDataPoint)
            DataPoint.Type.ADDRESS -> getAddressFragment(currentDataPoint)
            DataPoint.Type.BIRTHDATE -> getBirthdateFragment()
            DataPoint.Type.PHONE -> getPhoneFragment(currentDataPoint)
            else -> null
        }
        return fragment as FlowPresentable?
    }

    private fun getIdFragment(currentDataPoint: RequiredDataPoint) =
        fragmentFactory.collectIdDocumentFragment(
            dataPointMap[DataPoint.Type.ID_DOCUMENT] as IdDocumentDataPoint?,
            currentDataPoint.datapointConfiguration as IdDataPointConfiguration,
            COLLECT_ID_DOCUMENT_TAG
        ).apply { delegate = this@CollectUserDataFlow }

    private fun getBirthdateFragment() =
        fragmentFactory.collectBirthdateFragment(
            dataPointMap[DataPoint.Type.BIRTHDATE] as BirthdateDataPoint?,
            COLLECT_BIRTHDATE_TAG
        ).apply { delegate = this@CollectUserDataFlow }

    private fun getNameFragment() =
        fragmentFactory.collectNameFragment(dataPointMap[DataPoint.Type.NAME] as NameDataPoint?, COLLECT_NAME_TAG)
            .apply { delegate = this@CollectUserDataFlow }

    private fun getEmailFragment() =
        fragmentFactory.collectEmailFragment(
            dataPointMap[DataPoint.Type.EMAIL] as EmailDataPoint?,
            COLLECT_EMAIL_TAG
        ).apply { delegate = this@CollectUserDataFlow }

    private fun getAddressFragment(currentDataPoint: RequiredDataPoint): CollectUserAddressContract.View {
        val configuration = currentDataPoint.datapointConfiguration ?: AllowedCountriesConfiguration(emptyList())
        return fragmentFactory.collectAddressFragment(
            dataPointMap[DataPoint.Type.ADDRESS] as AddressDataPoint?,
            configuration as AllowedCountriesConfiguration,
            COLLECT_ADDRESS_TAG
        ).apply { delegate = this@CollectUserDataFlow }
    }

    private fun getPhoneFragment(currentDataPoint: RequiredDataPoint): CollectUserPhoneContract.View {
        val configuration = currentDataPoint.datapointConfiguration ?: AllowedCountriesConfiguration(emptyList())
        return fragmentFactory.collectPhoneFragment(
            dataPointMap[DataPoint.Type.PHONE] as PhoneDataPoint?,
            configuration as AllowedCountriesConfiguration,
            COLLECT_PHONE_TAG
        ).apply { delegate = this@CollectUserDataFlow }
    }

    override fun restoreState() {
        (fragmentWithTag(COLLECT_NAME_TAG) as? CollectUserNameSurnameContract.View)?.let { it.delegate = this }
        (fragmentWithTag(COLLECT_EMAIL_TAG) as? CollectUserEmailContract.View)?.let { it.delegate = this }
        (fragmentWithTag(COLLECT_ID_DOCUMENT_TAG) as? CollectUserIdContract.View)?.let { it.delegate = this }
        (fragmentWithTag(COLLECT_ADDRESS_TAG) as? CollectUserAddressContract.View)?.let { it.delegate = this }
        (fragmentWithTag(COLLECT_BIRTHDATE_TAG) as? CollectUserBirthdateContract.View)?.let { it.delegate = this }
        (fragmentWithTag(COLLECT_PHONE_TAG) as? CollectUserPhoneContract.View)?.let { it.delegate = this }
    }

    override fun onNameEnteredCorrectly(value: NameDataPoint) = onValueEnteredCorrectly(value)

    override fun onBackFromInputName() = onBackPressed()

    override fun onEmailEnteredCorrectly(value: EmailDataPoint) = onValueEnteredCorrectly(value)

    override fun onBackFromCollectEmail() = onBackPressed()

    override fun onIdEnteredCorrectly(value: IdDocumentDataPoint) = onValueEnteredCorrectly(value)

    override fun onBackFromCollectId() = onBackPressed()

    override fun onAddressSelected(value: AddressDataPoint) = onValueEnteredCorrectly(value)

    override fun onBackFromAddress() = onBackPressed()

    override fun onBirthdateEnteredCorrectly(value: BirthdateDataPoint) = onValueEnteredCorrectly(value)

    override fun onBackFromBirthdateVerification() = onBackPressed()

    override fun onPhoneEnteredCorrectly(value: PhoneDataPoint) = onValueEnteredCorrectly(value)

    override fun onBackFromCollectPhone() = onBackPressed()

    private fun onBackPressed() {
        if (step > 0) {
            step--
            popFragment()
        } else {
            onBack.invoke()
        }
    }

    private fun onValueEnteredCorrectly(data: DataPoint) {
        dataPointMap[data.getType()] = data
        if (hasMoreSteps()) {
            step++
            val fragment = getFragmentForStep(step)
            push(fragment as BaseFragment)
        } else {
            updateUser()
        }
    }

    private fun updateUser() {
        showLoading()
        val dataPointList = DataPointList(dataPointMap.values.toList())
        aptoPlatform.updateUserInfo(dataPointList) { result ->
            result.either(::handleFailure) {
                hideLoading()
                onFinish.invoke()
            }
        }
    }

    internal class NoConfigurationStep : Failure.FeatureFailure()
}

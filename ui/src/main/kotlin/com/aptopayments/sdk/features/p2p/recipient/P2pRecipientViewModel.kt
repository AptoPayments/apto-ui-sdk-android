package com.aptopayments.sdk.features.p2p.recipient

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.utils.extensions.asLiveData
import com.aptopayments.sdk.utils.extensions.distinctUntilChanged
import com.aptopayments.sdk.utils.extensions.isValidEmail
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlin.coroutines.resume

const val DEBOUNCE_TIME = 1000L

internal class P2pRecipientViewModel(
    private val debounceTime: Long,
    private val aptoPlatform: AptoPlatformProtocol,
) : BaseViewModel() {

    private var countryCode: String = ""

    private val requestFlow = MutableStateFlow(InputData())

    private val _configuration = MutableLiveData(Configuration())
    val configuration = _configuration.asLiveData()

    private val _state = MutableLiveData(State())
    val state = _state.distinctUntilChanged()

    private val _cardholder = MutableLiveData<CardHolder?>(null)
    val cardholder = _cardholder.asLiveData()

    val action = LiveEvent<Action>()

    private var job: Job? = null

    init {
        getConfiguration()
        observeInputToHitApi()
    }

    private fun observeInputToHitApi() {
        viewModelScope.launch {
            requestFlow.debounce(debounceTime).collect {
                when {
                    it.countryCode != null && it.phoneNumber != null ->
                        onValidInput(
                            phone = PhoneNumber(countryCode = it.countryCode, phoneNumber = it.phoneNumber)
                        )
                    it.email != null -> onValidInput(email = it.email)
                }
            }
        }
    }

    private fun getConfiguration() {
        aptoPlatform.fetchContextConfiguration(false) { result ->
            result.either({
                handleFailure(it)
            }) { configuration ->
                _configuration.postValue(
                    Configuration(
                        allowedCountries = configuration.projectConfiguration.allowedCountries.map { it.isoCode },
                        credential = getCredential(configuration),
                        companyName = configuration.projectConfiguration.name
                    )
                )
            }
        }
    }

    private fun onPhoneChanged(countryCode: String, phoneNumber: String, isValid: Boolean) {
        if (isValid) {
            cancelJob()
            requestFlow.value = InputData(countryCode = countryCode, phoneNumber = phoneNumber)
        } else {
            onInvalidInput()
        }
    }

    fun onPhoneCountryChanged(countryCode: String) {
        this.countryCode = countryCode
    }

    fun onPhoneNumberChanged(phoneNumber: String, isValid: Boolean) {
        onPhoneChanged(countryCode, phoneNumber, isValid)
    }

    fun onEmailChanged(email: String) {
        if (email.isValidEmail()) {
            cancelJob()
            requestFlow.value = InputData(email = email)
        } else {
            onInvalidInput()
        }
    }

    fun onContinueClicked() {
        _cardholder.value?.data?.let { data ->
            action.postValue(Action.Continue(data))
        }
    }

    private fun getCredential(configuration: ContextConfiguration) =
        if (configuration.projectConfiguration.primaryAuthCredential == DataPoint.Type.EMAIL) {
            Credential.EMAIL
        } else {
            Credential.PHONE
        }

    fun onValidInput(phone: PhoneNumber? = null, email: String? = null) {
        job = viewModelScope.launch {
            showLoading()
            _cardholder.postValue(null)
            getCardHolder(phone, email).either(
                { failure ->
                    hideLoading()
                    val error: RecipientError = P2PRecipientErrorMapper().invoke(failure)
                    if (error == RecipientError.NO_UI_ERROR) {
                        handleFailure(failure)
                    }
                    _state.value = State(error = error, showContinueButton = false)
                },
                {
                    hideLoading()
                    _cardholder.value =
                        CardHolder(data = it, id = email ?: "+${phone!!.countryCode} ${phone.phoneNumber}")
                    _state.value = State(error = RecipientError.NO_UI_ERROR, showContinueButton = true)
                }
            )
        }
    }

    private fun onInvalidInput() {
        cancelJob()
        _cardholder.value = null
        _state.postValue(State(showContinueButton = false, error = RecipientError.NO_UI_ERROR))
    }

    private fun cancelJob() {
        job?.cancel()
        job = null
        hideLoading()
    }

    private suspend fun getCardHolder(phone: PhoneNumber?, email: String?) =
        suspendCancellableCoroutine<Either<Failure, CardHolderData>> { cont ->
            aptoPlatform.p2pFindRecipient(phone = phone, email = email) { result ->
                cont.resume(result)
            }
        }

    enum class Credential(val description: String) {
        PHONE("p2p_transfer_main_screen_intro_phone_number_description"),
        EMAIL("p2p_transfer_main_screen_intro_email_description")
    }

    data class State(
        val error: RecipientError = RecipientError.NO_UI_ERROR,
        val showContinueButton: Boolean = false,
    )

    data class CardHolder(
        val data: CardHolderData? = null,
        val id: String? = ""
    )

    data class Configuration(
        val allowedCountries: List<String> = emptyList(),
        val credential: Credential = Credential.PHONE,
        val companyName: String = "",
    )

    private data class InputData(
        val countryCode: String? = null,
        val phoneNumber: String? = null,
        val email: String? = null,
    )

    sealed class Action {
        class Continue(val cardholder: CardHolderData) : Action()
    }
}

package com.aptopayments.sdk.features.selectbalancestore

import com.aptopayments.mobile.data.card.SelectBalanceStoreError
import com.aptopayments.mobile.data.card.SelectBalanceStoreResult
import com.aptopayments.sdk.features.analytics.Event

fun SelectBalanceStoreResult.getErrorEvent(): Event {
    return when (this.error) {
        SelectBalanceStoreError.COUNTRY_UNSUPPORTED -> Event.SelectBalanceStoreOauthConfirmCountryUnsupported
        SelectBalanceStoreError.REGION_UNSUPPORTED -> Event.SelectBalanceStoreOauthConfirmRegionUnsupported
        SelectBalanceStoreError.ADDRESS_UNVERIFIED -> Event.SelectBalanceStoreOauthConfirmAddressUnverified
        SelectBalanceStoreError.CURRENCY_UNSUPPORTED -> Event.SelectBalanceStoreOauthConfirmCurrencyUnsupported
        SelectBalanceStoreError.CANNOT_CAPTURE_FUNDS -> Event.SelectBalanceStoreOauthConfirmCannotCaptureFunds
        SelectBalanceStoreError.INSUFFICIENT_FUNDS -> Event.SelectBalanceStoreOauthConfirmInsufficientFunds
        SelectBalanceStoreError.BALANCE_NOT_FOUND -> Event.SelectBalanceStoreOauthConfirmBalanceNotFound
        SelectBalanceStoreError.ACCESS_TOKEN_INVALID -> Event.SelectBalanceStoreOauthConfirmAccessTokenInvalid
        SelectBalanceStoreError.SCOPES_REQUIRED -> Event.SelectBalanceStoreOauthConfirmScopesRequired
        SelectBalanceStoreError.LEGAL_NAME_MISSING -> Event.SelectBalanceStoreOauthConfirmLegalNameMissing
        SelectBalanceStoreError.DATE_OF_BIRTH_MISSING -> Event.SelectBalanceStoreOauthConfirmDobMissing
        SelectBalanceStoreError.DATE_OF_BIRTH_ERROR -> Event.SelectBalanceStoreOauthConfirmDobInvalid
        SelectBalanceStoreError.ADDRESS_MISSING -> Event.SelectBalanceStoreOauthConfirmAddressMissing
        SelectBalanceStoreError.EMAIL_MISSING -> Event.SelectBalanceStoreOauthConfirmEmailMissing
        SelectBalanceStoreError.EMAIL_ERROR -> Event.SelectBalanceStoreOauthConfirmEmailError
        SelectBalanceStoreError.BALANCE_VALIDATIONS_EMAIL_SENDS_DISABLED -> Event.SelectBalanceStoreOauthConfirmEmailSendsDisabled
        SelectBalanceStoreError.BALANCE_VALIDATIONS_INSUFFICIENT_APPLICATION_LIMIT -> Event.SelectBalanceStoreOauthConfirmInsufficientApplicationLimit
        SelectBalanceStoreError.IDENTITY_NOT_VERIFIED -> Event.SelectBalanceStoreOauthConfirmIdentityNotVerified
        else -> Event.SelectBalanceStoreOauthConfirmUnknownError
    }
}

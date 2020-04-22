package com.aptopayments.sdk.core.data

import com.aptopayments.core.data.card.*
import com.aptopayments.core.data.config.*
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.core.data.oauth.OAuthAttemptStatus
import com.aptopayments.core.data.oauth.OAuthUserDataUpdate
import com.aptopayments.core.data.oauth.OAuthUserDataUpdateResult
import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.DataPointList
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.sdk.features.oauth.OAuthConfig
import org.mockito.Mockito
import java.net.URL

class TestDataProvider {

    companion object {
        fun provideOAuthAttempt() = OAuthAttempt(
                id = "",
                url = URL("http://www.google.es"),
                status = OAuthAttemptStatus.PENDING,
                userData = DataPointList(),
                tokenId = "",
                error = null,
                errorMessage = null
        )

        fun provideContextConfiguration() = ContextConfiguration(
                teamConfiguration = TeamConfiguration(
                        name = "",
                        logoUrl = ""),
                projectConfiguration = ProjectConfiguration(
                        name = "",
                        summary = "",
                        branding = Branding.createDefault(),
                        labels = hashMapOf(),
                        allowedCountries = arrayListOf(Country("US")),
                        supportEmailAddress = "",
                        trackerAccessToken = "",
                        isTrackerActive = false,
                        primaryAuthCredential = DataPoint.Type.PHONE)
        )

        fun provideProjectBranding() = Branding.createDefault()

        fun provideContextConfigurationEmail() = ContextConfiguration(
                teamConfiguration = TeamConfiguration(
                        name = "",
                        logoUrl = ""),
                projectConfiguration = ProjectConfiguration(
                        name = "",
                        summary = "",
                        branding = Branding.createDefault(),
                        labels = hashMapOf(),
                        allowedCountries = arrayListOf(Country("US")),
                        supportEmailAddress = "",
                        trackerAccessToken = "",
                        isTrackerActive = false,
                        primaryAuthCredential = DataPoint.Type.EMAIL)
        )

        fun provideOauthConfig() = OAuthConfig(
                title = "title",
                explanation = "explanation",
                callToAction = "callToAction",
                newUserAction = "newUserAction",
                allowedBalanceType = provideAllowedBalanceType(),
                assetUrl = null
        )

        fun provideAllowedBalanceType() = AllowedBalanceType(
                balanceType= "type",
                baseUri = URL("http://www.aptopayments.com"))

        fun provideOAuthUserData() = OAuthUserDataUpdate(
                result = OAuthUserDataUpdateResult.VALID,
                userData = DataPointList()
        )

        fun provideCardApplicationId() = "bestCardApplicationIdEver"

        fun provideVerification() = Verification("", "")

        fun <T> anyObject(): T = Mockito.any<T>()

        fun provideCard(
                accountID: String = "",
                cardProductID: String = "",
                cardNetwork: Card.CardNetwork = Card.CardNetwork.UNKNOWN,
                lastFourDigits: String = "",
                cardBrand: String = "",
                cardIssuer: String = "",
                state: Card.CardState = Card.CardState.UNKNOWN,
                isWaitlisted: Boolean? = false,
                cardStyle: CardStyle? = null,
                kycStatus: KycStatus? = KycStatus.UNKNOWN,
                kycReason: List<String>? = null,
                orderedStatus: Card.OrderedStatus = Card.OrderedStatus.UNKNOWN,
                spendableAmount: Money? = null,
                nativeSpendableAmount: Money? = null,
                cardHolder: String = "",
                features: Features? = null
        ) = Card(
                accountID = accountID,
                cardProductID = cardProductID,
                cardNetwork = cardNetwork,
                lastFourDigits = lastFourDigits,
                cardBrand = cardBrand,
                cardIssuer = cardIssuer,
                state = state,
                isWaitlisted = isWaitlisted,
                cardStyle = cardStyle,
                kycStatus = kycStatus,
                kycReason = kycReason,
                orderedStatus = orderedStatus,
                spendableAmount = spendableAmount,
                nativeSpendableAmount = nativeSpendableAmount,
                cardHolder = cardHolder,
                features = features
        )

        fun provideCategorySpendingList(): List<CategorySpending> {
            val spending1 = CategorySpending("glass", Money("USD", 130.0))
            val spending2 = CategorySpending("car", Money("USD", 80.0))
            val spending3 = CategorySpending("plane", Money("USD", 100.0))
            val list = listOf(spending1, spending2, spending3)
            return list
        }

        fun provideDefaultTheme() = DEFAULT_THEME

        fun provideCardId() = "CARD_ID"
    }
        
}

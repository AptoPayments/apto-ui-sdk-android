package com.aptopayments.sdk.core.data

import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.config.*
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.oauth.OAuthAttemptStatus
import com.aptopayments.mobile.data.oauth.OAuthUserDataUpdate
import com.aptopayments.mobile.data.oauth.OAuthUserDataUpdateResult
import com.aptopayments.mobile.data.stats.CategorySpending
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.DataPointList
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
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
            projectConfiguration = ProjectConfiguration(
                name = "",
                summary = "",
                branding = Branding.createDefault(),
                labels = hashMapOf(),
                allowedCountries = arrayListOf(Country("US")),
                supportEmailAddress = "",
                trackerAccessToken = "",
                isTrackerActive = false,
                primaryAuthCredential = DataPoint.Type.PHONE
            )
        )

        fun provideProjectBranding() = Branding.createDefault()

        fun provideContextConfigurationEmail() = ContextConfiguration(
            projectConfiguration = ProjectConfiguration(
                name = "",
                summary = "",
                branding = Branding.createDefault(),
                labels = hashMapOf(),
                allowedCountries = arrayListOf(Country("US")),
                supportEmailAddress = "",
                trackerAccessToken = "",
                isTrackerActive = false,
                primaryAuthCredential = DataPoint.Type.EMAIL
            )
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
            balanceType = "type",
            baseUri = URL("http://www.aptopayments.com")
        )

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
            return listOf(spending1, spending2, spending3)
        }

        fun provideCardId() = "CARD_ID"
    }
}

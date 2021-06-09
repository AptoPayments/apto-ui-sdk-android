package com.aptopayments.sdk.core.data

import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.Branding
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.oauth.OAuthAttemptStatus
import com.aptopayments.mobile.data.oauth.OAuthUserDataUpdate
import com.aptopayments.mobile.data.oauth.OAuthUserDataUpdateResult
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.paymentsources.NewCard
import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.stats.CategorySpending
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.DataPointList
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.sdk.features.oauth.OAuthConfig
import org.mockito.Mockito
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
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
            features: Features? = null,
            metadata: String? = null
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
            features = features,
            metadata = metadata
        )

        fun provideCardProduct(
            id: String = "",
            cardholderAgreement: Content? = null,
            privacyPolicy: Content? = null,
            termsAndConditions: Content? = null,
            faq: Content? = null,
            name: String = "",
            waitlistBackgroundImage: URL? = null,
            waitlistBackgroundColor: Int? = null,
            waitlistDarkBackgroundColor: Int? = null,
            waitlistAsset: URL? = null,
            exchangeRates: Content? = null,
        ) = CardProduct(
            id = id,
            cardholderAgreement = cardholderAgreement,
            privacyPolicy = privacyPolicy,
            termsAndConditions = termsAndConditions,
            faq = faq,
            name = name,
            waitlistBackgroundImage = waitlistBackgroundImage,
            waitlistBackgroundColor = waitlistBackgroundColor,
            waitlistDarkBackgroundColor = waitlistDarkBackgroundColor,
            waitlistAsset = waitlistAsset,
            exchangeRates = exchangeRates,
        )

        fun provideCategorySpendingList(): List<CategorySpending> {
            val spending1 = CategorySpending("glass", Money("USD", 130.0))
            val spending2 = CategorySpending("car", Money("USD", 80.0))
            val spending3 = CategorySpending("plane", Money("USD", 100.0))
            return listOf(spending1, spending2, spending3)
        }

        fun provideCardId() = "CARD_ID"

        fun provideVisaValidNumbers() = listOf("4242424242424242", "4000056655665556")

        fun provideVisaInValidNumbers() = listOf("4242424242424241")

        fun provideVisaInValidPatternNumbers() = listOf("5555555555554444")

        fun provideMasterValidNumbers() =
            listOf("5555555555554444", "2223003122003222", "5200828282828210", "5105105105105100")

        fun provideMasterInValidPatternNumbers() = listOf("5655555555554444", "4242424242424242", "6011111111111117")

        fun provideAmexValidNumbers() = listOf("378282246310005", "371449635398431", "340000000000009")

        fun provideAmexInValidPatternNumbers() = listOf("318282246310005", "2223003122003222")

        fun provideDiscoverValidNumbers() = listOf("6011111111111117", "6011000990139424", "6445644564456445")

        fun provideDiscoverInValidPatternNumbers() = listOf("6021111111111111", "378282246310005")

        fun provideNewCard() = NewCard(
            description = "desc",
            pan = "4242424242424242",
            cvv = "123",
            expirationMonth = "01",
            expirationYear = "25",
            zipCode = "12345"
        )

        fun providePaymentSourcesCard(id: String = "entity_12345") = com.aptopayments.mobile.data.paymentsources.Card(
            id = id,
            description = "desc",
            isPreferred = true,
            network = Card.CardNetwork.VISA,
            lastFour = "4242"
        )

        fun providePaymentSourcesPayment(id: String = "entity_12345", status: PaymentStatus = PaymentStatus.PROCESSED) = Payment(
            id = id,
            status = status,
            amount = Money("USD", 100.0),
            source = providePaymentSourcesCard(),
            approvalCode = "123456",
            createdAt = ZonedDateTime.of(2020, 10, 2, 15, 53, 0, 0, ZoneOffset.UTC)
        )

        fun provideMoney(amount: Double = 10.0, currency: String = "USD") = Money(currency, amount)

        fun provideTransaction(
            transactionId: String = "transaction_1234",
            createdAt: ZonedDateTime = ZonedDateTime.now()
        ) = Transaction(
            transactionId,
            transactionType = Transaction.TransactionType.PURCHASE,
            createdAt = createdAt,
            transactionDescription = null,
            lastMessage = null,
            declineCode = null,
            merchant = null,
            store = null,
            localAmount = provideMoney(),
            billingAmount = provideMoney(),
            holdAmount = provideMoney(0.0),
            cashbackAmount = provideMoney(0.0),
            feeAmount = provideMoney(0.0),
            nativeBalance = provideMoney(),
            settlement = null,
            ecommerce = null,
            international = null,
            cardPresent = null,
            emv = null,
            cardNetwork = Card.CardNetwork.VISA,
            state = Transaction.TransactionState.COMPLETE,
            adjustments = null,
            fundingSourceName = null
        )

        fun provideBalance(
            id: String = "",
            state: Balance.BalanceState? = null,
            type: String = "",
            fundingSourceType: String = "",
            balance: Money? = null,
            amountSpendable: Money? = null,
            amountHeld: Money? = null,
            custodianWallet: CustodianWallet? = null
        ) = Balance(
            id = id,
            state = state,
            type = type,
            fundingSourceType = fundingSourceType,
            balance = balance,
            amountSpendable = amountSpendable,
            amountHeld = amountHeld,
            custodianWallet = custodianWallet
        )

        fun monthlyStatement(
            id: String = "monthly_statement_12345",
            month: Int = 12,
            year: Int = 2021,
            downloadUrl: String = "https://www.aptopayments.com/statement.pdf",
            urlExpiration: ZonedDateTime? = ZonedDateTime.now().plusMinutes(1)
        ): MonthlyStatement {
            return MonthlyStatement(
                id = id,
                month = month,
                year = year,
                downloadUrl = downloadUrl,
                urlExpiration = urlExpiration
            )
        }
    }
}

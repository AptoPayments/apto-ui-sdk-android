package com.aptopayments.sdk.features.selectcountry

import androidx.annotation.VisibleForTesting
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.json.JSONObject
import org.koin.core.inject
import java.lang.reflect.Modifier

private const val COUNTRY_SELECTOR_TAG = "CountrySelectorFragment"

internal class CardProductSelectorFlow(
    val onBack: () -> Unit,
    val onFinish: (cardProductId: String) -> Unit
) : Flow(), CountrySelectorContract.Delegate {

    private val analyticsManager: AnalyticsServiceContract by inject()
    private val aptoPlatformProtocol: AptoPlatformProtocol by inject()

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    var countryCardProductMap: HashMap<String, ArrayList<String>?> = HashMap()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        aptoPlatformProtocol.fetchCardProducts { result ->
            result.either(
                { onInitComplete(Either.Left(it)) },
                { cardProductList ->
                    when (cardProductList.size) {
                        0 -> onInitComplete(Either.Left(CardProductSelectorInitFailure()))
                        1 -> onFinish(cardProductList[0].id)
                        else -> {
                            val allowedCountriesSet = mutableSetOf<Country>()
                            cardProductList.forEach { cardProduct ->
                                cardProduct.countries?.forEach { allowedCountry ->
                                    val cardProductArrayList = countryCardProductMap[allowedCountry] ?: ArrayList()
                                    cardProductArrayList.add(cardProduct.id)
                                    countryCardProductMap[allowedCountry] = cardProductArrayList
                                    allowedCountriesSet.add(Country(allowedCountry))
                                }
                            }
                            val fragment =
                                fragmentFactory.countrySelectorFragment(allowedCountriesSet.toList(), COUNTRY_SELECTOR_TAG)
                            fragment.delegate = this
                            analyticsManager.track(Event.CardProductSelectorCountrySelectorShown)
                            setStartElement(element = fragment as FlowPresentable)
                        }
                    }
                    onInitComplete(Either.Right(Unit))
                }
            )
        }
    }

    override fun restoreState() {
        (fragmentWithTag(COUNTRY_SELECTOR_TAG) as? CountrySelectorContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onCountrySelected(country: Country) {
        val possibleCardProducts = countryCardProductMap[country.isoCode]
        if (possibleCardProducts?.size == 1) {
            val cardProductId = possibleCardProducts[0]

            val properties = JSONObject()
            properties.put("cardProductId", cardProductId)
            properties.put("countryCode", country.isoCode)
            analyticsManager.track(Event.CardProductSelectorProductSelected, properties)
            onFinish(cardProductId)
        } else {
            // TODO: show the card product selector
        }
    }

    override fun onBackFromCountrySelector() {
        analyticsManager.track(Event.CardProductSelectorCountrySelectorClosed)
        onBack.invoke()
    }

    internal class CardProductSelectorInitFailure : Failure.FeatureFailure()
}

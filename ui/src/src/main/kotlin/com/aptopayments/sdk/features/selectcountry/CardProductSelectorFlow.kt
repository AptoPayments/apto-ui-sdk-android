package com.aptopayments.sdk.features.selectcountry

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import org.json.JSONObject
import java.lang.reflect.Modifier
import javax.inject.Inject

private const val COUNTRY_SELECTOR_TAG = "CountrySelectorFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardProductSelectorFlow (
        val onBack: (Unit) -> Unit,
        val onFinish: (cardProductId: String) -> Unit
) : Flow(), CountrySelectorContract.Delegate {

    @Inject lateinit var analyticsManager: AnalyticsServiceContract
    @Inject lateinit var aptoPlatformProtocol: AptoPlatformProtocol

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    var countryCardProductMap: HashMap<String, ArrayList<String>?> = HashMap()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        appComponent.inject(this)
        aptoPlatformProtocol.fetchCardProducts { result ->
            result.either({ onInitComplete(Either.Left(it)) }, { cardProductList ->
                when {
                    cardProductList.isEmpty() -> {
                        onInitComplete(Either.Left(CardProductSelectorInitFailure()))
                    }
                    cardProductList.size == 1 -> onFinish(cardProductList[0].id)
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
                        val fragment = fragmentFactory.countrySelectorFragment(
                                UIConfig.uiTheme, allowedCountriesSet.toList(), COUNTRY_SELECTOR_TAG)
                        fragment.delegate = this
                        analyticsManager.track(Event.CardProductSelectorCountrySelectorShown)
                        // TODO: show the card product selector if only 1 country
                        setStartElement(element = fragment as FlowPresentable)
                    }
                }
                onInitComplete(Either.Right(Unit))
            })
        }
    }

    override fun restoreState() {
        (fragmentWithTag(COUNTRY_SELECTOR_TAG) as? CountrySelectorContract.View)?.let {
            it.delegate = this
        }
    }

    override fun getCountrySelectorTitle(): String {
        rootActivity()?.let {
            return "select_card_product.select_country.title".localized(it)
        } ?: return ""
    }

    override fun getCountrySelectorDescription(): String {
        rootActivity()?.let {
            return "select_card_product.select_country.explanation".localized(it)
        } ?: return ""
    }

    override fun getCountrySelectorCallToAction(): String {
        rootActivity()?.let {
            return "select_card_product.select_country.call_to_action".localized(it)
        } ?: return ""
    }

    override fun onCountrySelected(country: Country) {
        val possibleCardProducts = countryCardProductMap[country.isoCode]
        if (possibleCardProducts?.size == 1) {
            val cardProductId = possibleCardProducts[0]
            val properties = JSONObject().put("cardProductId", cardProductId)
            properties.put("countryCode", country.isoCode)
            analyticsManager.track(Event.CardProductSelectorProductSelected, properties)
            onFinish(cardProductId)
        }
        else {
            // TODO: show the card product selector
        }
    }

    override fun onBackFromCountrySelector() {
        analyticsManager.track(Event.CardProductSelectorCountrySelectorClosed)
        onBack(Unit)
    }

    internal class CardProductSelectorInitFailure : Failure.FeatureFailure()
}

package com.aptopayments.sdk.features.inputdata.address

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.aptopayments.core.data.geo.Country
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.TimeUnit

internal class PlacesAutocompleteAdapter internal constructor(
    context: Context,
    private val placesClient: PlacesClient,
    private val countryList: List<Country>
) : ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1),
    Filterable {
    private var resultList: List<AutocompletePrediction> = listOf()

    override fun getCount() = resultList.size

    override fun getItem(position: Int) = resultList[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)
        val item = getItem(position)
        val textView1 = row.findViewById<TextView>(android.R.id.text1)
        val textView2 = row.findViewById<TextView>(android.R.id.text2)
        textView1.text = item.getPrimaryText(null)
        textView2.text = item.getSecondaryText(null)
        return row
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val results = FilterResults()
                var filterData: List<AutocompletePrediction> = emptyList()

                if (!charSequence.isNullOrEmpty()) {
                    filterData = getAutocomplete(charSequence)
                }
                results.values = filterData
                results.count = filterData.size
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, results: FilterResults) {
                if (results.count > 0) {
                    resultList = results.values as List<AutocompletePrediction>
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null)
                } else {
                    super.convertResultToString(resultValue)
                }
            }
        }
    }

    private fun getAutocomplete(constraint: CharSequence): List<AutocompletePrediction> {
        val requestBuilder = buildRequest(constraint)
        val results = placesClient.findAutocompletePredictions(requestBuilder.build())

        try {
            Tasks.await(results, 10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            // Nothing
        }
        return if (results.isSuccessful && results.result != null) {
            results.result!!.autocompletePredictions
        } else {
            emptyList()
        }
    }

    private fun buildRequest(constraint: CharSequence): FindAutocompletePredictionsRequest.Builder {
        return FindAutocompletePredictionsRequest.builder()
            .setQuery(constraint.toString())
            .setCountries(countryList.map { it.isoCode })
            .setSessionToken(AutocompleteSessionToken.newInstance())
            .setTypeFilter(TypeFilter.ADDRESS)
    }
}

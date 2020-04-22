package com.aptopayments.sdk.features.inputdata.address

import com.aptopayments.sdk.utils.CoroutineDispatcherProvider
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class PlaceFetcher(
    private val placesClient: PlacesClient,
    private val dispatchers: CoroutineDispatcherProvider
) {
    private val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS)

    suspend fun fetchPlace(placeID: String): Place? = withContext(dispatchers.io) {
        doFetchRequest(placeID)
    }

    private suspend fun doFetchRequest(placeID: String): Place? = suspendCoroutine { cont ->
        val request = FetchPlaceRequest.builder(placeID, placeFields).build()
        placesClient.fetchPlace(request).addOnSuccessListener { task ->
            cont.resume(task.place)
        }.addOnFailureListener {
            cont.resume(null)
        }
    }
}

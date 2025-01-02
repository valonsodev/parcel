package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import okhttp3.Request
import okio.IOException

private const val API_URL = "https://api-test.dhl.com/track/shipments"
// TODO: find a good way to store secrets outside of Git
private const val API_KEY = "demo-key"

private val dhlJsonAdapter = api_moshi.adapter(DHLResponse::class.java)

fun getDHLParcel(trackingNumber: String): Parcel? {
    val request = Request.Builder()
        .url("$API_URL?trackingNumber=$trackingNumber")
        .header("DHL-API-Key", API_KEY)
        .build()
    api_client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val resp = dhlJsonAdapter.fromJson(response.body!!.source())
        val shipment = resp?.shipments?.get(0)

        if (shipment != null) {
            // TODO: parse parcel history
            val parcel = Parcel(shipment.id, emptyList(), "")
            return parcel
        }
    }
    return null
}

@JsonClass(generateAdapter = true)
internal data class DHLShipment(
    val id: String,
    val service: String,
)

@JsonClass(generateAdapter = true)
internal data class DHLResponse(
    val shipments: List<DHLShipment>,
)

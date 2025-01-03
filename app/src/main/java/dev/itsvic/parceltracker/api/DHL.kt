package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import okhttp3.Request
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            val status = when (shipment.status.statusCode) {
                "unknown" -> Status.Unknown
                "pre-transit" -> Status.Preadvice
                "transit" -> when (shipment.status.status) {
                    // DHL uses a "transit" status code for this. stupid
                    "OUT FOR DELIVERY" -> Status.OutForDelivery
                    else -> Status.InTransit
                }

                "delivered" -> Status.Delivered
                else -> Status.Unknown
            }

            val history = shipment.events.map {
                ParcelHistoryItem(
                    it.description ?: it.status,
                    LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_DATE_TIME),
                    if (it.location == null) "Unknown location" else "${it.location.address.postalCode} ${it.location.address.addressLocality}"
                )
            }

            val parcel = Parcel(shipment.id, history, status)
            return parcel
        }
    }
    return null
}

@JsonClass(generateAdapter = true)
internal data class DHLResponse(
    val shipments: List<DHLShipment>,
)

@JsonClass(generateAdapter = true)
internal data class DHLShipment(
    val id: String,
    val service: String,
    val events: List<DHLEvent>,
    val status: DHLEvent,
)

@JsonClass(generateAdapter = true)
internal data class DHLEvent(
    val description: String?,
    val location: DHLEventLocation?,
    val status: String,
    val statusCode: String,
    val timestamp: String,
)

@JsonClass(generateAdapter = true)
internal data class DHLEventLocation(
    val address: DHLAddress,
)

@JsonClass(generateAdapter = true)
internal data class DHLAddress(
    val addressLocality: String,
    val countryCode: String,
    val postalCode: String,
)

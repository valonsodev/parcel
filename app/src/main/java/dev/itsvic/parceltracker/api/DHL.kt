package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import okhttp3.Request
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val API_URL = "https://api-eu.dhl.com/track/shipments"

// please don't take it for yourself, make one instead for free - https://developer.dhl.com/
private const val API_KEY = "VzYp08GaeA2esfeCPBLtzHkLShfRTk28"

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
                    // DHL uses a "transit" status code for different statues
                    "447" -> Status.Customs // ARRIVED AT CUSTOMS
                    "506" -> Status.Customs // HELD AT CUSTOMS
                    "449" -> Status.Customs // CLEARED CUSTOMS
                    "576" -> Status.InWarehouse // PROCESSED AT LOCAL DISTRIBUTION CENTER
                    "577" -> Status.OutForDelivery // DEPARTED FROM LOCAL DISTRIBUTION CENTER
                    "OUT FOR DELIVERY" -> Status.OutForDelivery
                    else -> Status.InTransit
                }
                "failure" -> Status.DeliveryFailure
                "delivered" -> Status.Delivered
                else -> Status.Unknown
            }

            val history = shipment.events.map {
                ParcelHistoryItem(
                    it.description ?: it.status,
                    LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_DATE_TIME),
                    if (it.location == null) "Unknown location"
                    else
                        if (it.location.address.postalCode != null)
                            "${it.location.address.postalCode} ${it.location.address.addressLocality}"
                        else it.location.address.addressLocality
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
    val postalCode: String?,
)

package dev.itsvic.parceltracker.api

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import java.time.LocalDateTime

val api_client = OkHttpClient()
val api_moshi: Moshi = Moshi.Builder().build()

// TODO: fill out with more data
data class Parcel(
    val id: String,
    val history: List<ParcelHistoryItem>,
    val currentStatus: Status,
)

data class ParcelHistoryItem(
    val description: String,
    val time: LocalDateTime,
    val location: String,
)

enum class Service {
    UNDEFINED,
    DHL,
    GLS,
    POLISH_POST,
    EXAMPLE,
}

val serviceToHumanString = mapOf(
    Service.UNDEFINED to "Undefined",
    Service.GLS to "GLS",
    Service.DHL to "DHL",
    Service.POLISH_POST to "Poczta Polska",
    Service.EXAMPLE to "Example Post"
)

enum class Status {
    Preadvice,
    InTransit,
    InWarehouse,
    Customs,
    OutForDelivery,
    DeliveryFailure,
    Delivered,
    AwaitingPickup,
    PickedUp,
    Unknown,
}

val statusToHumanString = mapOf(
    Status.Preadvice to "Preadvice",
    Status.InTransit to "In transit",
    Status.InWarehouse to "Arrived at warehouse",
    Status.Customs to "Arrived at customs",
    Status.OutForDelivery to "Out for delivery",
    Status.DeliveryFailure to "Failed to deliver",
    Status.Delivered to "Delivered",
    Status.AwaitingPickup to "Awaiting pickup",
    Status.PickedUp to "Picked up",
    Status.Unknown to "Unknown status code"
)

val serviceOptions = listOf(
    // Service.DHL, // unfinished
    Service.GLS,
    Service.POLISH_POST,
)

fun getParcel(id: String, postCode: String?, service: Service): Parcel? {
    return when (service) {
        Service.DHL -> getDHLParcel(id)
        Service.GLS -> getGLSParcel(id, postCode)
        Service.POLISH_POST -> getPolishPostParcel(id)
        else -> throw NotImplementedError("Service $service has no fetch implementation yet")
    }
}

package dev.itsvic.parceltracker.api

import com.squareup.moshi.Moshi
import dev.itsvic.parceltracker.R
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
    Service.UNDEFINED to R.string.service_undefined,
    Service.GLS to R.string.service_gls,
    Service.DHL to R.string.service_dhl,
    Service.POLISH_POST to R.string.service_polish_post,
    Service.EXAMPLE to R.string.service_example
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
    NetworkFailure,
}

val statusToHumanString = mapOf(
    Status.Preadvice to R.string.status_preadvice,
    Status.InTransit to R.string.status_in_transit,
    Status.InWarehouse to R.string.status_in_warehouse,
    Status.Customs to R.string.status_customs,
    Status.OutForDelivery to R.string.status_out_for_delivery,
    Status.DeliveryFailure to R.string.status_delivery_failure,
    Status.Delivered to R.string.status_delivered,
    Status.AwaitingPickup to R.string.status_awaiting_pickup,
    Status.PickedUp to R.string.status_picked_up,
    Status.Unknown to R.string.status_unknown,
    Status.NetworkFailure to R.string.status_network_failure,
)

val serviceOptions = listOf(
    Service.DHL,
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

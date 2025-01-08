package dev.itsvic.parceltracker.api

import android.util.Log
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
    EVRI,
    DPD_UK,
    PACKETA,

    EXAMPLE,
}

val serviceOptions = listOf(
    Service.DHL,
    Service.GLS,
    Service.POLISH_POST,
    Service.EVRI,
    Service.DPD_UK,
    Service.PACKETA,
)

val serviceToHumanString = mapOf(
    Service.UNDEFINED to R.string.service_undefined,
    Service.GLS to R.string.service_gls,
    Service.DHL to R.string.service_dhl,
    Service.POLISH_POST to R.string.service_polish_post,
    Service.EVRI to R.string.service_evri,
    Service.DPD_UK to R.string.service_dpd_uk,
    Service.PACKETA to R.string.service_packeta,

    Service.EXAMPLE to R.string.service_example,
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
    NoData,
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
    Status.NoData to R.string.status_no_data,
)

suspend fun getParcel(id: String, postCode: String?, service: Service): Parcel {
    return when (service) {
        Service.DHL -> getDHLParcel(id)
        Service.GLS -> getGLSParcel(id, postCode)
        Service.POLISH_POST -> getPolishPostParcel(id)
        Service.EVRI -> getEvriParcel(id)
        Service.DPD_UK -> getDpdUkParcel(id, postCode)
        Service.PACKETA -> getPacketaParcel(id)

        // to be used only in demo mode.
        Service.EXAMPLE -> getExampleParcel(id)
        else -> throw NotImplementedError("Service $service has no fetch implementation yet")
    }
}

class ParcelNonExistentException: Exception("Parcel does not exist in delivery service API")

internal fun logUnknownStatus(service: String, data: String): Status {
    Log.d("APICore", "Unknown status reported by $service: $data")
    return Status.Unknown
}

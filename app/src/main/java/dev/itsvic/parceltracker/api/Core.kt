package dev.itsvic.parceltracker.api

import android.util.Log
import com.squareup.moshi.Moshi
import dev.itsvic.parceltracker.BuildConfig
import dev.itsvic.parceltracker.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.LocalDateTime

internal val api_client = OkHttpClient.Builder()
    .addInterceptor(
        HttpLoggingInterceptor {
            Log.d("OkHttp", it)
        }.setLevel(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.BASIC
        )
    )
    .build()
internal val api_moshi: Moshi = Moshi.Builder().build()
internal val api_factory = MoshiConverterFactory.create(api_moshi)

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
    SAMEDAY_BG,
    SAMEDAY_HU,
    SAMEDAY_RO,

    EXAMPLE,
}

val serviceOptions = listOf(
    Service.DHL,
    Service.GLS,

    Service.DPD_UK,
    Service.EVRI,

    Service.PACKETA,
    Service.POLISH_POST,
    Service.SAMEDAY_BG,
    Service.SAMEDAY_HU,
    Service.SAMEDAY_RO,
)

private val serviceToHumanString = mapOf(
    Service.UNDEFINED to R.string.service_undefined,
    Service.DHL to R.string.service_dhl,
    Service.POLISH_POST to R.string.service_polish_post,
    Service.EVRI to R.string.service_evri,
    Service.DPD_UK to R.string.service_dpd_uk,
    Service.PACKETA to R.string.service_packeta,

    Service.SAMEDAY_BG to R.string.service_sameday_bg,
    Service.SAMEDAY_HU to R.string.service_sameday_hu,
    Service.SAMEDAY_RO to R.string.service_sameday_ro,
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
    // use DeliveryService abstraction if possible, otherwise default to the old hardcoded list
    getDeliveryService(service)?.let {
        return it.getParcel(id, postCode)
    }

    return when (service) {
        Service.DHL -> getDHLParcel(id)
        Service.POLISH_POST -> getPolishPostParcel(id)
        Service.EVRI -> getEvriParcel(id)
        Service.DPD_UK -> getDpdUkParcel(id, postCode)
        Service.PACKETA -> getPacketaParcel(id)
        Service.SAMEDAY_BG -> getSamedayParcel("bg", id)
        Service.SAMEDAY_HU -> getSamedayParcel("hu", id)
        Service.SAMEDAY_RO -> getSamedayParcel("ro", id)

        else -> throw NotImplementedError("Service $service has no fetch implementation yet")
    }
}

fun getDeliveryService(service: Service): DeliveryService? {
    return when (service) {
        Service.GLS -> GLSDeliveryService
        Service.EXAMPLE -> ExampleDeliveryService
        else -> null
    }
}

fun getDeliveryServiceName(service: Service): Int? {
    // get DeliveryService abstraction. if missing, use the old hardcoded list
    getDeliveryService(service)?.let { return it.nameResource }
    return serviceToHumanString[service]
}

interface DeliveryService {
    val nameResource: Int
    suspend fun getParcel(trackingId: String, postalCode: String?): Parcel
}

class ParcelNonExistentException : Exception("Parcel does not exist in delivery service API")

internal fun logUnknownStatus(service: String, data: String): Status {
    Log.d("APICore", "Unknown status reported by $service: $data")
    return Status.Unknown
}

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
    Service.DHL to R.string.service_dhl,
    Service.POLISH_POST to R.string.service_polish_post,
)

enum class Status(val nameResource: Int) {
    Preadvice(R.string.status_preadvice),
    InTransit(R.string.status_in_transit),
    InWarehouse(R.string.status_in_warehouse),
    Customs(R.string.status_customs),
    OutForDelivery(R.string.status_out_for_delivery),
    DeliveryFailure(R.string.status_delivery_failure),
    Delivered(R.string.status_delivered),
    AwaitingPickup(R.string.status_awaiting_pickup),
    PickedUp(R.string.status_picked_up),
    Unknown(R.string.status_unknown),
    NetworkFailure(R.string.status_network_failure),
    NoData(R.string.status_no_data),
}

suspend fun getParcel(id: String, postCode: String?, service: Service): Parcel {
    // use DeliveryService abstraction if possible, otherwise default to the old hardcoded list
    getDeliveryService(service)?.let {
        return it.getParcel(id, postCode)
    }

    return when (service) {
        Service.DHL -> getDHLParcel(id)
        Service.POLISH_POST -> getPolishPostParcel(id)

        else -> throw NotImplementedError("Service $service has no fetch implementation yet")
    }
}

fun getDeliveryService(service: Service): DeliveryService? {
    return when (service) {
        Service.DPD_UK -> DpdUkDeliveryService
        Service.EVRI -> EvriDeliveryService
        Service.GLS -> GLSDeliveryService
        Service.PACKETA -> PacketaDeliveryService
        Service.SAMEDAY_BG -> SamedayBulgariaDeliveryService
        Service.SAMEDAY_HU -> SamedayHungaryDeliveryService
        Service.SAMEDAY_RO -> SamedayRomaniaDeliveryService

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

package dev.itsvic.parceltracker.api

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient

val api_client = OkHttpClient()
val api_moshi: Moshi = Moshi.Builder().build()

// TODO: fill out with more data
data class Parcel(
    val id: String,
    val history: List<ParcelHistoryItem>,
    val currentStatus: String,
)

data class ParcelHistoryItem(
    val description: String,
    val time: String,
    val location: String,
)

enum class Service {
    DHL,
    GLS,

    EXAMPLE,
}

fun getParcel(id: String, postCode: String?, service: Service): Parcel? {
    return when (service) {
        Service.DHL -> getDHLParcel(id)
        Service.GLS -> getGLSParcel(id, postCode)
        else -> throw NotImplementedError("Service $service has no fetch implementation yet")
    }
}

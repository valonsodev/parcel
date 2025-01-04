package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import okhttp3.Request
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Reverse-engineered from their private API. Pretty basic at least

// rstt029 - no post code, basic information
// rstt028 - post code, full information

// TODO: should respect system language
private const val API_BASE = "https://gls-group.com/app/service/open/rest/EN/en"
private val glsJsonAdapter = api_moshi.adapter(GLSResponse::class.java)

internal fun getGLSParcel(id: String, postalCode: String?): Parcel {
    // just throw an IOException for now until we implement the rstt029 API
    if (postalCode == null) throw IOException("Needs post codes for now")

    val request = Request.Builder()
        .url("$API_BASE/rstt028/$id?postalCode=$postalCode")
        .build()

    api_client.newCall(request).execute().use { response ->
        if (response.code == 404) throw ParcelNonExistentException()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val resp = glsJsonAdapter.fromJson(response.body!!.source())

        if (resp != null) {
            val history = resp.history.map { item ->
                ParcelHistoryItem(
                    item.evtDscr,
                    LocalDateTime.parse("${item.date}T${item.time}", DateTimeFormatter.ISO_DATE_TIME),
                    if (item.address.city != "") "${item.address.city}, ${item.address.countryName}" else item.address.countryName
                )
            }
            val parcel = Parcel(id, history, when (resp.progressBar.statusInfo) {
                "PREADVICE" -> Status.Preadvice
                "INTRANSIT" -> Status.InTransit
                "INWAREHOUSE" -> Status.InWarehouse
                "INDELIVERY" -> Status.OutForDelivery
                "DELIVERED" -> Status.Delivered
                else -> Status.Unknown
            })
            return parcel
        }
    }

    throw ParcelNonExistentException()
}

@JsonClass(generateAdapter = true)
internal data class GLSResponse(
    val history: List<GLSHistoryItem>,
    val progressBar: GLSProgress,
)

@JsonClass(generateAdapter = true)
internal data class GLSHistoryItem(
    val time: String,
    val date: String,
    val evtDscr: String,
    val address: GLSHistoryAddress,
)

@JsonClass(generateAdapter = true)
internal data class GLSHistoryAddress(
    val city: String,
    val countryName: String,
    val countryCode: String,
)

@JsonClass(generateAdapter = true)
internal data class GLSProgress(
    val statusInfo: String,
)


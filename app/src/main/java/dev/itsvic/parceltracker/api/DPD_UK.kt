package dev.itsvic.parceltracker.api

import android.util.Log
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val retrofit = Retrofit.Builder()
    .baseUrl("https://apis.track.dpd.co.uk/v1/")
    .client(api_client)
    .addConverterFactory(MoshiConverterFactory.create(api_moshi))
    .build()

private val service = retrofit.create(DpdUkAPI::class.java)

internal suspend fun getDpdUkParcel(trackingId: String, postcode: String?): Parcel {
    val urlResp = service.getParcelURL(trackingId, postcode)

    if (urlResp.code() == 404) throw ParcelNonExistentException()
    Log.d("DpdUk", "$urlResp")
//    val id = urlResp.headers()["Location"]!!.substring(32)
    val id = urlResp.raw().request.url.pathSegments.last()
    Log.d("DpdUk", "id=$id")

    val events = service.getParcelEvents(id)

    val history = events.data.map {
        ParcelHistoryItem(
            it.eventText,
            LocalDateTime.parse(
                it.eventDate.replace(' ', 'T'),
                DateTimeFormatter.ISO_DATE_TIME
            ),
            it.eventLocation,
        )
    }

    val status = when (val statusCode = events.data.first().eventCode) {
        "000" -> Status.Preadvice
        "009" -> Status.InTransit
        "004" -> Status.InWarehouse
        "015" -> Status.OutForDelivery
        "001" -> Status.Delivered
        else -> logUnknownStatus("DPD UK", statusCode)
    }

    return Parcel(trackingId, history, status)
}

private interface DpdUkAPI {
    @GET("track")
    suspend fun getParcelURL(
        @Query("parcel") trackingId: String,
        @Query("postcode") postcode: String?
    ): Response<ResponseBody>

    // currently unused by us
    @GET("parcels/{id}")
    suspend fun getParcelInfo(@Path("id") id: String)

    @GET("parcels/{id}/parcelevents")
    suspend fun getParcelEvents(@Path("id") id: String): DpdUkParcelEventsResponse
}

@JsonClass(generateAdapter = true)
internal data class DpdUkParcelEventsResponse(
    val data: List<DpdUkEvent>
)

@JsonClass(generateAdapter = true)
internal data class DpdUkEvent(
    val eventCode: String,
    val eventDate: String,
    val eventText: String,
    val eventLocation: String,
)

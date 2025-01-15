package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.ZoneId
import java.time.ZonedDateTime

private val retrofit_builder = Retrofit.Builder()
    .client(api_client)
    .addConverterFactory(MoshiConverterFactory.create(api_moshi))

internal suspend fun getSamedayParcel(region: String, trackingId: String): Parcel {
    val retrofit = retrofit_builder.baseUrl("https://api.sameday.${region}/api/public/").build()
    val service = retrofit.create(SamedayAPI::class.java)

    val resp = try {
        service.getAwbHistory(trackingId)
    } catch (_: HttpException) {
        throw ParcelNonExistentException()
    }

    val history = resp.awbHistory.map {
        ParcelHistoryItem(
            it.status,
            // i'm sure there's a more concise way of doing this
            ZonedDateTime.parse(it.statusDate).toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
            if (it.transitLocation.isNotBlank())
                "${it.transitLocation}, ${it.county}, ${it.country}"
            else if (it.county.isNotBlank())
                "${it.county}, ${it.country}"
            else if (it.country.isNotBlank())
                it.country
            else ""
        )
    }

    val status = when (val id = resp.awbHistory.first().statusStateId) {
        1 -> Status.Preadvice
        2 -> Status.InTransit
        7 -> Status.InTransit
        3 -> Status.InWarehouse
        4 -> Status.OutForDelivery
        18 -> Status.AwaitingPickup
        5 -> Status.Delivered
        else -> logUnknownStatus("Sameday", id.toString())
    }

    return Parcel(trackingId, history, status)
}

private interface SamedayAPI {
    @GET("awb/{id}/awb-history")
    suspend fun getAwbHistory(@Path("id") id: String): SamedayHistoryResponse
}

@JsonClass(generateAdapter = true)
internal data class SamedayHistoryResponse(
    val awbHistory: List<SamedayHistoryItem>,
)

@JsonClass(generateAdapter = true)
internal data class SamedayHistoryItem(
    val country: String,
    val county: String,
    val status: String,
    val statusDate: String,
    val statusStateId: Int,
    val transitLocation: String,
)

package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant
import java.time.ZoneId

private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.hermesworld.co.uk/enterprise-tracking-api/v1/")
    .client(api_client)
    .addConverterFactory(MoshiConverterFactory.create(api_moshi))
    .build()

private val service = retrofit.create(EvriAPI::class.java)

internal suspend fun getEvriParcel(trackingId: String): Parcel {
    val urnResp = try {
        val resp = service.getParcelURN(trackingId)
        if (resp.parcelIdentifiers.isEmpty())
            throw ParcelNonExistentException()

        resp
    } catch (_: HttpException) {
        throw ParcelNonExistentException()
    }

    val urn = urnResp.parcelIdentifiers.first().urn
    val parcelResp = service.getParcel(urn)
    val parcel = parcelResp.results.first()

    val history = parcel.trackingEvents.map {
        ParcelHistoryItem(
            it.trackingPoint.description,
            Instant.parse(it.dateTime).atZone(ZoneId.systemDefault()).toLocalDateTime(),
            ""
        )
    }

    val status = when (parcel.trackingEvents.first().trackingStage.trackingStageCode) {
        "1" -> Status.Preadvice
        "2" -> Status.InWarehouse
        "3" -> Status.InTransit
        "4_COURIER" -> Status.OutForDelivery
        "5_COURIER" -> Status.Delivered
        else -> logUnknownStatus("Evri", parcel.trackingEvents.first().trackingStage.trackingStageCode)
    }

    return Parcel(trackingId, history, status)
}

// TODO: there may be more info with a postcode, but i can't really test that
private interface EvriAPI {
    @Headers("Apikey: 0DExZiK9in2ihGce7cDPrnpQ4s4nIpWG")
    @GET("parcels/reference/{id}")
    suspend fun getParcelURN(@Path("id") trackingId: String): EvriParcelReferenceResponse

    @Headers("Apikey: Vi8HZURvXHANfpiFDGta6bJclafLJcAY")
    @GET("parcels")
    suspend fun getParcel(@Query("uniqueIds") urn: String): EvriParcelResponse
}

@JsonClass(generateAdapter = true)
internal data class EvriParcelReferenceResponse(
    val parcelIdentifiers: List<EvriParcelIdentifier>,
)

@JsonClass(generateAdapter = true)
internal data class EvriParcelIdentifier(
    val urn: String,
)

@JsonClass(generateAdapter = true)
internal data class EvriParcelResponse(
    val failures: List<EvriFailedQueries>,
    val results: List<EvriParcel>,
)

@JsonClass(generateAdapter = true)
internal data class EvriFailedQueries(
    val uniqueId: String,
)

@JsonClass(generateAdapter = true)
internal data class EvriParcel(
    val trackingEvents: List<EvriTrackingEvent>,
)

@JsonClass(generateAdapter = true)
internal data class EvriTrackingEvent(
    val trackingPoint: EvriTrackingPoint,
    val trackingStage: EvriTrackingStage,
    val dateTime: String,
)

@JsonClass(generateAdapter = true)
internal data class EvriTrackingPoint(
    val description: String,
)

@JsonClass(generateAdapter = true)
internal data class EvriTrackingStage(
    val trackingStageCode: String,
)

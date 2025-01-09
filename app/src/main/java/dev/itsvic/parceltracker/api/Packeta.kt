package dev.itsvic.parceltracker.api

import android.os.LocaleList
import com.squareup.moshi.JsonClass
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.POST
import retrofit2.http.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// reverse engineered from their nuxt app lol

private val retrofit = Retrofit.Builder()
    .baseUrl("https://tracking.app.packeta.com/api/")
    .client(api_client)
    .addConverterFactory(MoshiConverterFactory.create(api_moshi))
    .build()

private val service = retrofit.create(PacketaAPI::class.java)

internal suspend fun getPacketaParcel(trackingId: String): Parcel {
    val cleanId = trackingId.replace(Regex("\\s"), "").lowercase().replace("z", "")
    val locale = LocaleList.getDefault().get(0)

    val resp = try {
        service.getPacketById(cleanId, locale.language)
    } catch (e: HttpException) {
        if (e.code() == 400 || e.code() == 404)
            throw ParcelNonExistentException()
        else
            throw IOException()
    }

    val history = resp.item.trackingDetails.reversed().map {
        val locationRegex = Regex("(.*\\.)(?: (.*))?")
        val match = locationRegex.find(it.text)!!.destructured.toList()
        ParcelHistoryItem(
            match[0],
            LocalDateTime.parse(
                it.time.replace(' ', 'T'),
                DateTimeFormatter.ISO_DATE_TIME
            ),
            // location is optional, might not always exist
            if (match.size == 2) match[1] else ""
        )
    }

    val status = when (resp.item.packetStatusId) {
        // guesswork from the enum found in packeta web tracker code
        "1" -> Status.InWarehouse // WAITING_FOR_DELIVERY
        "2" -> Status.AwaitingPickup // READY_FOR_PICKUP
        "3" -> Status.PickedUp // ISSUED_AND_ACCOUNTED
        "21" -> Status.Unknown // LOST_OR_UNKNOWN
        "31" -> Status.InTransit // ON_THE_WAY
        "997" -> Status.InTransit // TO_BE_PROCESSED
        else -> logUnknownStatus("Packeta", resp.item.packetStatusId)
    }

    return Parcel(resp.item.barcode, history, status)
}

private interface PacketaAPI {
    @POST("getPacketById/{id}/{locale}")
    suspend fun getPacketById(
        @Path("id") id: String,
        @Path("locale") locale: String
    ): PacketaPacketResponse
}

@JsonClass(generateAdapter = true)
internal data class PacketaPacketResponse(
    val item: PacketaItem,
)

@JsonClass(generateAdapter = true)
internal data class PacketaItem(
    val barcode: String,
    val packetStatusId: String,
    val trackingDetails: List<PacketaHistoryItem>,
)

@JsonClass(generateAdapter = true)
internal data class PacketaHistoryItem(
    val text: String,
    val time: String,
)

package dev.itsvic.parceltracker.api

import android.os.LocaleList
import android.text.Html
import android.util.Log
import com.squareup.moshi.JsonClass
import dev.itsvic.parceltracker.R
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Reverse-engineered from their private API. Pretty basic at least

object GLSDeliveryService : DeliveryService {
    override val nameResource: Int = R.string.service_gls
    override val acceptsPostCode: Boolean = true
    override val requiresPostCode: Boolean = true

    override suspend fun getParcel(trackingId: String, postalCode: String?): Parcel {
        val locale = LocaleList.getDefault().get(0).language

        val resp = try {
            service.getExtendedParcel(
                id = trackingId,
                postalCode = postalCode!!,
                locale = locale
            )
        } catch (e: Exception) {
            Log.d("GLSDeliveryService", "exception", e)
            throw ParcelNonExistentException()
        }

        val history = resp.history.map { item ->
            ParcelHistoryItem(
                Html.fromHtml(item.evtDscr, Html.FROM_HTML_MODE_LEGACY).toString(),
                LocalDateTime.parse("${item.date}T${item.time}", DateTimeFormatter.ISO_DATE_TIME),
                if (item.address.city != "")
                    "${item.address.city}, ${item.address.countryName}"
                else item.address.countryName
            )
        }

        val status = when (resp.progressBar.statusInfo) {
            "PREADVICE" -> Status.Preadvice
            "INTRANSIT" -> Status.InTransit
            "INWAREHOUSE" -> Status.InWarehouse
            "INDELIVERY" -> Status.OutForDelivery
            "DELIVERED" -> Status.Delivered
            else -> logUnknownStatus("GLS", resp.progressBar.statusInfo)
        }

        val parcel = Parcel(trackingId, history, status)
        return parcel
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gls-group.com/app/service/open/rest/")
        .client(api_client)
        .addConverterFactory(api_factory)
        .build()
    private val service = retrofit.create(API::class.java)

    private interface API {
        @GET("EU/{locale}/rstt028/{id}")
        suspend fun getExtendedParcel(
            @Path("locale") locale: String,
            @Path("id") id: String,
            @Query("postalCode") postalCode: String,
        ): ExtendedParcelInfo
    }

    @JsonClass(generateAdapter = true)
    internal data class ExtendedParcelInfo(
        val history: List<GLSHistoryItem>,
        val progressBar: Progress,
    )

    @JsonClass(generateAdapter = true)
    internal data class GLSHistoryItem(
        val time: String,
        val date: String,
        val evtDscr: String,
        val address: HistoryAddress,
    )

    @JsonClass(generateAdapter = true)
    internal data class HistoryAddress(
        val city: String,
        val countryName: String,
        val countryCode: String,
    )

    @JsonClass(generateAdapter = true)
    internal data class Progress(
        val statusInfo: String,
    )
}

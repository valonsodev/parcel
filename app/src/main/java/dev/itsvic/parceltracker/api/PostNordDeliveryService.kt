package dev.itsvic.parceltracker.api

import android.os.LocaleList
import com.squareup.moshi.JsonClass
import dev.itsvic.parceltracker.R
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Made by following the API documentation.
object PostNordDeliveryService : DeliveryService {
    override val nameResource: Int = R.string.service_postnord
    override val acceptsPostCode: Boolean = false
    override val requiresPostCode: Boolean = false

    // Define supported locales and default
    private const val DEFAULT_LOCALE = "en"
    private val SUPPORTED_LOCALES = setOf("en", "sv", "no", "da", "fi")

    override suspend fun getParcel(trackingId: String, postCode: String?): Parcel {
        val locale = LocaleList.getDefault().get(0).language
        val apiLocale = if (SUPPORTED_LOCALES.contains(locale)) {
            locale
        } else {
            DEFAULT_LOCALE
        }

        val resp = try {
            service.getShipments(trackingId, apiLocale)
        } catch (_: HttpException) {
            throw ParcelNonExistentException()
        }

        val status = when (resp.items.first().status.code) {
            "EN_ROUTE" -> Status.InTransit
            "AVAILABLE_FOR_DELIVERY" -> Status.InWarehouse
            "DELIVERED" -> Status.Delivered
            "DELIVERY_IMPOSSIBLE" -> Status.DeliveryFailure
            "OTHER" -> Status.Unknown
            else -> logUnknownStatus("Postnord", resp.items.first().status.code)
        }

        val history = resp.items.first().events.map {
            ParcelHistoryItem(it.eventDescription, LocalDateTime.parse(it.eventTime, DateTimeFormatter.ISO_DATE_TIME), if (it.location.name != null) "${it.location.name}, ${it.location.countryCode}" else it.location.countryCode)
        }

        return Parcel(resp.shipmentId, history, status)
    }

    private val retrofit = Retrofit
        .Builder()
        .baseUrl("https://api2.postnord.com/rest/shipment/v1/trackingweb/")
        .client(api_client)
        .addConverterFactory(api_factory)
        .build()

    private val service = retrofit.create(API::class.java)

    private interface API {
        @GET("shipmentInformation")
        @Headers("x-bap-key: web-tracking-sc")
        suspend fun getShipments(
            @Query("shipmentId") id: String,
            @Query("locale") locale: String
        ): ShipmentResponse
    }

    @JsonClass(generateAdapter = true)
    internal data class ShipmentResponse(
        val shipmentId: String,
        val items: List<Item>
    )

    @JsonClass(generateAdapter = true)
    internal data class Item(
        val itemId: String,
        val deliveryInformation: DeliveryInformation,
        val events: List<Event>,
        val status: PNStatus
    )

    @JsonClass(generateAdapter = true)
    internal data class DeliveryInformation(
        val deliveryTo: String,
        val deliveryToInfo: String,
    )

    @JsonClass(generateAdapter = true)
    internal data class Event(
        val eventDescription: String,
        val eventTime: String, // ISO-8601 representation of the datetime
        val status: String,
        val location: Location
    )

    @JsonClass(generateAdapter = true)
    internal data class Location(
        val countryCode: String,
        val locationType: String,
        val name: String?
    )

    @JsonClass(generateAdapter = true)
    internal data class PNStatus(
        val code: String,
        val header: String,
        val description: String
    )
}
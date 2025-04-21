package dev.itsvic.parceltracker.api

import android.os.LocaleList
import com.squareup.moshi.JsonClass
import dev.itsvic.parceltracker.R
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.time.ZoneId
import java.time.ZonedDateTime

// Reverse-engineered from https://www.postnord.com/track-and-trace/
// And additional information about locales from https://developer.postnord.com/apis/details?systemName=shipment-v5-trackandtrace-shipmentinformation
object PostNordDeliveryService : DeliveryService {
    override val nameResource: Int = R.string.service_postnord
    override val acceptsPostCode: Boolean = false
    override val requiresPostCode: Boolean = false

    // Define supported locales and default
    private const val DEFAULT_LOCALE = "en"
    private val supportedLocales = setOf("en", "sv", "no", "da", "fi")

    override suspend fun getParcel(trackingId: String, postCode: String?): Parcel {
        val locale = LocaleList.getDefault().get(0).language
        val apiLocale = if (supportedLocales.contains(locale)) {
            locale
        } else {
            DEFAULT_LOCALE
        }

        val resp = try {
            service.getShipments(trackingId, apiLocale)
        } catch (_: HttpException) {
            throw ParcelNonExistentException()
        }

        val item = resp.items.first()

        val status = when (item.status.code) {
            "CREATED" -> Status.Preadvice
            "EN_ROUTE", "DELAYED", "EXPECTED_DELAY" -> Status.InTransit
            "AVAILABLE_FOR_DELIVERY", "AVAILABLE_FOR_DELIVERY_PAR_LOC" -> Status.InWarehouse
            "DELIVERED" -> Status.Delivered
            "DELIVERY_IMPOSSIBLE", "DELIVERY_REFUSED", "STOPPED", "RETURNED" -> Status.DeliveryFailure
            "OTHER", "INFORMED" -> Status.Unknown
            else -> logUnknownStatus("PostNord", item.status.code)
        }

        val history = item.events.map {
            ParcelHistoryItem(
                it.eventDescription,
                ZonedDateTime.parse(it.eventTime).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                if (it.location.name != null) "${it.location.name}, ${it.location.countryCode}"
                else it.location.countryCode
            )
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
        val status: ItemStatus
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
    internal data class ItemStatus(
        val code: String,
        val header: String,
        val description: String
    )
}
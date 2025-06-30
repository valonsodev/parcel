package dev.itsvic.parceltracker.api

import android.os.LocaleList
import com.squareup.moshi.JsonClass
import dev.itsvic.parceltracker.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

// Reverse-engineered from https://inpost.pl/en/find-parcel
object InPostDeliveryService : DeliveryService {
  override val nameResource: Int = R.string.service_inpost
  override val acceptsPostCode: Boolean = false
  override val requiresPostCode: Boolean = false

  private const val BASE_URL = "https://inpost.pl/"

  private val retrofit =
      Retrofit.Builder()
          .baseUrl(BASE_URL)
          .client(api_client)
          .addConverterFactory(api_factory)
          .build()

  private val service = retrofit.create(API::class.java)

  override fun acceptsFormat(trackingId: String): Boolean {
    val inpostRegex = """^\d{24}$""".toRegex()
    return inpostRegex.matchEntire(trackingId) != null
  }

  override suspend fun getParcel(trackingId: String, postalCode: String?): Parcel {
    val language = mapLanguageToAPIFormat(LocaleList.getDefault().get(0).language)
    val response =
        try {
          service.getParcel(trackingId, true, language)
        } catch (_: Exception) {
          throw ParcelNonExistentException()
        }

    val parcelData = response.firstOrNull() ?: throw ParcelNonExistentException()

    return Parcel(
        parcelData.mainTrackingNumber,
        eventsToHistory(parcelData.events),
        eventCodeToStatus(parcelData.events.first().eventCode))
  }

  private fun mapLanguageToAPIFormat(language: String): String {
    return when (language) {
      "pl" -> "pl_PL"
      "uk" -> "en_UK"
      "ua" -> "uk_UA"
      else -> "en_EN" // Fallback to English
    }
  }

  private fun eventCodeToStatus(eventCode: String): Status {
    // All event codes: https://developers.inpost-group.com/tracking-events
    return when (eventCode) {
      "CRE.1001" -> Status.Preadvice
      "CRE.1002" -> Status.AwaitingPickup
      "FMD.1001" -> Status.AwaitingPickup
      "FMD.1002" -> Status.PickedUp
      "FMD.1003" -> Status.InTransit
      "FMD.1004" -> Status.PickedUp
      "FMD.1005" -> Status.PickedUp
      "MMD.1001" -> Status.InWarehouse
      "MMD.1003" -> Status.InTransit
      "MMD.1004" -> Status.InTransit
      "LMD.1001" -> Status.InTransit
      "LMD.1002" -> Status.Delivered
      "LMD.1003" -> Status.AwaitingPickup
      "LMD.1004" -> Status.AwaitingPickup
      "LMD.1005" -> Status.AwaitingPickup
      "RTS.1001" -> Status.DeliveryFailure
      "RTS.1002" -> Status.Delivered
      "FUL.1003" -> Status.OutForDelivery
      "EOL.1001" -> Status.Delivered
      "EOL.1002" -> Status.PickedUp
      "EOL.1003" -> Status.Delivered
      "EOL.1004" -> Status.Delivered
      "EOL.1005" -> Status.Delivered
      "HAN.1001" -> Status.InTransit
      else -> logUnknownStatus("InPost", eventCode)
    }
  }

  private fun eventsToHistory(events: List<Event>): List<ParcelHistoryItem> {
    return events.map { item ->
      ParcelHistoryItem(
          item.eventDescription,
          LocalDateTime.parse(item.timestamp, DateTimeFormatter.ISO_DATE_TIME),
          item.location ?: "")
    }
  }

  private interface API {
    @GET("shipx-proxy/")
    @Headers(
        "X-Requested-With: XMLHttpRequest") // This is a necessary hack to spoof Ajax. Otherwise we
    // would get 403
    suspend fun getParcel(
        @Query("number") number: String,
        @Query("new_api") newApi: Boolean,
        @Query("language") language: String
    ): List<ParcelResponse>
  }

  @JsonClass(generateAdapter = true)
  internal data class ParcelResponse(
      val mainTrackingNumber: String,
      val status: Any?, // seems to always be null
      val statusTitle: Any?, // seems to always be null
      val statusDescription: Any?, // seems to always be null
      val trackingNumbers: List<TrackingNumbers>,
      val events: List<Event>
  )

  @JsonClass(generateAdapter = true)
  internal data class TrackingNumbers(val trackingNumber: String, val kind: String)

  @JsonClass(generateAdapter = true)
  internal data class Event(
      val eventCode: String,
      val status: Any?, // seems to always be null
      val timestamp: String, // format: 0000-00-00T00:00:00.000+00:00
      val carrier: String,
      val eventTitle: String,
      val eventDescription: String,
      val statusTitle: Any?, // seems to always be null
      val statusDescription: Any?, // seems to always be null
      val location: String? // seems to always be null
  )
}

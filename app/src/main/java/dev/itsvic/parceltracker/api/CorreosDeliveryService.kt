// SPDX-License-Identifier: GPL-3.0-or-later
package dev.itsvic.parceltracker.api

import android.os.LocaleList
import com.squareup.moshi.JsonClass
import dev.itsvic.parceltracker.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.text.toIntOrNull
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

// Reverse-engineered from https://www.correos.es (public tracking API)
// Example request:
// https://api1.correos.es/digital-services/searchengines/api/v1/envios?text=<TRACKING>&language=<LANG>
object CorreosDeliveryService : DeliveryService {
  override val nameResource: Int = R.string.service_correos
  override val acceptsPostCode: Boolean = false
  override val requiresPostCode: Boolean = false

  private val supportedLanguages = setOf("ES", "FR", "EN", "PT", "ZH")

  override suspend fun getParcel(trackingId: String, postalCode: String?): Parcel {
    val resp =
        runCatching { service.getShipment(trackingId, getApiLanguage()) }
            .getOrElse { throw ParcelNonExistentException() }

    val shipment = resp.shipment?.firstOrNull() ?: throw ParcelNonExistentException()

    // events are in chronological order, reverse
    val events = shipment.events?.reversed() ?: throw ParcelNonExistentException()
    if (events.isEmpty()) throw ParcelNonExistentException()

    val history =
        events.map { evt ->
          ParcelHistoryItem(
              description = evt.extendedText ?: evt.summaryText ?: "",
              time = parseCorreosDateTime(evt.eventDate, evt.eventTime),
              location = evt.codired ?: "")
        }

    // There is a "pendingCustomsPay" field so maybe Status.CustomsHeld if true
    // It's always been null for me so idk
    val status = mapStatus(events.firstOrNull())
    return Parcel(shipment.shipmentCode ?: trackingId, history, status)
  }

  private fun getApiLanguage(): String {
    val language = LocaleList.getDefault()[0].language.uppercase(Locale.ROOT)
    return language.takeIf { it in supportedLanguages } ?: "EN"
  }

  private fun mapStatus(event: Event?): Status {
    if (event == null) return Status.Unknown

    when (event.phase?.toIntOrNull()) {
      0,
      1 -> return Status.Preadvice
      2 -> return Status.InTransit
      3 -> return Status.AwaitingPickup
      4 -> return Status.PickedUp
      else -> {
        logUnknownStatus("Correos", "Unknown phase: ${event.phase}")
      }
    }
    // Fall back to desPhase
    when (event.desPhase) {
      "PRE-ADMISIÓN" -> return Status.Preadvice
      "EN CAMINO" -> return Status.InTransit
      "EN ENTREGA" -> return Status.OutForDelivery
      "ENTREGADO" -> return Status.Delivered
      else -> {
        logUnknownStatus("Correos", "Unknown desPhase: ${event.desPhase}")
      }
    }

    return Status.Unknown
  }

  private val correosDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

  private fun parseCorreosDateTime(date: String?, time: String?): LocalDateTime {
    // date format: dd/MM/yyyy ; time format: HH:mm:ss
    return runCatching { LocalDateTime.parse("$date $time", correosDateTimeFormatter) }
        .getOrDefault(LocalDateTime.MIN)
  }

  private val retrofit =
      Retrofit.Builder()
          .baseUrl("https://api1.correos.es/digital-services/searchengines/api/v1/")
          .client(api_client)
          .addConverterFactory(api_factory)
          .build()

  private val service = retrofit.create(API::class.java)

  private interface API {
    @GET("envios")
    suspend fun getShipment(
        @Query("text") trackingId: String,
        @Query("language") language: String,
    ): CorreosResponse
  }

  @JsonClass(generateAdapter = true)
  internal data class CorreosResponse(
      val type: String?,
      val shipment: List<CorreosShipment>?,
  )

  @JsonClass(generateAdapter = true)
  internal data class CorreosShipment(
      val shipmentCode: String?,
      val events: List<Event>?,
      val pendingCustomsPay: Boolean? = null,
  )

  @JsonClass(generateAdapter = true)
  internal data class Event(
      val eventDate: String?,
      val eventTime: String?,
      val phase: String?,
      val desPhase: String?,
      val colour: String?,
      val summaryText: String?,
      val extendedText: String?,
      val actionWeb: String?,
      val actionWebParam: String?,
      val codired: String?,
  )
}

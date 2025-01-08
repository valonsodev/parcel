package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Reverse-engineered from https://emonitoring.poczta-polska.pl
private const val API_URL = "https://uss.poczta-polska.pl/uss/v1.1/tracking/checkmailex"
private const val API_KEY =
    "BiGwVG2XHvXY+kPwJVPA8gnKchOFsyy39Thkyb1wAiWcKLQ1ICyLiCrxj1+vVGC+kQk3k0b74qkmt5/qVIzo7lTfXhfgJ72Iyzz05wH2XZI6AgXVDciX7G2jLCdoOEM6XegPsMJChiouWS2RZuf3eOXpK5RPl8Sy4pWj+b07MLg=.Mjg0Q0NFNzM0RTBERTIwOTNFOUYxNkYxMUY1NDZGMTA0NDMwQUIyRjg4REUxMjk5NDAyMkQ0N0VCNDgwNTc1NA==.b24415d1b30a456cb8ba187b34cb6a86"

private val ppReqAdapter = api_moshi.adapter(PolishPostRequest::class.java)
private val ppRespAdapter = api_moshi.adapter(PolishPostResponse::class.java)

internal fun getPolishPostParcel(id: String): Parcel {
    val ppReq = PolishPostRequest(id, "EN", true)
    val body = ppReqAdapter.toJson(ppReq).toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url(API_URL)
        .header("Api_key", API_KEY)
        .post(body)
        .build()

    api_client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val resp = ppRespAdapter.fromJson(response.body!!.source())

        if (resp != null) {
            if (resp.mailInfo == null || resp.mailStatus == -1) throw ParcelNonExistentException()
            val history = resp.mailInfo.events.reversed().map { item ->
                ParcelHistoryItem(
                    item.name,
                    LocalDateTime.parse(item.time, DateTimeFormatter.ISO_DATE_TIME),
                    if (item.postOffice.description != null) "${item.postOffice.name}\n${item.postOffice.description.street} ${item.postOffice.description.houseNumber}\n${item.postOffice.description.zipCode} ${item.postOffice.description.city}" else item.postOffice.name
                )
            }
            val status = when (resp.mailInfo.events.last().code) {
                "P_NAD" -> Status.Preadvice
                "P_ZWC" -> Status.Customs
                "P_ZWOLDDOR" -> Status.Customs
                "P_WPUCPP" -> Status.Customs
                "P_WZL" -> Status.InTransit
                "P_WDML" -> Status.OutForDelivery
                "P_A" -> Status.DeliveryFailure
                "P_KWD" -> Status.AwaitingPickup
                "P_OWU" -> Status.PickedUp
                // Post-delivery customs declaration(?)
                "P_ROZL_CEL" -> Status.Delivered
                else -> logUnknownStatus("Polish Post", resp.mailInfo.events.last().code)
            }

            return Parcel(resp.number, history, status)
        }
    }

    throw ParcelNonExistentException()
}

@JsonClass(generateAdapter = true)
internal data class PolishPostRequest(
    val number: String,
    val language: String, // X1 - Polish(?), EN - English
    val addPostOfficeInfo: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class PolishPostResponse(
    val mailInfo: PolishPostParcel?,
    val mailStatus: Int,
    val number: String,
)

@JsonClass(generateAdapter = true)
internal data class PolishPostParcel(
    val number: String,
    val events: List<PolishPostParcelHistoryItem>,
)

@JsonClass(generateAdapter = true)
internal data class PolishPostParcelHistoryItem(
    val code: String,
    val name: String,
    val postOffice: PolishPostPostOffice,
    val time: String,
)

@JsonClass(generateAdapter = true)
internal data class PolishPostPostOffice(
    val name: String,
    val description: PolishPostPostOfficeDescription?,
)

@JsonClass(generateAdapter = true)
internal data class PolishPostPostOfficeDescription(
    val city: String,
    val houseNumber: String,
    val street: String,
    val zipCode: String,
)

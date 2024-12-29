package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

private const val API_URL = "https://api-test.dhl.com/track/shipments"
private const val API_KEY = "demo-key"  // TODO: find a good way to store secrets outside of Git

private val client = OkHttpClient()
private val moshi = Moshi.Builder().build()
private val dhlJsonAdapter = moshi.adapter(DHLResponse::class.java)

fun getParcelData(trackingNumber: String): DHLResponse? {
    val request = Request.Builder()
        .url("$API_URL?trackingNumber=$trackingNumber")
        .header("DHL-API-Key", API_KEY)
        .build()
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val shipment = dhlJsonAdapter.fromJson(response.body!!.source())
        return shipment
    }
}

@JsonClass(generateAdapter = true)
data class DHLShipment(
    val id: String,
    val service: String,
)

@JsonClass(generateAdapter = true)
data class DHLResponse(
    val shipments: List<DHLShipment>,
)

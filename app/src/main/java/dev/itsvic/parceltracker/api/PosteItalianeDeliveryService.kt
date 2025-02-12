package dev.itsvic.parceltracker.api

import com.squareup.moshi.JsonClass
import dev.itsvic.parceltracker.R
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import java.math.BigInteger

// Poste Italiane (Italian Post)
// fucking asshats with their shitty italian API

object PosteItalianeDeliveryService : DeliveryService {
    override val nameResource: Int = R.string.service_poste_italiane

    override suspend fun getParcel(trackingId: String, postalCode: String?): Parcel {
        TODO("Not yet implemented")
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.poste.it/online/dovequando/DQ-REST/")
        .client(api_client)
        .addConverterFactory(api_factory)
        .build()

    private val service = retrofit.create(API::class.java)

    private interface API {
        @POST("ricercasemplice")
        suspend fun getParcel(
            @Body data: GetParcelRequest
        ): GetParcelResponse
    }

    @JsonClass(generateAdapter = true)
    internal data class GetParcelRequest(
        val codiceSpedizione: String,
        val tipoRichiedente: String = "WEB",
        val periodoRicerca: Int = 1,
    )

    @JsonClass(generateAdapter = true)
    internal data class GetParcelResponse(
        /** movement list */
        val listaMovimenti: List<ParcelEvent>,
    )

    @JsonClass(generateAdapter = true)
    internal data class ParcelEvent(
        /** possibly state of the event? */
        val box: String,
        /** event date, epoch with milliseconds */
        val dataOra: BigInteger,
        /** return flag */
        val flagRitorno: Boolean,
        /** post code */
        val frazionario: String?,
        /** place */
        val luogo: String,
        /** state in italian */
        val statoLavorazione: String,
    )
}
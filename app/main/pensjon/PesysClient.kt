package pensjon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.aap.ktor.client.AzureAdTokenProvider
import no.nav.aap.ktor.client.AzureConfig
import org.slf4j.LoggerFactory
import java.time.LocalDate

private val secureLog = LoggerFactory.getLogger("secureLog")

class PesysClient(azureConfig: AzureConfig) {

    private val tokenProvider = AzureAdTokenProvider(
        config = azureConfig,
        scope = "api://dev-fss.pensjon-saksbehandling.pensjon-pen-proxy-fss-q2/.default"
    )

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) = secureLog.info(message)
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }

    suspend fun hentUføreHistorikk(personident: String, virkningstidspunktFom: LocalDate): Uførehistorikk? {
        val response = httpClient.get("https://pensjon-pen-proxy-fss-q2.dev-fss-pub.nais.io/aap/uforeperioder?virkFom=$virkningstidspunktFom") {
            header("fnr", personident)
            bearerAuth(tokenProvider.getClientCredentialToken())
            accept(ContentType.Application.Json)

        }
        return if (response.status.isSuccess()) {
            try {
                response.body<Uførehistorikk>()
            } catch (e: Exception) {
                secureLog.warn("Failed to parse JSON to `data class Uførehistorikk(...)`:\n${response.bodyAsText()}", e)
                null
            }
        } else null
    }

    suspend fun hentVilkårsinformasjon(personident: String, vedtaksreferanse: String): Vedtaksinfo? {

        val response = httpClient.get("https://pensjon-pen-proxy-fss-q2.dev-fss-pub.nais.io/aap/vedtak/vilkarliste/$vedtaksreferanse") {
            header("fnr", personident)
            bearerAuth(tokenProvider.getClientCredentialToken())
            accept(ContentType.Application.Json)

        }
        return if (response.status.isSuccess()) {
            try {
                response.body<Vedtaksinfo>()
            } catch (e: Exception) {
                secureLog.warn("Failed to parse JSON to `data class Vedtaksinfo(...)`:\n${response.bodyAsText()}", e)
                null
            }
        } else null
    }
}

enum class VedtakType {
    OPPHOR,
    SAMMENSTOT,
    FORGANG,
    ENDRING,
    INTERNKON
}

data class Uførehistorikk(
    val uforePerioder: List<Periode>,
    val yrkesskadePerioder: List<Periode>
)

data class Periode(
    val periodeFom: LocalDate?,
    val periodeTom: LocalDate?,
    val uforetidspunkt: LocalDate?,
//    val skadetidspunkt: LocalDate?,
    val virkFom: LocalDate,
    val grad: Int
)

data class Vedtaksinfo(
    val vedtakType: VedtakTypeDto,
    val virkFom: LocalDate,
    val vilkarsvedtakResultat: VilkarsvedtakResultat,
    val vilkarListe: List<VilkarInfo>,
    val kravGjelder: KravGjelder,
    val mottattDato: LocalDate,
    val boddArbeidUtland: Boolean,
    val vurdereTrygdeAvtale: Boolean,
    val norgeBehandlendeLand: Boolean
)

data class VilkarInfo(
    val vilkarType: VilkarType,
    val resultat: Resultat,
    var standardbegrunnelseCode: StandardbegrunnelseCode,
    val standardbegrunnelse: String
)

enum class StandardbegrunnelseCode {
    STDBEGR_12_13_1_I_1,
    STDBEGR_12_13_1_I_2,
    STDBEGR_12_13_1_I_3,
    STDBEGR_12_13_1_O_1,
    STDBEGR_12_17_1_1,
    STDBEGR_12_17_1_I_1,
    STDBEGR_12_17_1_I_2,
    STDBEGR_12_17_1_O_1,
    STDBEGR_12_17_1_O_2,
    STDBEGR_12_17_1_O_3,
    STDBEGR_12_17_2_1,
    STDBEGR_12_17_2_2,
    STDBEGR_12_17_2_3,
    STDBEGR_12_17_2_4,
    STDBEGR_12_17_2_5,
    STDBEGR_12_17_3_1,
    STDBEGR_12_17_3_2,
    STDBEGR_12_2_1_I_1,
    STDBEGR_12_2_1_O_1,
    STDBEGR_12_2_1_O_2,
    STDBEGR_12_2_1_O_3,
    STDBEGR_12_2_1_O_4,
    STDBEGR_12_2_1_O_5,
    STDBEGR_12_2_1_O_6,
    STDBEGR_12_3_1_I_1,
    STDBEGR_12_3_1_O_1,
    STDBEGR_12_4_1_I_1,
    STDBEGR_12_4_1_I_2,
    STDBEGR_12_4_1_O_1,
    STDBEGR_12_4_1_O_2,
    STDBEGR_12_4_1_O_3,
    STDBEGR_12_4_1_O_4,
    STDBEGR_12_4_1_O_5,
    STDBEGR_12_4_1_O_6,
    STDBEGR_12_5_1_I_1,
    STDBEGR_12_5_1_I_2,
    STDBEGR_12_5_1_I_3,
    STDBEGR_12_5_1_O_1,
    STDBEGR_12_5_1_O_2,
    STDBEGR_12_5_2_I_1,
    STDBEGR_12_5_2_I_2,
    STDBEGR_12_5_2_I_3,
    STDBEGR_12_5_2_O_1,
    STDBEGR_12_5_2_O_2,
    STDBEGR_12_6_1_I_1,
    STDBEGR_12_6_1_I_2,
    STDBEGR_12_6_1_I_3,
    STDBEGR_12_6_1_O_1,
    STDBEGR_12_6_1_O_2,
    STDBEGR_12_7_1_1,
    STDBEGR_12_7_1_2,
    STDBEGR_12_7_1_3,
    STDBEGR_12_7_1_4,
    STDBEGR_12_7_1_I_1,
    STDBEGR_12_7_1_I_2,
    STDBEGR_12_7_2_I_1,
    STDBEGR_12_7_2_I_2,
    STDBEGR_12_7_2_I_3,
    STDBEGR_12_7_2_I_4,
    STDBEGR_12_7_2_O_1,
    STDBEGR_12_7_2_O_2,
    STDBEGR_12_7_2_O_3,
    STDBEGR_12_7_2_O_4,
    STDBEGR_12_8_1_1,
    STDBEGR_12_8_1_2,
    STDBEGR_12_8_1_3,
    STDBEGR_12_8_2_1,
    STDBEGR_12_8_2_10,
    STDBEGR_12_8_2_11,
    STDBEGR_12_8_2_2,
    STDBEGR_12_8_2_3,
    STDBEGR_12_8_2_4,
    STDBEGR_12_8_2_5,
    STDBEGR_12_8_2_9,
    STDBEGR_12_9_1_1,
    STDBEGR_12_9_1_2,
    STDBEGR_12_9_1_3,
    STDBEGR_22_12_1_1,
    STDBEGR_22_12_1_11,
    STDBEGR_22_12_1_12,
    STDBEGR_22_12_1_2,
    STDBEGR_22_12_1_3,
    STDBEGR_22_12_1_4,
    STDBEGR_22_12_1_5,
    STDBEGR_17_5_A,
    STDBEGR_17_5_B,
    STDBEGR_17_5_C,
    STDBEGR_17_10_1,
    STDBEGR_17_10_2,
    STDBEGR_22_12_1_13,
    STDBEGR_22_12_1_14,
    STDBEGR_22_12_1_15,
    STDBEGR_17_12,
}

enum class VilkarsvedtakResultat {
    AVSLAG,
    INNVILGET,
    OPPHOR,
    VELG,
    VETIKKE
}

enum class VedtakTypeDto {
    FORSTEGANGSBEHANDLING,  //kun denne ?
    ANNET
}

enum class Resultat {
    IKKE_OPPFYLT,
    IKKE_VURDERT,
    OPPFYLT
}

enum class VilkarType {
    ALDER,
    FORTSATT_MEDLEMSKAP,
    FORUTGAENDE_MEDLEMSKAP,
    GARANTERTUNGUFOR,
    HENSIKTSMESSIG_BEH,
    HENS_ARBRETT_TILTAK,
    M_F_UT_ETR_TRYAVTAL,
    NEDSATT_INNT_EVNE,
    NIE_MIN_HALV,
    RETT_TIL_EKSPORT_ETTER_TRYGDEAVTALE,
    SYKDOM_SKADE_LYTE,
    YRKESSKADE,
    RETT_TIL_GJENLEVENDE_TILLEGG_VILKAR,
    FORSORGET_AV_AVDOD,
    DOD_SKYLDES_YRKESSKADE
}

enum class KravGjelder {
    FORSTEGANGSBEHANDLING,
    FORSTEGANGSBEHANDLING_BOSATT_UTLAND,
    FORSTEGANGSBEHANDLING_NORGE_UTLAND,
    ANNET
}

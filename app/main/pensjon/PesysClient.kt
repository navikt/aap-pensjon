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
import java.util.*

private val secureLog = LoggerFactory.getLogger("secureLog")

class PesysClient(azureConfig: AzureConfig) {

    val tokenProvider = AzureAdTokenProvider(
        config = azureConfig,
        scope = "api://dev-gcp.teampensjon.pensjon-pen-q2/.default"
    )

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Logging) {
            level = LogLevel.BODY
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

        val response = httpClient.get("https://pensjon-pen-q2.dev.adeo.no/pen/springapi/uforeperioder?virkFom=$virkningstidspunktFom") {
            header("fnr", personident)
            bearerAuth(tokenProvider.getClientCredentialToken())
            accept(ContentType.Application.Json)

        }
        return if (response.status.isSuccess()) {
            response.body<Uførehistorikk>()
        } else null

    }

    suspend fun hentVilkårsinformasjon(personident: String, vedtaksreferanse: String): String? {

        val response = httpClient.get("https://pensjon-pen-q2.dev.adeo.no/pen/springapi/vedtak/vilkarliste/$vedtaksreferanse") {
            header("fnr", personident)
            bearerAuth(tokenProvider.getClientCredentialToken())
            accept(ContentType.Application.Json)

        }
        return if (response.status.isSuccess()) {
            response.bodyAsText()
        } else null

    }
}

enum class vedtakType {
    OPPHOR,
    SAMMENSTOT,
    FORGANG,
    ENDRING,
    INTERNKON
}

data class Uførehistorikk(
    val uforePeriodeListe: List<Periode>,
    val yrkesskadePeriodeListe: List<Periode>
)

data class Periode(
    val uforePeriodeFom: LocalDate,
    val uforePeriodeTom: LocalDate,
    val uforetidspunkt: LocalDate?,
    val skadetidspunkt: LocalDate?,
    val virkFom: LocalDate,
    val grad: Int
)


package pensjon

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.aap.kafka.streams.v2.KafkaStreams
import no.nav.aap.kafka.streams.v2.Streams
import no.nav.aap.ktor.config.loadConfig
import pensjon.kafka.topology
import java.time.LocalDate

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.server(kafka: Streams = KafkaStreams()) {
    val config = loadConfig<Config>()
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = prometheus }
    install(ContentNegotiation) { jackson {} }

    val pesysClient = PesysClient(config.azure)

    kafka.connect(
        config = config.kafka,
        registry = prometheus,
        topology = topology(pesysClient)
    )

    routing {
        actuators(prometheus, kafka)

        get("/uførehistorikk/{personident}") {
            val personident = call.parameters.getOrFail("personident")
            val scope = call.request.header("scope")!!
            pesysClient.hentUføreHistorikk(personident, LocalDate.now().minusYears(10), scope)
                ?.let { call.respond(it) }
                ?: call.respond("no match")
        }

        get("/vilkårsinformasjon/{personident}") {
            val personident = call.parameters.getOrFail("personident")
            val scope = call.request.header("scope")!!
            pesysClient.hentVilkårsinformasjon(personident, LocalDate.now().toString(), scope)
                ?.let { call.respond(it) }
                ?: call.respond("no match")
        }
    }
}

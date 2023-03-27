package pensjon

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.test.StreamsMock
import no.nav.aap.ktor.client.AzureConfig

import org.junit.jupiter.api.Test
import pensjon.kafka.topology
import java.io.File
import java.net.URL


class DescribeTopology {

    @Test
    fun mermaid() {
        val topology = topology(PesysClient(AzureConfig(URL("http:mock.no"), "", "")))

        val kafka = StreamsMock()
        kafka.connect(topology, StreamsConfig("", ""), SimpleMeterRegistry())

        val diagram = kafka.visulize().mermaid().generateDiagram()
        File("../docs/topology.mmd").apply { writeText(diagram) }
    }
}
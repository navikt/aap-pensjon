package pensjon

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.aap.kafka.streams.v2.config.StreamsConfig
import no.nav.aap.kafka.streams.v2.test.KStreamsMock

import org.junit.jupiter.api.Test
import pensjon.kafka.topology
import java.io.File


class DescribeTopology {

    @Test
    fun mermaid() {

        val topology = topology()

        val kafka = KStreamsMock()
        kafka.connect(topology, StreamsConfig("", ""), SimpleMeterRegistry())

        kafka.visulize().mermaid().generateSubDiagrams().forEachIndexed { index, mermaid ->
            File("../docs/topology$index.mmd").apply { writeText(mermaid) }
        }
    }
}
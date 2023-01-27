package pensjon.kafka

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology

fun topology(): Topology{
    val streams = StreamsBuilder()
    return streams.build()
}
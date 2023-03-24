package pensjon.kafka

import no.nav.aap.kafka.streams.v2.Topology
import no.nav.aap.kafka.streams.v2.topology
import pensjon.PesysClient

internal fun topology(pesysClient: PesysClient): Topology = topology{
    consume(Topics.kravbehandling)
        .repartition(12)
        .rekey { hendelse -> hendelse.jsonPayLoad.fnr } //TODO Er dette aktiv eller må vi slå opp i PDL?
        .secureLog { value -> info("kravbehandling: $value") }
        .forEach { key, value ->  }

    consume(Topics.vedtaksbehandling)
        .repartition(12)
        .rekey { hendelse -> hendelse.jsonPayLoad.fnr } //TODO Er dette aktiv eller må vi slå opp i PDL?
        .secureLog { value -> info("vedtaksbehandling: $value") }
        .forEach { key, value ->  }

}

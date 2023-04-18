package pensjon.kafka

import no.nav.aap.kafka.streams.v2.Topic
import no.nav.aap.kafka.streams.v2.serde.JsonSerde

object Topics {
    val kravbehandling = Topic("pensjon-saksbehandling.pen-kravbehandling-v1", JsonSerde.jackson<KravHendelse>())
    val vedtaksbehandling = Topic("pensjon-saksbehandling.pen-vedtaksbehandling-v1", JsonSerde.jackson<VedtakHendelse>())
}

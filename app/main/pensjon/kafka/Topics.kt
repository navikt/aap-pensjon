package pensjon.kafka

import no.nav.aap.kafka.streams.v2.Topic
import no.nav.aap.kafka.streams.v2.serde.JsonSerde

object Topics {

    val kravbehandling = Topic("pen-kravbehandling-v1", JsonSerde.jackson<KravHendelse>())
    val vedtaksbehandling = Topic("pen-vedtaksbehandling-v1", JsonSerde.jackson<VedtakHendelse>())

}

package pensjon.kafka

data class KravHendelse(
    val uuid: String,
    val jsonPayLoad: Payload
) {
    data class Payload(
        val fnr: String,
        val virkFom: String,
        val kravStatus: String,
        val datoHendelse: String
    )
}

data class VedtakHendelse(
    val uuid: String,
    val jsonPayLoad: Payload
){
    data class Payload(
        val fnr: String,
        val vedtakReferanse: String,
        val datoHendelse: String
    )
}




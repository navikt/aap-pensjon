package pensjon

import no.nav.aap.kafka.streams.KStreamsConfig
import no.nav.aap.ktor.client.AzureConfig

internal data class Config(
    val azure: AzureConfig,
    val kafka: KStreamsConfig,
)

package $package$.server.model

import kotlinx.serialization.Serializable

@Serializable
data class Ping(val pong: String)

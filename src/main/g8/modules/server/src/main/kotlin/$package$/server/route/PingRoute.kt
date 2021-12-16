package $package$.server.route

import $package$.server.model.Ping
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun Application.pingRouting() {
    routing {
        authenticate("auth-jwt") {
            route("/api") {
                pingRoute()
            }
        }
    }
}


private fun Route.pingRoute() {
    route("/ping") {
        get {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("preferred_username").asString()

            val response = "[pong] Username: \$username"

            call.respondText(ContentType.Application.Json, HttpStatusCode.OK, suspend {
                Json.encodeToString(Ping(response))
            })
        }
    }
}


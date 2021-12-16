package $package$.server.route

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*

fun Application.loginRouting() {
    routing {
        authenticate("oauth2") {
            route("/") {
                loginRoute()
                callbackRoute()
            }
        }
    }
}

private fun Route.loginRoute() {
    get("/login") {
        // redirect to keycloak login page
    }
}

private fun Route.callbackRoute() {
    get("/callback") {
        val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()

        call.application.log.info("[callback] Access token: \${principal?.accessToken}")
        call.respond(HttpStatusCode.OK, "Success")
    }
}
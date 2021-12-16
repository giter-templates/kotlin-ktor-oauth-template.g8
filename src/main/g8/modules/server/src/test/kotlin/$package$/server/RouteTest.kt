package $package$.server

import $package$.server.model.Ping
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class RouteTest {
    @Test
    fun testRoot() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/ping").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(Json.encodeToString(Ping("pong")), response.content)
            }
        }
    }
}
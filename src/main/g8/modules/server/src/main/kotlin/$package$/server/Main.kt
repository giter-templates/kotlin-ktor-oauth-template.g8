package $package$.server

import com.auth0.jwk.JwkProviderBuilder
import $package$.server.route.loginRouting
import $package$.server.route.pingRouting
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.sessions.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.net.URL
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.registerRoutes() {
    routing {
        loginRouting()
        pingRouting()
    }
}

fun Application.metrics(prometheusRegistry: PrometheusMeterRegistry) {
    install(MicrometerMetrics) {
        registry = prometheusRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics()
        )
    }

    routing {
        get("/metrics") {
            call.respond(prometheusRegistry.scrape())
        }
    }
}

fun Application.oauth() {
    val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    val name = environment.config.property("oauth.name").getString()
    val authorizeUrl = environment.config.property("oauth.authorizeUrl").getString()
    val accessTokenUrl = environment.config.property("oauth.accessTokenUrl").getString()
    val clientId = environment.config.property("oauth.clientId").getString()
    val clientSecret = environment.config.property("oauth.clientSecret").getString()

    val oauthProvider = OAuthServerSettings.OAuth2ServerSettings(
        name = name,
        authorizeUrl = authorizeUrl,
        accessTokenUrl = accessTokenUrl,
        clientId = clientId,
        clientSecret = clientSecret,
        accessTokenRequiresBasicAuth = false,
        requestMethod = HttpMethod.Post,
        defaultScopes = listOf("roles")
    )

    val issuer = environment.config.property("jwt.issuer").getString()
    val realm = environment.config.property("jwt.realm").getString()
    val certUrl = environment.config.property("jwt.certUrl").getString()

    val jwkProvider = JwkProviderBuilder(certUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        oauth("oauth2") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                oauthProvider
            }
            client = httpClient
        }

        jwt("auth-jwt") {
            realm = realm
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }
}

fun Application.module() {
    val appMicrometerRegistry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(ContentNegotiation) {
        json()
    }

    oauth()
    metrics(appMicrometerRegistry)
    registerRoutes()
}

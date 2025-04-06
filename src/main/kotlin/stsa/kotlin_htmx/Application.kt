// src/main/kotlin/stsa/kotlin_htmx/Application.kt
package stsa.kotlin_htmx

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import kotlinx.coroutines.launch
import stsa.kotlin_htmx.plugins.configureHTTP
import stsa.kotlin_htmx.plugins.configureMonitoring
import stsa.kotlin_htmx.plugins.configureRouting
import stsa.kotlin_htmx.plugins.configureDatabase
import stsa.kotlin_htmx.configurePageRoutes
import stsa.kotlin_htmx.services.DataImporter
import java.io.File
import stsa.kotlin_htmx.routes.categoryEndpoints
import io.ktor.server.routing.routing
import io.ktor.server.auth.*
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.basic

data class ApplicationConfig(
    val lookupApiKey: String
) {
    companion object {
        fun load(): ApplicationConfig {
            System.setProperty("io.ktor.development", "true")
            fun Map<String, String>.envOrLookup(key: String): String =
                System.getenv(key) ?: this[key]!!
            val envVars: Map<String, String> = envFile().let { file ->
                if (file.exists()) {
                    file.readLines()
                        .map { it.split("=") }
                        .filter { it.size == 2 }
                        .associate { it.first().trim() to it.last().trim() }
                } else emptyMap()
            }
            return ApplicationConfig(
                lookupApiKey = envVars.envOrLookup("LOOKUP_API_KEY")
            )
        }
    }
}

fun envFile(): File = listOf(".env.local", ".env.default")
    .map { File(it) }
    .first { it.exists() }

fun main() {
    if (envFile().readText().contains("KTOR_DEVELOPMENT=true")) {
        System.setProperty("io.ktor.development", "true")
    }
    embeddedServer(Netty, port = 8085, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val config = ApplicationConfig.load()

    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to /keys"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                } else null
            }
        }
    }

    configureHTTP()
    configureMonitoring()
    configureRouting()
    install(Compression)

    // Connect to the database.
    configureDatabase()

    // Launch the data importer in a coroutine.
    launch {
        DataImporter.importData()
    }



    // Load page routes.
    configurePageRoutes()

    routing {
        categoryEndpoints("skins","skin", "Skins")
        categoryEndpoints("agents","agent", "Agents")
        categoryEndpoints("crates","crate", "Crates")

        authenticate("auth-basic") {
            categoryEndpoints("keys","key", "Keys")
        }

    }




}


package stsa.kotlin_htmx.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/csgo"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPass = System.getenv("DB_PASSWORD") ?: "1234"

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPass
    )
}

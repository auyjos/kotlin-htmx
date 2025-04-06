
package stsa.kotlin_htmx.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase() {
    // Update these parameters with your actual database details.
    Database.connect(
        url = "jdbc:postgresql://localhost:5432/csgo",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "1234"
    )
}

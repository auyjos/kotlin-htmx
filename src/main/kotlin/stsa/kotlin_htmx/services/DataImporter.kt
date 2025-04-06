// src/main/kotlin/stsa/kotlin_htmx/services/DataImporter.kt
package stsa.kotlin_htmx.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import stsa.kotlin_htmx.models.CsgoItem
import stsa.kotlin_htmx.models.CsgoItemsTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.StdOutSqlLogger


object DataImporter {
    // Ktor client with JSON serialization support.
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Separate endpoints for each category.
    private const val SKINS_API_URL = "https://bymykel.github.io/CSGO-API/api/en/skins.json"
    private const val AGENTS_API_URL = "https://bymykel.github.io/CSGO-API/api/en/agents.json"
    private const val CRATES_API_URL = "https://bymykel.github.io/CSGO-API/api/en/crates.json"
    private const val KEYS_API_URL = "https://bymykel.github.io/CSGO-API/api/en/keys.json"

    // Suspend function to fetch data and populate the database.
    suspend fun importData() {
        // Ensure table exists.
        transaction {
            SchemaUtils.create(CsgoItemsTable)
        }

        // Fetch and insert skins.
        val skins: List<CsgoItem> = client.get(SKINS_API_URL).body()
        println("Fetched ${skins.size} skins")
        transaction {
            skins.forEach { insertItem(it, "skin") }
        }

        // Fetch and insert agents.
        val agents: List<CsgoItem> = client.get(AGENTS_API_URL).body()
        println("Fetched ${agents.size} skins")
        transaction {
            agents.forEach { insertItem(it, "agent") }
        }

        // Fetch and insert crates.
        val crates: List<CsgoItem> = client.get(CRATES_API_URL).body()
        println("Fetched ${crates.size} skins")
        transaction {
            crates.forEach { insertItem(it, "crate") }
        }

        // Fetch and insert keys.
        val keys: List<CsgoItem> = client.get(KEYS_API_URL).body()
        println("Fetched ${keys.size} skins")
        transaction {
            keys.forEach { insertItem(it, "key") }
        }
    }

    private fun insertItem(item: CsgoItem, category: String) {
        val exists = transaction {
            val count = CsgoItemsTable.select { CsgoItemsTable.id eq item.id }.count()
            println("Item ${item.id} exists count: $count")
            count > 0
        }
        if (!exists) {
            transaction {
                addLogger(StdOutSqlLogger)
                CsgoItemsTable.insert { row ->
                    row[CsgoItemsTable.id] = item.id
                    row[CsgoItemsTable.name] = item.name
                    row[CsgoItemsTable.description] = item.description ?: ""
                    row[CsgoItemsTable.crates] = item.crates?.firstOrNull()?.name ?: ""
                    row[CsgoItemsTable.team] = item.team?.name ?: ""
                    row[CsgoItemsTable.image] = item.image ?: ""
                    row[CsgoItemsTable.category] = category
                }
                println("Inserted item ${item.id}")
            }
        } else {
            println("Skipping item ${item.id}, already exists")
        }
    }

}

// src/main/kotlin/stsa/kotlin_htmx/services/SearchService.kt
package stsa.kotlin_htmx.services

import com.github.benmanes.caffeine.cache.Caffeine
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import stsa.kotlin_htmx.models.CsgoItemsTable
import java.util.concurrent.TimeUnit

object SearchService {
    // Cache for full-category queries.
    private val categoryCache = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build<String, List<Map<String, Any?>>>()

    // Cache for search queries.
    private val searchCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<Pair<String, String>, List<Map<String, Any?>>>()

    fun getCategoryItems(category: String): List<Map<String, Any?>> {
        return categoryCache.get(category) {
            transaction {
                CsgoItemsTable.select { CsgoItemsTable.category eq category }
                    .map { row ->
                        mapOf(
                            "id" to row[CsgoItemsTable.id],
                            "name" to row[CsgoItemsTable.name],
                            "description" to row[CsgoItemsTable.description],
                            "crates" to row[CsgoItemsTable.crates],
                            "team" to row[CsgoItemsTable.team],
                            "image" to row[CsgoItemsTable.image],
                            "category" to row[CsgoItemsTable.category]
                        )
                    }
            }
        }
    }

    fun getSearchResults(category: String, lookupValue: String): List<Map<String, Any?>> {
        return searchCache.get(Pair(category, lookupValue)) {
            transaction {
                when (category) {
                    "skin" -> CsgoItemsTable.select {
                        (CsgoItemsTable.category eq "skin") and (CsgoItemsTable.name like "%$lookupValue%")
                    }
                    "agent" -> CsgoItemsTable.select {
                        (CsgoItemsTable.category eq "agent") and (CsgoItemsTable.name like "%$lookupValue%")
                    }
                    "crate" -> CsgoItemsTable.select {
                        (CsgoItemsTable.category eq "crate") and (CsgoItemsTable.name like "%$lookupValue%")
                    }
                    "key" -> CsgoItemsTable.select {
                        (CsgoItemsTable.category eq "key") and (CsgoItemsTable.name like "%$lookupValue%")
                    }
                    else -> emptyList()
                }.map { row ->
                    mapOf(
                        "id" to row[CsgoItemsTable.id],
                        "name" to row[CsgoItemsTable.name],
                        "description" to row[CsgoItemsTable.description],
                        "crates" to row[CsgoItemsTable.crates],
                        "team" to row[CsgoItemsTable.team],
                        "image" to row[CsgoItemsTable.image],
                        "category" to row[CsgoItemsTable.category]
                    )
                }
            }
        }
    }
}

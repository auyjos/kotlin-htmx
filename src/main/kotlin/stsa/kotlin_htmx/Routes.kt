// src/main/kotlin/stsa/kotlin_htmx/Routes.kt
package stsa.kotlin_htmx

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.link.pages.LinkMainPage
import stsa.kotlin_htmx.models.CsgoItemsTable
import stsa.kotlin_htmx.pages.EmptyTemplate
import stsa.kotlin_htmx.pages.MainTemplate
import stsa.kotlin_htmx.pages.SelectionTemplate
import io.ktor.server.html.*
import kotlinx.css.ul
import kotlinx.html.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.server.request.receiveParameters
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.or
import stsa.kotlin_htmx.services.SearchService.getCategoryItems
import stsa.kotlin_htmx.services.SearchService.getSearchResults
import io.ktor.server.response.respondText
import org.jetbrains.exposed.sql.selectAll



private val logger = LoggerFactory.getLogger("stsa.kotlin_htmx.Routes")

fun Application.configurePageRoutes() {

    fun String.escapeXml(): String {
        return this
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }




    routing {
        // Main front page route.
        get("/") {
            call.respondHtmlTemplate(MainTemplate(template = EmptyTemplate(), "Front Page")) {
                mainSectionTemplate {
                    emptyContentWrapper {
                        section {
                            p { +"Prueba José Auyón" }
                        }
                    }
                }
            }
        }


        // Link route: renders the main selection page or a category-specific page.
        route("/link") {
            // GET route for /link, optionally with a ?cat= parameter.
            get {
                val catParam = call.request.queryParameters["cat"]?.lowercase()
                if (catParam.isNullOrBlank()) {
                    // No category provided: show the main selection page.
                    val linkMainPage = LinkMainPage()
                    linkMainPage.renderMainPage(this)
                } else {
                    // Normalize the category value.
                    val normalizedCat = when (catParam) {
                        "skins" -> "skin"
                        "agents" -> "agent"
                        "crates" -> "crate"
                        "keys" -> "key"
                        else -> catParam
                    }
                    // Query DB for items in the specified category.
                    val items = transaction {
                        CsgoItemsTable.select { CsgoItemsTable.category eq normalizedCat }
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
                    // Render the category-specific page with a search form.
                    call.respondHtmlTemplate(MainTemplate(template = SelectionTemplate(), "Select Main")) {
                        mainSectionTemplate {
                            selectionPagesContent {
                                section {
                                    h2 { +"Category: ${normalizedCat.capitalize()}" }
                                    // Search form for the category.
                                    form {
                                        action = "/link/search"  // Form action points to the GET search route.
                                        method = FormMethod.get
                                        encType = FormEncType.applicationXWwwFormUrlEncoded
                                        attributes["hx-get"] = "/link/search" // HTMX GET request.
                                        attributes["hx-swap"] = "outerHTML"

                                        // Hidden field to preserve the current category.
                                        input {
                                            type = InputType.hidden
                                            name = "category"
                                            value = normalizedCat
                                        }

                                        // Text field for search term.
                                        input {
                                            type = InputType.text
                                            name = "lookupValue"
                                            placeholder = "Search in ${normalizedCat.capitalize()}"
                                            attributes["aria-label"] = "Value"
                                            required = true
                                        }
                                        button {
                                            attributes["aria-label"] = "Search"
                                            +"Search"
                                        }
                                    }


                                    // Display all items for the category.
                                    if (items.isEmpty()) {
                                        p { +"No results found for ${normalizedCat.capitalize()}" }
                                    } else {
                                        ul {
                                            items.forEach { item ->
                                                li {
                                                    +"ID: ${item["id"]} | Name: ${item["name"]} | Description: ${item["description"]} | Crates: ${item["crates"]} | Team: ${item["team"]} | Image URL: ${item["image"]} | Category: ${item["category"]}"
                                                }
                                            }
                                        }
                                    }


                                    // Link to go back to the full selection page.
                                    p {
                                        a(href = "/link") { +"Back to selection" }
                                    }
                                }
                            }
                        }
                    }
                }
            }

                        }


        get("/xml") {
            val categoryParam = call.request.queryParameters["category"]?.trim()
            val lookupValue = call.request.queryParameters["lookupValue"]?.trim()

            val condition = if (categoryParam != null && lookupValue != null) {
                (CsgoItemsTable.category eq categoryParam) and (
                        (CsgoItemsTable.name like "%$lookupValue%") or
                                (CsgoItemsTable.crates like "%$lookupValue%")
                        )
            } else if (categoryParam != null) {
                CsgoItemsTable.category eq categoryParam
            } else {
                null // means selectAll
            }

            val items = transaction {
                if (condition != null) {
                    CsgoItemsTable.select { condition }
                } else {
                    CsgoItemsTable.selectAll()
                }.map {
                    mapOf(
                        "id" to it[CsgoItemsTable.id],
                        "name" to it[CsgoItemsTable.name],
                        "description" to it[CsgoItemsTable.description],
                        "crates" to it[CsgoItemsTable.crates],
                        "team" to it[CsgoItemsTable.team],
                        "image" to it[CsgoItemsTable.image],
                        "category" to it[CsgoItemsTable.category]
                    )
                }
            }

            val xml = buildString {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<results>")
                items.forEach { item ->
                    append("<item>")
                    append("<id>${item["id"].toString().escapeXml()}</id>")
                    append("<name>${item["name"].toString().escapeXml()}</name>")
                    append("<description>${item["description"].toString().escapeXml()}</description>")
                    append("<crates>${item["crates"].toString().escapeXml()}</crates>")
                    append("<team>${item["team"].toString().escapeXml()}</team>")
                    append("<image>${item["image"].toString().escapeXml()}</image>")
                    append("<category>${item["category"].toString().escapeXml()}</category>")
                    append("</item>")
                }
                append("</results>")
            }
            call.response.headers.append("Content-Disposition", "attachment; filename=\"results.xml\"")
            call.respondText(xml, ContentType.Application.Xml.withCharset(Charsets.UTF_8))
        }

    }
}





fun parseLookup(input: String): Pair<String?, String> {
    val parts = input.split("=", limit = 2)
    return if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
        val prop = parts[0].trim().lowercase()
        val normalizedProp = when (prop) {
            "skin", "skins" -> "skin"
            "agent", "agents" -> "agent"
            "crate", "crates" -> "crate"
            "key", "keys" -> "key"
            else -> null
        }
        Pair(normalizedProp, parts[1].trim())
    } else {
        // If there's no "=" or one part is blank, return null for property and the whole input as query.
        Pair(null, input.trim())
    }
}

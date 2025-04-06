package stsa.kotlin_htmx.routes
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.html.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import stsa.kotlin_htmx.models.CsgoItemsTable
import stsa.kotlin_htmx.pages.MainTemplate
import stsa.kotlin_htmx.pages.SelectionTemplate
import kotlin.text.Typography.section
import io.ktor.server.response.respondRedirect


private val logger = LoggerFactory.getLogger("stsa.kotlin_htmx.routes.LinkCategoryRoutes")

fun Route.categoryEndpoints(pathSegment: String,category: String, displayName: String) {
    // GET /<category>
    get("/$pathSegment") {
        // Optional debug
        call.response.headers.append("Cache-Control", "no-store")
        val items = transaction {
            CsgoItemsTable.select { CsgoItemsTable.category eq category }
                .map {
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

        call.respondHtmlTemplate(MainTemplate(template = SelectionTemplate(), displayName)) {
            mainSectionTemplate {
                selectionPagesContent {
                    section {
                        h2 { +displayName }

                        // Search form
                        form {
                            action = "/$pathSegment/search"
                            method = FormMethod.get
                            encType = FormEncType.applicationXWwwFormUrlEncoded
                            attributes["hx-get"] = "/$pathSegment/search"
                            attributes["hx-swap"] = "outerHTML"

                            input {
                                type = InputType.text
                                name = "lookupValue"
                                placeholder = "Search in $displayName"
                                attributes["aria-label"] = "Value"
                                required = true
                            }
                            button { +"Search" }
                        }

                        // Items list
                        if (items.isEmpty()) {
                            p { +"No $displayName found." }
                        } else {
                            ul {
                                items.forEach { item ->
                                    li {
                                        +"ID: ${item["id"]}; Name: ${item["name"]}; Description: ${item["description"]}; Crates: ${item["crates"]}; Team: ${item["team"]}; Image: ${item["image"]}; Category: ${item["category"]}"
                                    }

                                }
                            }
                        }

                        p { a(href = "/") { +"Back to selection" } }
                    }
                }
            }
        }
    }

    // GET /<category>/search
    get("/$pathSegment/search") {
        val lookupValue = call.request.queryParameters["lookupValue"]?.trim() ?: ""
        val exportParam = call.request.queryParameters["export"]?.trim()?.lowercase() ?: ""

        val condition = if (lookupValue.isBlank()) {
            CsgoItemsTable.category eq category
        } else {
            (CsgoItemsTable.category eq category) and (
                    (CsgoItemsTable.name like "%$lookupValue%") or
                            (CsgoItemsTable.crates like "%$lookupValue%")
                    )
        }

        val items = transaction {
            CsgoItemsTable.select { condition }
                .map {
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

        if (exportParam == "xml") {
            val redirectUrl = "/xml?category=$category&lookupValue=$lookupValue"
            call.respondRedirect(redirectUrl)
            return@get
        } else {
            call.respondHtml {
                body {
                    h2 { +"Results for $displayName" }
                    if (items.isEmpty()) {
                        p { +"No results found for $displayName = $lookupValue" }
                    } else {
                        ul {
                            items.forEach { item ->
                                li {
                                    +"ID: ${item["id"]}; Name: ${item["name"]}; Description: ${item["description"]}; Crates: ${item["crates"]}; Team: ${item["team"]}; Image: ${item["image"]}; Category: ${item["category"]}"
                                }
                            }
                        }
                        // ✅ Add export XML link
                        p {
                            a(href = "/$pathSegment/search?lookupValue=$lookupValue&export=xml") {
                                +"Export as XML"
                            }
                        }
                    }
                    // Back link
                    p {
                        a(href = "/$pathSegment") {
                            +"Back to $displayName"
                        }
                    }
                }
            }

        }
    }


}

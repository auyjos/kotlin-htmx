
package stsa.kotlin_htmx.models

import org.jetbrains.exposed.sql.Table

object CsgoItemsTable : Table("csgo_items") {
    val id = varchar("id", 50)
    val name = varchar("name", 255)
    val description = text("description")
    val crates = varchar("crates", 255)
    val team = varchar("team", 255)
    val image = varchar("image", 255)
    val category = varchar("category", 50) // e.g., "skin", "agent", etc.

    override val primaryKey = PrimaryKey(id, name = "PK_csgo_items")
}

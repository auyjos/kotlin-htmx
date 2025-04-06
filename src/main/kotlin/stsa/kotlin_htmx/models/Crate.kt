
package stsa.kotlin_htmx.models

import kotlinx.serialization.Serializable

@Serializable
data class Crate(
    val id: String,
    val name: String,
    val image: String
)

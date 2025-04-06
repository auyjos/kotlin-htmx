
package stsa.kotlin_htmx.models

import kotlinx.serialization.Serializable
import kotlin.jvm.Throws

@Serializable

data class CsgoItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val crates: List<Crate>? = null, // Updated to List<Crate>
    val team: Team? = null,
    val image: String? = null
)
package stsa.kotlin_htmx

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import java.util.Base64

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/skins").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/agents").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/crates").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
        client.get("/keys").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
        //TODO: <-- YOUR CODE HERE -> Use an authenticated client
        //DONE: Handled authclient
        client.get("/keys").apply {
            assertEquals(HttpStatusCode.Unauthorized, status) // this is expected
        }
    }
    @Test
    fun testKeysWithAuth() = testApplication {
        application {
            module()
        }

        val credentials = "admin:admin" // replace with your .env creds if needed
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray())

        val response = client.get("/keys") {
            header(HttpHeaders.Authorization, "Basic $encoded")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}

package de.scandurra.youtrackdiscord.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class YouTrackClient(
    private val baseUrl: String,
    private val token: String
) : AutoCloseable {
    private val json = Json {
        ignoreUnknownKeys = true
        /* for some reason the api returns duplicated keys */
        isLenient = true
    }

    private val client = HttpClient {
        install(HttpTimeout) { requestTimeoutMillis = 30000; connectTimeoutMillis = 15000; socketTimeoutMillis = 30000 }
        install(ContentNegotiation) { json(json) }
        defaultRequest { accept(ContentType.Application.Json) }
    }

    suspend fun fetchNotifications(): List<UserNotification> {
        val url = "$baseUrl/api/users/notifications"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            //parameter("$top", top)
            //parameter("$skip", skip)
            parameter("fields", arrayOf("id", "read", "metadata", "content", "timestamp").joinToString(","))
        }

        val raws: List<UserNotificationRaw> = response.body()

        return raws.map { raw ->
            val decodedContent = Decoder.decodeBase64GzipToString(raw.content)
            val decodedMetadata: NotificationMetadata = json.decodeFromString(Decoder.decodeBase64GzipToString(raw.metadata))

            UserNotification(
                read = raw.read,
                timestamp = raw.timestamp,
                id = raw.id,
                metadata = decodedMetadata,
                content = decodedContent
            )
        }
    }

    suspend fun resolveProjectId(name: String): String? {
        val url = "$baseUrl/api/admin/projects"
        val response = client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("fields", "id,shortName,name")
            parameter("query", name)
        }
        val projects: List<AdminProject> = response.body()
        return projects.firstOrNull()?.id
    }

    suspend fun createIssue(project: String, summary: String, description: String?): CreatedIssue {
        val projectId = resolveProjectId(project)
            ?: throw IllegalArgumentException("Project $project not found")
        val url = "$baseUrl/api/issues"
        val response = client.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            parameter("fields", "id,idReadable,summary")
            setBody(CreateIssueRequest(RefProject(projectId), summary, description))
        }
        return response.body()
    }

    override fun close() { client.close() }
}

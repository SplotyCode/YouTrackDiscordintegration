package de.scandurra.youtrackdiscord.notification

import de.scandurra.youtrackdiscord.client.YouTrackClient
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class SendNotificationTask (
    val client: YouTrackClient,
    val jda: JDA,
    val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(SendNotificationTask::class.java)
    val sentIds = ConcurrentHashMap.newKeySet<String>()

    suspend fun run(userId: String, delay: Duration) {
        while (true) {
            try {
                send(userId)
            } catch (e: Exception) {
                logger.error("Error while sending notifications", e)
            }
            delay(delay)
        }
    }

    private suspend fun send(userId: String) {
        val notifications = client.fetchNotifications()
            .filter { !it.read }
            .filter { sentIds.add(it.id) }
            .sortedBy { it.timestamp }
        for (notification in notifications) {
            sendDirectMessage(userId, YouTrackDiscordFormatter.toMessage(notification.metadata, baseUrl))
        }
    }

    private fun sendDirectMessage(userId: String, message: MessageCreateData) {
        val user = jda.getUserById(userId)
        if (user != null) {
            sendDirectMessage(user, message)
        } else {
            jda.retrieveUserById(userId).queue({ u ->
                sendDirectMessage(u, message)
            }, { err -> logger.error("Failed to retrieve user {}: {}", userId, err.message) })
        }
    }

    private fun sendDirectMessage(user: User, message: MessageCreateData) {
        user.openPrivateChannel().queue({ ch ->
            ch.sendMessage(message).queue(
                { logger.info("Sent DM to user {} ({})", user.name, user.id) },
                { err -> logger.error("Failed to send DM: {}", err.message) }
            )
        }, { err -> logger.error("Failed to open private channel: {}", err.message) })
    }
}

package de.scandurra.youtrackdiscord.notification

import de.scandurra.youtrackdiscord.client.YouTrackClient
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class SendNotificationTask (
    val client: YouTrackClient,
    val jda: JDA,
    val baseUrl: String,
) {
    val sentIds = ConcurrentHashMap.newKeySet<String>()

    suspend fun run(userId: String, delay: Duration) {
        while (true) {
            try {
                send(userId)
            } catch (e: Exception) {
                e.printStackTrace()
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
            }, { err -> println("[ERROR] Failed to retrieve user $userId: ${err.message}") })
        }
    }

    private fun sendDirectMessage(user: User, message: MessageCreateData) {
        user.openPrivateChannel().queue({ ch ->
            ch.sendMessage(message).queue(
                { println("[INFO] Sent DM to user ${user.name} (${user.id}") },
                { err -> println("[ERROR] Failed to send DM: ${err.message}") }
            )
        }, { err -> println("[ERROR] Failed to open private channel: ${err.message}") })
    }
}

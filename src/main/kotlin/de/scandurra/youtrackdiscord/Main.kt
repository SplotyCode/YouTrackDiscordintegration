package de.scandurra.youtrackdiscord

import de.scandurra.youtrackdiscord.application.CommandRegistry
import de.scandurra.youtrackdiscord.client.YouTrackClient
import de.scandurra.youtrackdiscord.notification.SendNotificationTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

private val logger = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    val baseUrl = requireEnv("YT_BASE_URL")
    val ytToken = requireEnv("YT_TOKEN")
    val discordToken = requireEnv("DISCORD_TOKEN")
    val discordUserId = requireEnv("DISCORD_USER_ID")

    val jda = JDABuilder.createDefault(discordToken).build()
    jda.awaitReady()
    logger.info("JDA ready as {}", jda.selfUser.asTag)

    YouTrackClient(baseUrl, ytToken).use { yt ->
        CommandRegistry.registerAndSendCommands(jda, yt, baseUrl)

        logger.info("Starting 10-minute notification scheduler for YouTrack base: {}", baseUrl)
        val task = SendNotificationTask(yt, jda, baseUrl)

        launch {
            task.run(discordUserId, 10.minutes)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                jda.shutdown()
            } catch (_: Exception) {}
        })
        while (true) delay(60.minutes)
    }
}

private fun requireEnv(name: String): String =
    System.getenv(name) ?: throw IllegalArgumentException("Missing required env var $name")


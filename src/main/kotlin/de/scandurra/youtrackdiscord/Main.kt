package de.scandurra.youtrackdiscord

import de.scandurra.youtrackdiscord.application.CommandRegistry
import de.scandurra.youtrackdiscord.client.YouTrackClient
import de.scandurra.youtrackdiscord.notification.SendNotificationTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import kotlin.time.Duration.Companion.minutes

fun main() = runBlocking {
    val baseUrl = requireEnv("YT_BASE_URL")
    val ytToken = requireEnv("YT_TOKEN")
    val discordToken = requireEnv("DISCORD_TOKEN")
    val discordUserId = requireEnv("DISCORD_USER_ID")

    val jda = JDABuilder.createDefault(discordToken).build()
    jda.awaitReady()
    println("[INFO] JDA ready as ${jda.selfUser.asTag}")

    YouTrackClient(baseUrl, ytToken).use { yt ->
        CommandRegistry.registerAndSendCommands(jda, yt, baseUrl)

        println("[INFO] Starting 10-minute notification scheduler for YouTrack base: $baseUrl")
        val task = SendNotificationTask(yt, jda, baseUrl)

        launch {
            task.run(discordUserId, 10.minutes)
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                println("[INFO] Shutting down JDA...")
                jda.shutdown()
            } catch (_: Exception) {}
        })
        while (true) delay(60.minutes)
    }
}

private fun requireEnv(name: String): String =
    System.getenv(name) ?: throw IllegalArgumentException("Missing required env var $name")


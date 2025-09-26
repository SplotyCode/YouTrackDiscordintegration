package de.scandurra.youtrackdiscord.issue

import de.scandurra.youtrackdiscord.client.YouTrackClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import java.awt.Color

class CreateIssueCommandListener(
    private val yt: YouTrackClient,
    private val baseUrl: String
) : ListenerAdapter() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name != "yt-create") return
        val project = event.getOption("project")?.asString
        val summary = event.getOption("summary")?.asString
        val description = event.getOption("description")?.asString
        if (project.isNullOrBlank() || summary.isNullOrBlank()) {
            event.reply("Project and summary are required").setEphemeral(true).queue()
            return
        }
        event.deferReply(true).queue { hook ->
            GlobalScope.launch {
                createIssue(project, summary, description, hook)
            }
        }
    }

    private suspend fun createIssue(
        project: String,
        summary: String,
        description: String?,
        hook: InteractionHook
    ) {
        try {
            val created = yt.createIssue(project, summary, description)
            val url = "$baseUrl/issue/${created.idReadable}"
            val embed = EmbedBuilder()
                .setColor(Color(0x2ECC71))
                .setTitle("Issue ${created.idReadable} created", url)
                .setDescription("Your YouTrack-Issue was successfully created.")
                .addField("Project", project, true)
                .addField("Summary", truncate(summary, 256), false)
                .apply {
                    if (!description.isNullOrBlank()) {
                        addField("Description", truncate(description, 1024), false)
                    }
                }
                .setFooter("YouTrack", null)
                .build()

            hook.editOriginalEmbeds(embed).queue()
        } catch (e: Exception) {
            val embed = EmbedBuilder()
                .setColor(Color(0xE74C3C))
                .setTitle("Issue could not be created")
                .setDescription(buildString {
                    append("An error occurred while creating your issue:\n")
                    e.message?.let { append(truncate(it, 512)) }
                })
                .build()

            hook.editOriginalEmbeds(embed).queue()
        }
    }

    private fun truncate(text: String, max: Int): String =
        if (text.length <= max) text else text.take(max - 1) + "…"
}

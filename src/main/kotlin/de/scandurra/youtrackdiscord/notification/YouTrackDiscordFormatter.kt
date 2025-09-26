package de.scandurra.youtrackdiscord.notification

import de.scandurra.youtrackdiscord.client.NotificationMetadata
import de.scandurra.youtrackdiscord.notification.Changes.Change
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import kotlin.math.min

object YouTrackDiscordFormatter {
    fun toMessage(
        metadata: NotificationMetadata,
        baseUrl: String
    ): MessageCreateData {
        val changes = metadata.toChanges()
        val issue = changes.issue
        val eb = EmbedBuilder()
        val issueUrl = "$baseUrl/issue/${issue.id}"
        val title = "[${issue.id}] ${issue.summary}"
        eb.setTitle(title, issueUrl)
        eb.setDescription(buildDescription(changes))
        val instant = Instant.ofEpochMilli(metadata.change.endTimestamp)
        eb.setFooter("${issue.project.name} • ${metadata.type}")
        eb.setTimestamp(instant)
        return MessageCreateData.fromEmbeds(eb.build())
    }

    private fun buildDescription(changes: Changes): String {
        val lines = mutableListOf<String>()

        for (ch in changes.changes) {
            when (ch) {
                is Change.Comment -> {
                    lines += "💬 ${escape(ch.text, 600)}"
                }
                is Change.FieldSet -> {
                    lines += "**${ch.field}** ${escape(ch.to, 120)}"
                }
                is Change.FieldChange -> {
                    lines += "**${ch.field}**: ${escape(ch.from, 120)} → ${escape(ch.to, 120)}"
                }
                is Change.MultiAdd -> {
                    val joined = ch.added.joinToString(", ") { escape(it, 60) }
                    lines += "**${ch.field}** +${truncate(joined, 200)}"
                }
                is Change.MultiRemove -> {
                    val joined = ch.removed.joinToString(", ") { escape(it, 60) }
                    lines += "**${ch.field}** -${truncate(joined, 200)}"
                }
            }
        }

        if (lines.isEmpty()) {
            lines += "Unknown changes"
        }
        return lines.joinToString("\n")
    }

    private fun escape(text: String, maxLen: Int): String {
        val cleaned = text
            .replace("\r", "")
            .replace("_", "\\_")
            .replace("*", "\\*")
            .replace("~", "\\~")
            .replace("`", "\\`")
        return truncate(cleaned, maxLen)
    }

    private fun truncate(s: String, maxLen: Int): String {
        if (s.length <= maxLen) return s
        val cut = min(s.length, maxLen)
        return s.take(cut).trimEnd() + "…"
    }
}

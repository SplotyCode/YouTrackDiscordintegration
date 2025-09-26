package de.scandurra.youtrackdiscord.notification

import de.scandurra.youtrackdiscord.client.ChangeEvent
import de.scandurra.youtrackdiscord.client.Issue
import de.scandurra.youtrackdiscord.client.NotificationMetadata
import de.scandurra.youtrackdiscord.notification.Changes.Mapper
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(Changes::class.java)

data class Changes (
    val issue: Issue,
    val changes: List<Change>
) {
    sealed class Change {
        data class Comment(val text: String) : Change()
        data class FieldChange(val field: String, val from: String, val to: String) : Change()
        data class FieldSet(val field: String, val to: String) : Change()
        data class MultiAdd(val field: String, val added: List<String>) : Change()
        data class MultiRemove(val field: String, val removed: List<String>) : Change()
    }

    fun format(): String {
        return buildString {
            append("\n")
            append("**${issue.id} in ${issue.project.name}**\n")
            changes.forEach { change ->
                appendLine(change.toString())
            }
            append("\n")
        }
    }

    object Mapper {
        fun convert(events: List<ChangeEvent>): List<Change> =
            events.flatMap { ev ->
                when (ev.category) {
                    "COMMENT" -> listOfNotNull(convertComment(ev))
                    "CUSTOM_FIELD" -> convertCustomField(ev)
                    else -> {
                        logger.warn("Unknown event category: {}", ev.category)
                        emptyList()
                    }
                }
            }

        private fun convertComment(ev: ChangeEvent): Change.Comment {
            val candidate = ev.addedValues.first().name
            return Change.Comment(candidate)
        }

        private fun convertCustomField(ev: ChangeEvent): List<Change> {
            val fieldName = ev.name
            val removed = ev.removedValues.map { it.name }.filter { it.isNotEmpty() }
            val added = ev.addedValues.map { it.name }.filter { it.isNotEmpty() }

            return if (ev.multiValue) {
                val out = mutableListOf<Change>()
                if (added.isNotEmpty()) out += Change.MultiAdd(fieldName, added)
                if (removed.isNotEmpty()) out += Change.MultiRemove(fieldName, removed)
                out
            } else if (removed.isEmpty()) {
                listOf(Change.FieldSet(fieldName, added.single()))
            } else {
                val from = removed.single()
                val to = added.single()
                listOf(Change.FieldChange(fieldName, from, to))
            }
        }
    }
}
fun NotificationMetadata.toChanges(): Changes {
    return Changes(issue, Mapper.convert(change.events))
}

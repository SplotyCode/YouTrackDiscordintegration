package de.scandurra.youtrackdiscord.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserNotificationRaw(
    val read: Boolean,
    val metadata: String,
    val content: String,
    val timestamp: Long,
    val id: String,
)

@Serializable
data class UserNotification(
    val read: Boolean,
    val timestamp: Long,
    val id: String,
    val metadata: NotificationMetadata,
    val content: String
)

@Serializable
data class NotificationMetadata(
    val type: String,
    val issue: Issue,
    val onlyViaDuplicate: Boolean,
    val lastNotification: Boolean,
    val initialNotification: Boolean,
    val change: Change,
    val reason: Reason,
    val header: String
)

@Serializable
data class Issue(
    val invisibleAttachmentCount: Int,
    val summary: String,
    val resolved: String?,
    val votes: Int,
    val project: Project,
    val entityId: String,
    val starred: Boolean,
    val created: Long,
    val description: String,
    val id: String,
    /* fields */
)

@Serializable
data class Project(
    val entityId: String,
    val name: String,
    val shortName: String
)

@Serializable
data class Change(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val humanReadableTimeStamp: String,
    val events: List<ChangeEvent>
)

@Serializable
data class ChangeEvent(
    val multiValue: Boolean,
    val removedValues: List<ChangeValue>,
    val addedValues: List<ChangeValue>,
    val activityId: String,
    val entityId: String,
    val category: String,
    val name: String
)

@Serializable
data class ChangeValue(
    val name: String,
    val entityId: String,
    val type: String
)

@Serializable
data class Reason(
    val savedSearchReasons: List<JsonElement> = emptyList(),
    val tagReasons: List<JsonElement> = emptyList(),
    val mentionReasons: List<JsonElement> = emptyList()
)

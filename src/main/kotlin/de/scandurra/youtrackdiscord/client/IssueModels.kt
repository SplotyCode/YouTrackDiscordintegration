package de.scandurra.youtrackdiscord.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminProject(
    val id: String,
    val shortName: String,
    val name: String
)

@Serializable
data class RefProject(
    val id: String? = null
)

@Serializable
data class CreateIssueRequest(
    val project: RefProject,
    val summary: String,
    val description: String? = null
)

@Serializable
data class CreatedIssue(
    val id: String,
    @SerialName("idReadable") val idReadable: String,
    val summary: String
)

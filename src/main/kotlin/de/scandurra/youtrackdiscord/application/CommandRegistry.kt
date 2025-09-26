package de.scandurra.youtrackdiscord.application

import de.scandurra.youtrackdiscord.client.YouTrackClient
import de.scandurra.youtrackdiscord.issue.CreateIssueCommandListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object CommandRegistry {
    fun registerAndSendCommands(jda: JDA, yt: YouTrackClient, baseUrl: String) {
        jda.updateCommands().addCommands(
            Commands.slash("yt-create", "Create a new YouTrack issue")
                .addOption(OptionType.STRING, "project", "Project key (shortName) or id", true)
                .addOption(OptionType.STRING, "summary", "Issue summary", true)
                .addOption(OptionType.STRING, "description", "Issue description", false)
        ).queue()
        jda.addEventListener(CreateIssueCommandListener(yt, baseUrl))
    }
}
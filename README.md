# YouTrack Discord Integration

A small Kotlin application that bridges YouTrack and Discord

## Features
- Polls your YouTrack unread notifications and sends them as Discord direct messages to a specific user.
- Provides a `/yt-create` slash command in Discord to create YouTrack issues
  - `project` (required): YouTrack project shortName/key
  - `summary` (required)
  - `description` (optional)

## Prerequisites
- A YouTrack instance (e.g., Cloud: `https://<your-domain>.youtrack.cloud`).
  - You can create one with just one click [here](https://www.jetbrains.com/de-de/youtrack/).
- A YouTrack Permanent Token for the user whose notifications should be read.
  - Follow [this](https://www.jetbrains.com/help/youtrack/server/manage-permanent-token.html#delete-permanent-token) guide you only need to grant the `YouTrack` scope
- A Discord Application with a Bot user and token.
  - Create a new application [here](https://discord.com/developers/applications?new_application=true)
  - Then go to `Install` and click the `Install Link`
- Your Discord user ID
  - Enable Developer Mode in Discord
  - Right-click your profile → Copy User ID.

## Configuration (Environment Variables)
Set the following environment variables before running:
- `YT_BASE_URL` your YouTrack base URL (e.g., `https://<your-domain>.youtrack.cloud`).
- `YT_TOKEN` YouTrack permanent token (starts with `perm-...`).
- `DISCORD_TOKEN` Discord Bot token from the Developer Portal.
- `DISCORD_USER_ID` The numeric Discord user ID to receive DMs.

## Build and Run

macOS / Linux:
```
export YT_BASE_URL="https://splotycode.youtrack.cloud"
export YT_TOKEN="perm-<your-token>"
export DISCORD_TOKEN="<your-discord-bot-token>"
export DISCORD_USER_ID="<your-discord-user-id>"
./gradlew run
```

Windows (PowerShell):
```
$env:YT_BASE_URL="https://splotycode.youtrack.cloud"
$env:YT_TOKEN="perm-<your-token>"
$env:DISCORD_TOKEN="<your-discord-bot-token>"
$env:DISCORD_USER_ID="<your-discord-user-id>"
./gradlew.bat run
```

## Notes and Limitations
- Dedupe is in-memory only. If you restart the app, still-unread notifications may be sent again.
- Notifications are not marked as read in YouTrack by this tool (I was not able to find an API endpoint for this).
- The notification polling interval is currently fixed at 10 minutes.

## Troubleshooting
- Slash command is not visible:
  - Ensure the bot is invited with `applications.commands` scope
  - Restart discord (not just closing it)
- No DMs received:
  - Ensure the token has permissions to read user notifications
  - Check stderr for errors

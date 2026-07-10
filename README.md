# SpiritChat

SpiritChat is a Paper chat formatting plugin for modern Minecraft servers. It focuses on predictable chat formatting, player mentions, optional held-item showcases, unsigned-chat compatibility, and database-backed moderation logs.

[Stable Releases](https://modrinth.com/plugin/spiritchat)
[Dev Builds](https://ci.moonrise.gg/job/spiritchat/)

## Platform

- Paper 1.21.8+
- Java 21+
- PlaceholderAPI
- SQLite by default
- Optional external SQL: MySQL, MariaDB, PostgreSQL
- Optional LuckPerms integration for group-based chat formats

## Features

- Static chat formatting
- LuckPerms group chat formatting
- PlaceholderAPI placeholders in configured formats and messages
- Legacy `&` chat colors with safe MiniMessage sanitization
- `@PlayerName` mention highlights and ping notifications
- Per-player mention ping toggles
- Held-item chat showcase placeholders: `<i>` and `<item>`
- Chat-signing compatibility for modern clients and report-disabling client mods
- Optional database-backed chat logs with paginated lookup commands
- Configurable player-facing messages in `messages.yml`

## Commands

- `/chat` or `/spiritchat` - show help
- `/chat mentions <on|off>` - toggle personal mention pings
- `/chat logs recent [page]` - show recent chat logs
- `/chat logs player <name> [page]` - show logs for a player name
- `/chat logs uuid <uuid> [page]` - show logs for a player UUID
- `/chat reload` - reload configuration and reconnect storage

## Permissions

- `spiritchat.admin` - access reload and chat log commands
- `spiritchat.chat-colors` - use legacy `&` colors in chat
- `spiritchat.chatitem` - use held-item showcase placeholders

## Configuration

SpiritChat creates:

- `config.yml` - formatting, mentions, chat item, unsigned-chat, logging, and storage settings
- `messages.yml` - all player-facing command and notification messages

SQLite is used by default and stores data in the plugin folder. External SQL databases can be configured in `config.yml`; external pool sizes are clamped to a safe range.

Chat item placeholders are configurable, but they should not reuse reserved chat format placeholders such as `<message>`, `<player>`, or `<mention>`.

## Chat Signing

SpiritChat formats chat by modifying the message before it reaches players. On modern Minecraft versions, modified signed chat can be rejected by clients or shown as reportable when it no longer matches the original signature.

By default SpiritChat:

- rewrites outgoing player chat as system chat so formatted messages are not reportable
- advertises `preventsChatReports=true` in the server status response for clients that understand report-prevention metadata
- leaves `claim-secure-chat-enforced` disabled

SpiritChat cannot override Paper's secure-profile enforcement. If players use clients that do not send chat signatures at all, the server must still allow unsigned profiles in `server.properties`:

```properties
enforce-secure-profile=false
```

The related settings are in `config.yml` under `chat.anti-message-signing-settings`.

## Building

```bash
./gradlew clean build
```

The Paper jar is written to:

```txt
build/SpiritChat-Paper.jar
```

## Testing

```bash
./gradlew :platform-paper:test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) before opening issues or pull requests.

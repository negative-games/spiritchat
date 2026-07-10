# SpiritChat

SpiritChat is a Paper chat formatting plugin for modern Minecraft servers that need flexible formatting without making chat setup painful.

## At a Glance

- Static and LuckPerms group-based chat formats
- PlaceholderAPI placeholders in configured formats and messages
- Safe legacy `&` chat colors
- Player mention highlights and ping notifications
- Per-player mention ping toggles
- Held-item showcase placeholders
- Optional database-backed moderation logs
- Chat-signing compatibility for modern clients and report-disabling client mods

## Dependencies

- Paper 1.21.8+
- Java 21+
- PlaceholderAPI

## Optional Dependencies

- LuckPerms for group-based chat formats

## Main Features

SpiritChat can format all chat with one static format or select formats from the sender's LuckPerms group. Player-entered MiniMessage tags are sanitized so formatting placeholders cannot be injected through chat text.

Mentions support `@PlayerName` highlighting, action bar notifications, sounds, and per-player ping preferences.

Chat item showcases let players display their held item with `<i>` or `<item>` when they have permission. Escaped placeholders are left alone, and reserved format placeholders such as `<message>`, `<player>`, and `<mention>` are not treated as chat item placeholders.

Chat logs can be stored in SQLite, MySQL, MariaDB, or PostgreSQL and retrieved with paginated commands.

## Commands

- `/chat`
- `/chat mentions <on|off>`
- `/chat logs recent [page]`
- `/chat logs player <name> [page]`
- `/chat logs uuid <uuid> [page]`
- `/chat reload`

`/spiritchat` can be used anywhere `/chat` is shown.

## Permissions

- `spiritchat.admin`
- `spiritchat.chat-colors`
- `spiritchat.chatitem`

## Notes

SpiritChat includes chat-signing compatibility for servers that format chat on modern Minecraft versions. By default it rewrites outgoing player chat as unreportable system chat and advertises `preventsChatReports=true` for clients that understand report-prevention metadata.

SpiritChat cannot override Paper's secure-profile enforcement. If players use clients that do not send chat signatures at all, `enforce-secure-profile=false` must be set in `server.properties`.

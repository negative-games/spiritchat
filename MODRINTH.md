# SpiritChat

**SpiritChat** is a Paper chat formatting plugin for servers that want clean, configurable chat without a painful setup process.

SpiritChat is open source and currently in beta. The core chat formatting, mentions, chat item showcases, and logging features are usable today, while larger social and cosmetic systems are planned as the plugin grows.

## At a Glance

- Static server-wide chat formatting
- Optional LuckPerms group-based chat formats
- PlaceholderAPI support in configured formats and messages
- Safe legacy `&` chat colors for players with permission
- `@PlayerName` mention highlights, sounds, and action bar alerts
- Database-backed per-player mention ping preferences
- Held-item showcase placeholders
- Optional database-backed chat logs with paginated lookup commands
- Separate `config.yml` and `messages.yml`
- Chat-signing compatibility for modern Minecraft clients

## Dependencies

- **Paper 1.21.8+**
- **Java 21+**
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** - required placeholder support

## Optional Dependencies

- **[LuckPerms](https://luckperms.net/)** - optional group-based chat formats

## Main Features

### Chat Formatting

SpiritChat can use one static chat format for the whole server, or select a format from the sender's LuckPerms groups.

![Static chat format](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/static-chat.png)

![LuckPerms group chat format](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/group-chat.png)

<details>
<summary>Format Options</summary>

Static chat is the default mode and takes priority when it is enabled. Group chat can be enabled when different ranks or groups should have different chat styles.

Configured formats use MiniMessage placeholders such as:

- `<player>`
- `<message>`

Player-entered MiniMessage tags are stripped from normal chat text so players cannot inject format placeholders or arbitrary MiniMessage styling through their own messages.

Players with `spiritchat.chat-colors` can use legacy `&` color codes in chat.

</details>

### Mentions

Mention another online player with `@PlayerName` to highlight their name and notify them.

![Mention highlight and action bar notification](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/mentions.png)

<details>
<summary>Mention Options</summary>

Mentions can:

- Highlight the matched player name in chat
- Play a configurable sound
- Send an action bar notification
- Let players toggle their own pings with `/chat mentions <on|off>`

Player mention preferences are stored in the configured database. SQLite is used by default.

</details>

### Chat Items

Players can show their held item in chat with configured placeholders.

![Held item showcase with hover details](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/chat-item.png)

<details>
<summary>Chat Item Placeholders</summary>

Default placeholders:

- `<i>`
- `<item>`

Players need `spiritchat.chatitem` to use chat item showcases.

Escaped placeholders, such as `\<item>`, are left as normal text. Empty-hand materials are blocked by default, and reserved placeholders such as `<message>`, `<player>`, and `<mention>` cannot be used as chat item placeholders.

</details>

### Chat Logs

SpiritChat can store chat logs for staff review.

![Paginated chat log lookup](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/chat-logs.png)

<details>
<summary>Log Storage and Lookup</summary>

Chat logs are optional and disabled by default. When enabled, SpiritChat stores plain-text chat rows in the configured database.

Supported storage types:

- SQLite
- MySQL
- MariaDB
- PostgreSQL

Stored log rows include:

- Sender UUID
- Sender name
- Message
- World
- Server time

Staff can query recent logs, logs by player name, or logs by UUID with paginated commands.

</details>

### Configuration

SpiritChat keeps configuration split by purpose.

![Configurable chat formatting](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/config-formatting.png)

<details>
<summary>Configuration Files</summary>

`config.yml` controls:

- Chat formatting
- Mentions
- Chat item placeholders
- Chat-signing compatibility
- Chat logs
- Storage

`messages.yml` controls player-facing messages. Messages can use the global `<prefix>` placeholder when they should include the configured prefix.

</details>

### Roadmap

SpiritChat is planned to grow into a larger chat-management plugin.

<details>
<summary>Planned Cosmetic Features</summary>

Planned chat color options include:

- Hex colors
- Legacy colors
- Gradients
- Presets
- Player-made presets

Name colors are planned with the same style options, giving servers more room for rank perks, cosmetics, and player identity.

</details>

## Commands

```txt
/chat
/chat help
/chat mentions <on|off>
/chat logs recent [page]
/chat logs player <name> [page]
/chat logs uuid <uuid> [page]
/chat reload
/spiritchat
/spiritchat help
/spiritchat mentions <on|off>
/spiritchat logs recent [page]
/spiritchat logs player <name> [page]
/spiritchat logs uuid <uuid> [page]
/spiritchat reload
```

## Permissions

```txt
spiritchat.admin
spiritchat.chat-colors
spiritchat.chatitem
```

## Notes

SpiritChat includes compatibility options for modern signed-chat clients. By default, it rewrites formatted outgoing player chat as unreportable system chat and advertises report prevention in the server list status response for clients that understand it.

SpiritChat cannot override Paper's secure-profile enforcement. If players use clients that do not send profile public keys or chat signatures, set `enforce-secure-profile=false` in `server.properties` and restart the server.

Because SpiritChat is still in beta, test new releases and config changes on a staging server before rolling them out to a live network.

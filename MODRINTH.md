# SpiritChat

**SpiritChat** makes Minecraft chat look cleaner, feel more alive, and stay easier to manage.

It gives you the basics a server chat plugin should have without making setup painful: good-looking formats, rank-based styles, player mentions, item showcases, and useful staff tools. SpiritChat is open source, currently in beta, and growing into a wider chat and cosmetic system.

## What It Does

- Give your server a clean chat style out of the box
- Make ranks stand out with LuckPerms group formats
- Use PlaceholderAPI to show names, ranks, prefixes, stats, and more
- Let trusted players use legacy `&` colors
- Ping players with `@PlayerName` mentions
- Let players turn their own mention pings on or off
- Show held items directly in chat
- Keep optional chat logs for staff
- Customize the wording players see
- Reduce modern signed-chat headaches

## Chat That Matches Your Server

Use one simple format for everyone, or give different groups their own look. Staff, VIPs, members, and special ranks can all have chat styles that fit their role on your server.

![Static chat format](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/static-chat.png)

![LuckPerms group chat format](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/group-chat.png)

<details>
<summary>More about chat formatting</summary>

You can keep chat simple with one format for everyone, or use LuckPerms groups when ranks should look different.

Formats can use placeholders like:

- `<player>`
- `<message>`

You can also allow trusted players to use legacy `&` color codes in chat.

</details>

## Mentions Players Notice

When someone types `@PlayerName`, SpiritChat can highlight the mention, play a sound, and show an action bar message. It helps busy chats feel more responsive without needing a full social plugin.

![Mention highlight and action bar notification](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/mentions.png)

<details>
<summary>More about mentions</summary>

Mentions can:

- Highlight the player name in chat
- Play a sound
- Send an action bar notification
- Be toggled by players with `/chat mentions <on|off>`

Player mention preferences are saved between restarts.

</details>

## Show Items In Chat

Players can show what they are holding directly in chat. It is useful for trading, showing off gear, sharing loot, or just making chat feel a little more interactive.

![Held item showcase with hover details](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/chat-item.png)

<details>
<summary>More about item showcases</summary>

Default item placeholders:

- `<i>`
- `<item>`

Players need `spiritchat.chatitem` to use item showcases.

Escaped placeholders, such as `\<item>`, are sent as normal text. Empty-hand items are blocked by default.

</details>

## Staff Chat Logs

If chat logs are enabled, staff can look up recent messages in game with paginated commands. It is a quicker way to check context when chat moves fast.

![Paginated chat log lookup](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/chat-logs.png)

<details>
<summary>More about chat logs</summary>

Chat logs are optional and disabled by default, so you only use them if they make sense for your server.

Storage options:

- SQLite
- MySQL
- MariaDB
- PostgreSQL

Staff can look up recent messages, search by player name, or search by UUID.

</details>

## Easy To Shape Around Your Server

Change the chat style, mention alerts, item showcase words, staff messages, and plugin prefix so SpiritChat feels like part of your server, not a bolted-on extra.

![Configurable chat formatting](https://raw.githubusercontent.com/moonrise-studios/spiritchat/revision/v1/docs/modrinth/config-formatting.png)

<details>
<summary>What can be customized?</summary>

You can customize:

- Chat formats
- Mention alerts
- Item showcase placeholders
- Chat logs
- Storage
- Modern chat compatibility
- Player-facing messages

</details>

## Roadmap

SpiritChat is starting with the core chat experience first. From there, the plan is to grow into more cosmetic and interactive chat features.

Planned additions include:

- Chat colors with hex, legacy colors, gradients, presets, and player-made presets
- Name colors with the same style options
- Chat games

Follow development, report issues, or contribute on [GitHub](https://github.com/moonrise-studios/spiritchat).

## Requirements

- **Paper 1.21.8+**
- **Java 21+**
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)** is required
- **[LuckPerms](https://luckperms.net/)** is optional for group chat formats

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

## Modern Chat Support

Modern Minecraft chat can be awkward when a plugin changes how messages look. SpiritChat includes compatibility options to help formatted chat behave properly for modern clients.

There is one server setting to know about: SpiritChat cannot override Paper's secure-profile enforcement. If players use clients that do not send profile public keys or chat signatures, set `enforce-secure-profile=false` in `server.properties` and restart the server.

## Beta Note

SpiritChat is still in beta. Test new releases and config changes on a staging server before rolling them out to a live network.

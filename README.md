# SpiritChat

[![Modrinth](https://img.shields.io/modrinth/v/spiritchat?logo=modrinth&label=Modrinth&color=00AF5C)](https://modrinth.com/plugin/spiritchat)
[![Modrinth downloads](https://img.shields.io/modrinth/dt/spiritchat?logo=modrinth&label=Downloads&color=00AF5C)](https://modrinth.com/plugin/spiritchat)

**SpiritChat** is a beta chat formatting plugin for Paper servers, built to make chat setup simple, flexible, and easy to grow with your server.

SpiritChat currently focuses on reliable chat formatting fundamentals: static formats, LuckPerms group formats, chat item display, PlaceholderAPI support, update notifications, and simple admin workflows.

## Links

- Stable releases: [Modrinth](https://modrinth.com/plugin/spiritchat)
- Dev builds: [Moonrise CI](https://ci.moonrise.gg/job/spiritchat/)
- Issues and feature requests: use the GitHub issue templates
- Contributing: see [CONTRIBUTING.md](CONTRIBUTING.md)

## Supported Platform

- Loader: Paper
- Minecraft version: currently built against Paper `1.21.4`
- Java: 21

LuckPerms and PlaceholderAPI are optional integrations.

## Features

- Static chat formatting for one server-wide chat format
- Group chat formatting using LuckPerms group context
- Chat item formatting with `{i}` and `{item}` placeholders
- PlaceholderAPI support in chat formats
- Reload command for applying configuration changes
- Update notifications for operators with permission
- Optional bStats metrics

## Dependencies

### Required

- Paper-compatible Minecraft server

### Optional

- [LuckPerms](https://luckperms.net/) for group-based chat formatting
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholder integration

## Building

Use Java 21.

```bash
./gradlew build
```

The shaded plugin jar is written to:

```txt
build/libs/
```

If the Gradle wrapper is not executable on your machine:

```bash
chmod +x ./gradlew
```

## Testing

Run the unit test suite with:

```bash
./gradlew :server:test
```

Pull requests should include tests for behavior changes when the code can be tested without a live server.

## Commands

```txt
/spiritchat
/spiritchat reload
```

## Permissions

```txt
spiritchat.admin
spiritchat.updates
spiritchat.chat-colors
spiritchat.chat-item
```

## Contributing

Community contributions are welcome. The default branch is protected, so all changes should go through a pull request.

Before opening a pull request:

1. Create a branch from the latest default branch.
2. Keep the change focused.
3. Run `./gradlew :server:test`.
4. Update documentation when behavior, commands, permissions, or configuration changes.
5. Fill out the pull request description with what changed and how it was tested.

For more detail, see [CONTRIBUTING.md](CONTRIBUTING.md).

# SpiritChat 0.3.0 Beta

SpiritChat `0.3.0` is a beta release for Paper servers. This release focuses on project setup, publishing automation, documentation, and validation around the current stable chat formatting feature set.

Because this is a beta, test the plugin on a staging server before using it in production.

## What's Changed

- Added GitHub Actions test workflow
- Added Modrinth publishing workflow
- Added Modrinth description sync workflow
- Added GitHub issue templates for bug reports and feature requests
- Added JUnit test setup for the current config defaults
- Refreshed README, contribution guide, and Modrinth description
- Documented LuckPerms group-weight behavior for group chat formats
- Bumped plugin version to `0.3.0`

## Compatibility

- Server platform: Paper
- Minecraft version: `1.21.4`
- Java version: 21

## Optional Integrations

- LuckPerms for group-based chat formatting
- PlaceholderAPI for placeholders in chat formats

## Testing Notes

Validated with:

```bash
./gradlew clean build
```

Recommended server checks:

- Plugin starts without console errors
- `/spiritchat` shows help
- `/spiritchat reload` reloads configuration
- Static chat formatting works
- Group chat formatting works when `use-static-format` is `false`
- LuckPerms group weights are set so the intended group format wins
- Chat item placeholders `{i}` and `{item}` work for players with `spiritchat.chat-item`

## Known Notes

- Group chat format selection follows inherited LuckPerms groups by weight, highest to lowest.
- If `default` wins instead of `admin`, raise the `admin` group weight above `default`.
- Planned social, staff chat, network, and monitoring systems are not included in this beta.

## Publishing

Mark this GitHub release as a **pre-release**. The Modrinth workflow maps GitHub prereleases to Modrinth `beta` versions.

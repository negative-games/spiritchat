# Contributing to SpiritChat

Thanks for helping improve SpiritChat. This project is open source because community reports, testing, and patches make the plugin better for real servers.

## Workflow

The default branch is protected. Do not work directly on it and do not expect direct pushes to be accepted.

1. Fork or branch from the latest default branch.
2. Make one focused change per pull request.
3. Add or update tests when practical.
4. Run the test suite locally.
5. Open a pull request against the default branch.

## Development Requirements

- Java 21
- Gradle wrapper from this repository

Useful commands:

```bash
./gradlew :server:test
./gradlew build
```

If your local checkout does not have an executable Gradle wrapper:

```bash
chmod +x ./gradlew
```

## Pull Request Expectations

Pull requests should include:

- A clear summary of the change
- Any relevant issue number
- Notes on testing performed
- Documentation updates for changed commands, permissions, configuration, or supported behavior

Keep changes scoped. Avoid unrelated formatting, dependency, or refactor changes in the same pull request.

## Issues

Use the GitHub issue templates for bug reports and feature requests. For bugs, include:

- Paper and Minecraft version
- SpiritChat version or build
- LuckPerms and PlaceholderAPI versions when relevant
- Relevant configuration
- Steps to reproduce
- Any errors from the console

## Code Style

Follow the style already used in the repository. Prefer simple, direct changes over broad abstractions unless the abstraction removes real duplication or risk.

## Testing Notes

Some plugin behavior requires a Paper server to verify fully. When a change cannot be covered by unit tests, describe the manual server testing you performed in the pull request.

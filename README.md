 [!WARNING]
> **This project is unmaintained, untested, and provided as-is.**

## ⚠️ Disclaimer

This repository contains an experimental framework for **Hytale**. Please read the following before using it:

- **Untested:** The code has not been tested in any meaningful way. It may contain bugs, incomplete features, or behavior that breaks without warning.
- **Unmaintained:** This project is no longer actively developed. Issues and pull requests will likely go unanswered, and no updates, fixes, or compatibility patches are planned.
- **No compatibility guarantees:** Future Hytale updates may break this framework entirely.
- **Why it's abandoned:** Given Hytale's currently small player base, I've decided not to invest further time in this project. That may change if the community grows, but there are no promises.

### Use at your own risk

This software is provided **"as is", without warranty of any kind**, express or implied. The author is not responsible for any damage, data loss, server issues, or other problems resulting from its use.

### Want to continue it?

You're welcome to **fork** this repository and build on it, within the terms of the license. If you create an actively maintained fork, feel free to open an issue so it can be linked here.

# AetherCore

AetherCore is a server-side plugin/framework for [Hytale](https://hytale.com) that gives admins a native, in-game control panel plus the permissions, chat, moderation and localization systems behind it — one `/aether` command opens a full Custom UI admin interface, no external web dashboard required.

> **Status: pre-`v0.1`, actively developed.** Core modules (permissions, chat, players/moderation, localization, the admin UI) are functional and used daily on a local test server, but the project hasn't had a dedicated QA pass yet and APIs may still shift. See [Roadmap](#roadmap) below.

## What it does

- **A native admin panel, not a website.** `/aether` opens a Custom UI page built entirely on Hytale's own UI framework — sidebar tabs for Overview, Players, Permissions, Chat, Lang, and Admin Tools, with searchable/sortable/paginated tables, popups, and live notifications.
- **Permissions with real group inheritance.** Groups can inherit from multiple parent groups, permissions support wildcard nodes (`a.b.*`) and explicit negation (`-a.b.c`) with correct specific-before-general precedence, and grants can carry per-node world/expiry restrictions.
- **Chat formatting per group or per player**, with configurable join/leave/first-join broadcast messages and a live colored preview while editing.
- **Moderation**: ban, temp-ban, mute, temp-mute, freeze and kick, each with a full history log (who, when, reason, revoked by) browsable from the Players tab.
- **Personal Admin Tools**: vanish, gamemode (Adventure/Creative), Spectator, FreeCam, and independent walk/fly speed controls — self-service, applied instantly, and reapplied automatically on rejoin.
- **Fully localizable UI.** Every label in the admin panel — not just chat/command output — is resolved through the same Lang system, editable live from the Lang tab or via `/aether lang`, per admin language.
- **SQLite or MySQL**, selected in config, behind the same storage interfaces either way.

## Requirements

- Java 25
- Maven
- A Hytale server, API version `0.6.2`

## Building

```bash
mvn clean package
```

The plugin jar is produced under `target/AetherCore.jar` — copy it into your server's `mods/` folder.

## Configuration

AetherCore reads a single `config.json` from its plugin data folder. Relevant top-level fields:

```jsonc
{
  "pluginLang": "en-US",           // default UI/message language
  "dateTimeFormat": "yyyy-MM-dd HH:mm:ss",
  "storage": {
    "enabled": true,
    "type": "sqlite",              // "sqlite" or "mysql"
    "database": {
      "database": "aethercore",
      "username": "root",
      "password": "root",
      "address": "localhost:3306"  // used for mysql only
    }
  }
}
```

Each module can additionally carry its own config slice under `modules.<name>` in the same file.

## Commands

Opening the panel:

```
/aether                  opens the admin UI
```

Everything else nests under `/aether` as a subcommand, so the CLI mirrors what's in the panel:

```
/aether permissions group  <list|create|delete|add|remove|grant|children|default|prefix|suffix|priority>
/aether permissions user   <list|add|remove|grant|group>
/aether permissions list

/aether chat group  <format|server-join|player-join|leave|first-join|broadcast-first-join>
/aether chat player <format|server-join|player-join|leave|first-join|broadcast-first-join>

/aether lang lang    <add|remove|desc>
/aether lang message <add|set|remove|desc>
/aether lang list
```

Moderation ships as top-level commands rather than nested under `/aether`, matching where admins expect them:

```
/ban /tempban /unban
/mute /tempmute /unmute
/freeze /unfreeze
/kick
```

## Project layout

```
modules/
  lang/         translated messages + languages, backing every user-visible string
  player/       player records, moderation, and the Admin Tools sub-module
  permissions/  groups, direct grants, inheritance, world/expiry metadata
  chat/         per-group/per-player chat formatting and broadcast messages
ui/
  framework/    the Custom UI component model (AEUIComponent, AEUIPage, refresh/history)
  components/   reusable widgets: tables, popups, buttons, cards, inputs
  features/     one package per admin-panel tab
```
## License

Not yet decided — a `LICENSE` file will be added before this repository is treated as open for reuse. Until then, please don't redistribute without asking.

# PreppyLevels

<div align="center">

![Version](https://img.shields.io/badge/version-1.0--SNAPSHOT-blue)
![Minecraft](https://img.shields.io/badge/minecraft-1.20.4-green)
![Java](https://img.shields.io/badge/java-17-orange)
![License](https://img.shields.io/badge/license-MIT-green)

A powerful, feature-rich leveling plugin for Minecraft servers with customizable XP systems, multiple storage backends, and extensive API support.

[Features](#-features) • [Installation](#-installation) • [Configuration](#-configuration) • [API](#-api) • [Placeholders](#-placeholders) • [Building](#-building)

</div>

---

## ✨ Features

- **🎯 Customizable XP System** - Define XP requirements per level with flexible configuration
- **💾 Multiple Storage Backends** - Support for MySQL, H2, SQLite, YAML, and JSON storage
- **🤖 Automatic XP Gain** - Award XP for chat messages, commands, playtime, and server joins
- **📊 Real-time XP Bar** - Visual XP progress bar with configurable update intervals
- **🔊 Level Up Effects** - Sound effects and colored messages when players level up
- **🔌 PlaceholderAPI Integration** - Full support for placeholders in TAB, scoreboards, and more
- **⚡ Asynchronous API** - High-performance async API for other plugins
- **🎨 Beautiful Messages** - Color-coded messages with Adventure API support
- **📈 Progress Tracking** - Track level progress, XP needed, and more

## 📋 Requirements

- **Minecraft Server**: Paper/Spigot 1.20.4 or higher
- **Java**: 17 or higher
- **Optional**: PlaceholderAPI (for placeholders in TAB and other plugins)

## 🚀 Installation

1. Download the latest `PreppyLevels.jar` from the [Releases](https://github.com/jsemolik/PreppyLevels/releases) page
2. Place the JAR file in your server's `plugins` folder
3. Start or restart your server
4. Configure the plugin in `plugins/PreppyLevels/config.yml`
5. Reload the plugin with `/reload` or restart your server

## ⚙️ Configuration

The plugin will generate a default `config.yml` file on first run. Here's what you can configure:

### Storage Backend

Choose from 5 storage backends:

```yaml
# Options: MYSQL, H2, SQLITE, YAML, JSON
storage-type: H2
```

**H2** (Default) - Fast, file-based database, perfect for small to medium servers
**SQLite** - Lightweight, file-based database
**MySQL** - For large servers or multi-server setups
**YAML** - Simple file-based storage, easy to edit manually
**JSON** - Human-readable JSON format

### XP Requirements

Define custom XP requirements for each level:

```yaml
xp-requirements:
  1: 100
  2: 200
  3: 300
  4: 400
  5: 500
  # After level 5, uses default-xp-increment

default-xp-increment: 100  # Used when level not specified
```

### Automatic XP Gain

Configure automatic XP rewards:

```yaml
auto-xp:
  enabled: true
  tasks:
    chat: 1          # XP per chat message
    time-played: 5   # XP per minute played
    command: 2       # XP per command executed
    join: 10         # XP on server join
```

### XP Bar Settings

```yaml
xp-bar:
  update-interval: 20  # Update interval in ticks (20 = 1 second)
  show-level: true     # Show level number in XP bar
```

## 📝 Commands

| Command | Aliases | Description |
|---------|---------|-------------|
| `/level` | `/levels`, `/lvl` | View your current level, XP, and progress |

## 🎯 Placeholders

PreppyLevels integrates with PlaceholderAPI. Install PlaceholderAPI to use these placeholders in TAB, scoreboards, and other plugins.

### Available Placeholders

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%preppylevels_level%` | Player's current level | `5` |
| `%preppylevels_xp%` | Player's total XP | `1250` |
| `%preppylevels_xpneeded%` | XP needed for next level | `150` |
| `%preppylevels_nextlevel%` | Next level number | `6` |
| `%preppylevels_levelprogress%` | Level progress percentage | `75.5%` |

### Alternative Formats

- `%preppylevels_xp_needed%` or `%preppylevels_xp-needed%` (same as `xpneeded`)
- `%preppylevels_totalxp%` (same as `xp`)
- `%preppylevels_next_level%` or `%preppylevels_next-level%` (same as `nextlevel`)

### Usage Example (TAB Scoreboard)

```yaml
Level: %preppylevels_level%
XP Needed: %preppylevels_xpneeded%
Progress: %preppylevels_levelprogress%
```

## 🔌 API

PreppyLevels provides a comprehensive API for other plugins to interact with the leveling system.

### Getting the API

```java
PreppyLevelsAPI api = PreppyLevels.getAPIInstance();
```

### API Methods

#### Give XP to a Player

```java
UUID playerId = player.getUniqueId();
String playerName = player.getName();
long xpAmount = 100;

api.giveXp(playerId, playerName, xpAmount).thenRun(() -> {
    // XP given successfully
});
```

#### Get Player Level

```java
api.getLevel(playerId).thenAccept(level -> {
    player.sendMessage("Your level: " + level);
});
```

#### Get Player XP

```java
api.getXp(playerId).thenAccept(xp -> {
    player.sendMessage("Your total XP: " + xp);
});
```

#### Get XP Needed for Next Level

```java
api.getXpNeededForNextLevel(playerId).thenAccept(xpNeeded -> {
    player.sendMessage("You need " + xpNeeded + " more XP!");
});
```

### Full API Reference

```java
public class PreppyLevelsAPI {
    // Give XP to a player
    CompletableFuture<Void> giveXp(UUID playerId, String playerName, long xpAmount);
    
    // Get a player's current level
    CompletableFuture<Integer> getLevel(UUID playerId);
    
    // Get a player's total XP
    CompletableFuture<Long> getXp(UUID playerId);
    
    // Get XP needed for next level
    CompletableFuture<Long> getXpNeededForNextLevel(UUID playerId);
}
```

**Note**: All API methods return `CompletableFuture` for asynchronous operations. Use `.thenAccept()`, `.thenRun()`, or `.get()` to handle results.

## 💾 Storage Backends

### MySQL

Perfect for large servers or multi-server setups:

```yaml
storage-type: MYSQL
mysql:
  host: localhost
  port: 3306
  database: preppylevels
  username: root
  password: yourpassword
  pool-size: 10
```

### H2 (Default)

Fast, file-based database. Great for most servers:

```yaml
storage-type: H2
h2:
  file: preppylevels.db
```

### SQLite

Lightweight alternative to H2:

```yaml
storage-type: SQLITE
sqlite:
  file: preppylevels.db
```

### YAML

Simple file-based storage, easy to edit manually:

```yaml
storage-type: YAML
# Files stored in plugins/PreppyLevels/players/
```

### JSON

Human-readable JSON format:

```yaml
storage-type: JSON
# Files stored in plugins/PreppyLevels/players/
```

## 🛠️ Building from Source

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/jsemolik/PreppyLevels.git
cd PreppyLevels
```

2. Build with Maven:
```bash
mvn clean package
```

3. The compiled JAR will be in `target/PreppyLevels-1.0-SNAPSHOT.jar`

### Building with IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Open the Maven tool window (View → Tool Windows → Maven)
3. Expand your project → Lifecycle
4. Run `clean`, then `package`
5. Use the JAR from `target/PreppyLevels-1.0-SNAPSHOT.jar`

**Note**: The Maven build uses the `maven-shade-plugin` to bundle all dependencies into the final JAR.

## 📦 Dependencies

- **Paper API** 1.20.4 (provided)
- **H2 Database** 2.2.224
- **MySQL Connector** 8.2.0
- **SQLite JDBC** 3.44.1.0
- **HikariCP** 5.1.0 (connection pooling)
- **SnakeYAML** 2.2
- **Gson** 2.10.1
- **PlaceholderAPI** 2.11.5 (optional, provided)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feat/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feat/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Test your changes thoroughly
- Ensure the code compiles without errors

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [COPYING](COPYING) file for details.

## 👤 Author

**Oliver Steiner**

- GitHub: [@jsemolik](https://github.com/jsemolik)

## 🙏 Acknowledgments

- Built for Paper/Spigot servers
- Uses Adventure API for modern text components
- Integrates with PlaceholderAPI for maximum compatibility

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/jsemolik/PreppyLevels/issues)
- **Discussions**: [GitHub Discussions](https://github.com/jsemolik/PreppyLevels/discussions)

---

<div align="center">

Made with ❤️ for the Minecraft community

⭐ Star this repo if you find it useful!

</div>


# PreppyLevels

A leveling plugin for Velocity proxy with customizable XP system, multiple storage backends, and API support for other plugins.

## Features

- **Customizable XP System**: Configure XP requirements per level in `config.yml`
- **Multiple Storage Backends**: Support for MySQL, H2, SQLite, YAML, and JSON
- **Automatic XP Gain**: Configurable automatic XP for tasks like chatting, time played, commands, and server joins
- **Plugin API**: Other plugins can trigger XP gains through the API
- **Level Display**: Player levels displayed in chat messages
- **Database Persistence**: Player levels and XP are saved persistently

## Important: XP Bar Display

**The XP bar display requires the plugin to be installed on backend servers, not just the proxy.**

The Minecraft XP bar is controlled by the game server the player is connected to. While this plugin runs on the Velocity proxy and can track XP/levels, the actual XP bar display must be updated by the backend server.

### Options:

1. **Backend Plugin (Recommended)**: Create a lightweight backend plugin that:
   - Receives level/XP data from the proxy via plugin messaging
   - Updates the XP bar using the server's API
   - Can be installed on all backend servers

2. **Proxy-Only**: The plugin will still work for:
   - Tracking XP and levels
   - Sending chat messages
   - API functionality
   - But the XP bar won't be displayed until a backend component is added

## Installation

1. Place the plugin JAR in your Velocity proxy's `plugins` folder
2. Start the proxy to generate the default `config.yml`
3. Configure your storage backend and XP requirements in `config.yml`
4. Restart the proxy

## Configuration

Edit `config.yml` to customize:

- **Storage Type**: Choose between MYSQL, H2, SQLITE, YAML, or JSON
- **XP Requirements**: Set XP needed for each level
- **Auto XP**: Configure automatic XP gain for various tasks
- **Messages**: Customize chat messages for XP gains and level ups

## API Usage

Other plugins can interact with PreppyLevels:

```java
PreppyLevelsAPI api = // Get API instance from your plugin

// Give XP to a player
api.giveXp(playerId, playerName, 100);

// Get player level
api.getLevel(playerId).thenAccept(level -> {
    // Use level
});

// Get player XP
api.getXp(playerId).thenAccept(xp -> {
    // Use XP
});
```

## Storage Backends

### MySQL
Configure connection details in `config.yml` under the `mysql` section.

### H2 (Default)
Embedded database, no additional setup required. Database file location configured in `config.yml`.

### SQLite
File-based database, no additional setup required. Database file location configured in `config.yml`.

### YAML
Stores player data in YAML files. Files are stored in `plugins/PreppyLevels/players/`.

### JSON
Stores player data in JSON files. Files are stored in `plugins/PreppyLevels/players/`.

## Building

```bash
mvn clean package
```

The compiled JAR will be in the `target` directory.

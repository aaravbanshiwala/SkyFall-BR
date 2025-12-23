# SkyFallBR - Battle Royale Plugin

A comprehensive Minecraft battle royale plugin built with Kotlin, featuring multiple game modes, cosmetics, achievements, and extensive customization options.

## Features

- **Multiple Game Modes**: Solo, Duos, Squads, Limited Time Modes (LTM), and 50v50
- **Dynamic Loot System**: Configurable loot tables with rarity tiers
- **Cosmetics System**: Emotes, trails, weapon skins, death effects, and kill messages
- **Achievement System**: Track player progress and unlock rewards
- **Zone Mechanics**: Shrinking safe zones with customizable speeds
- **Building System**: Allow players to build bases and structures
- **Supply Drops**: Automated loot crates with legendary items
- **Statistics Tracking**: Comprehensive player stats and leaderboards
- **Party System**: Team up with friends for coordinated gameplay

## Installation

### Requirements
- Minecraft 1.21+
- Spigot/Paper server
- Java 17+

### Steps
1. Download the latest `SkyFallBR.jar` from releases
2. Place the jar file in your server's `plugins/` directory
3. Start/restart your server to generate configuration files
4. Configure settings in `plugins/SkyFallBR/config.yml`
5. Customize messages in `plugins/SkyFallBR/messages.yml`
6. Set up cosmetics in `plugins/SkyFallBR/cosmetics.yml`
7. Restart server or use `/br reload` to apply changes

### Optional Dependencies
- **Vault**: Economy integration for cosmetic purchases
- **LuckPerms**: Advanced permission management
- **PlaceholderAPI**: Additional placeholders in messages

## Quick Start

1. **Set Lobby**: Use `/br setlobby` while standing where you want the lobby
2. **Create Arena**: Use `/br createarena <name> <mode>` to create game areas
3. **Join Game**: Players use `/br join` to enter matchmaking
4. **Admin Commands**: Use `/br admin` for game management

## Configuration Overview

### config.yml
- **supplyDrop**: Configure automatic loot drops
- **modes**: Define game modes with unique rules and loot
- **modeRotation**: Control which modes cycle in rotation

### messages.yml
- Customize all game messages and notifications
- Supports Minecraft color codes and placeholders

### cosmetics.yml
- Configure emotes, trails, weapon skins, and effects
- Set rarity tiers and unlock requirements

## Commands

### Player Commands
- `/br join` - Join matchmaking queue
- `/br leave` - Leave current game
- `/br stats [player]` - View player statistics
- `/br achievements` - View achievement progress
- `/br cosmetics` - Open cosmetics menu
- `/br party` - Manage party settings
- `/br leaderboard <type>` - View leaderboards (wins/kills/kd/winrate)

### Admin Commands
- `/br reload` - Reload configuration files
- `/br setlobby` - Set lobby spawn location
- `/br createarena <name> <mode>` - Create new arena
- `/br admin start <arena>` - Force start a game
- `/br admin stop <arena>` - Force stop a game
- `/br admin list` - List all arenas and status

## Permissions

### Player Permissions
- `skyfallbr.join` - Join games (default: true)
- `skyfallbr.stats` - View statistics (default: true)
- `skyfallbr.achievements` - View achievements (default: true)
- `skyfallbr.cosmetics` - Access cosmetics menu (default: true)
- `skyfallbr.party` - Use party commands (default: true)

### Admin Permissions
- `skyfallbr.admin` - Access admin commands (default: op)
- `skyfallbr.reload` - Reload configurations (default: op)
- `skyfallbr.bypass` - Bypass game restrictions (default: op)

## FAQ

### Q: How do I create custom arenas?
A: Use `/br createarena <name> <mode>` while standing in the desired location. The arena will use your current position as the center.

### Q: Players can't join games!
A: Ensure you have set a lobby location with `/br setlobby` and created at least one arena.

### Q: How do cosmetics work?
A: Players unlock cosmetics through achievements, levels, or purchases. Use `/br cosmetics` to equip them.

### Q: The server lags during games!
A: Adjust performance settings in config.yml - reduce entity limits, increase cleanup intervals, or lower zone shrink speeds.

### Q: Can I add custom loot?
A: Yes! Edit the lootTables in config.yml under each game mode. Add new items with material, amount, and enchantments.

## Support

- **Issues**: Report bugs on GitHub Issues
- **Wiki**: Check the WIKI.md for detailed documentation
- **Discord**: Join our community server for help

## Contributing

We welcome contributions! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License
```Copyright (c) 2025 Aarav Banshiwala

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to use
the Software **strictly for educational, research, or testing purposes on
servers you own or have explicit permission to use**.

You **MAY NOT**:
- Deploy this Software on servers without explicit authorization from the server owner.
- Redistribute, sell, or publicly share the Software in any form.
- Use the Software for malicious or unethical purposes.

The Software is provided "as-is", without warranty of any kind. In no event
shall the authors be liable for any claims, damages, or other liability,
whether in an action of contract, tort, or otherwise, arising from, out of,
or in connection with the Software or the use or other dealings in the Software.

By using this Software, you agree to comply with these terms.```

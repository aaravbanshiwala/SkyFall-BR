# SkyFallBR Developer Wiki

This wiki provides comprehensive documentation for developers working with or extending the SkyFallBR plugin.

## Architecture Overview

### Core Components

```
SkyFallBR (Main Plugin Class)
├── CommandManager - Handles all /br subcommands
├── GameManager - Manages active games and arenas
├── ConfigManager - Loads and manages configuration files
├── DatabaseManager - SQLite database operations
├── AchievementManager - Achievement tracking and unlocking
├── CosmeticManager - Cosmetic item management
├── PartyManager - Party system functionality
├── SupplyDropManager - Supply drop spawning and management
├── ZoneManager - Safe zone mechanics
├── BuildManager - Player building restrictions
├── StatsManager - Player statistics tracking
└── Listeners - Event handling for game mechanics
```

### Game Flow

1. **Lobby Phase**: Players join matchmaking queue
2. **Preparation**: Players teleported to arena, starting items given
3. **Gameplay**: Combat, looting, zone shrinking
4. **Endgame**: Final players compete, winner determined
5. **Cleanup**: Stats saved, players returned to lobby

### Data Storage

- **SQLite Database**: Player stats, game history, cosmetics
- **YAML Configs**: Game settings, loot tables, messages
- **In-Memory**: Active game state, temporary data

## Adding Custom Weapons

### Method 1: Config-Based Weapons

Add weapons to `config.yml` under mode loot tables:

```yaml
lootTables:
  rare:
    items:
      - material: DIAMOND_SWORD
        amount: 1
        enchantments:
          SHARPNESS: 3
          UNBREAKING: 2
        displayName: "&bLegendary Sword"
        lore:
          - "&7A powerful weapon"
```

### Method 2: Custom Weapon Classes

1. Create a new weapon class extending base weapon functionality
2. Register in `WeaponManager.kt`
3. Add to loot tables with custom properties

```kotlin
class CustomWeapon : Weapon() {
    override fun getDamageMultiplier(): Double = 1.5
    override fun getSpecialEffect(player: Player) {
        // Custom behavior
    }
}
```

### Damage Calculation

Base damage = Minecraft weapon damage × enchantment multiplier × weapon multiplier

- Sharpness: +1.25 per level
- Custom weapons can override `getDamageMultiplier()`

## Creating Custom Arenas

### Basic Arena Creation

1. Use `/br createarena <name> <mode>` command
2. Stand at desired center location
3. Arena automatically registers with GameManager

### Advanced Arena Configuration

Create arenas programmatically:

```kotlin
val arena = Arena(
    id = "custom_arena",
    name = "Custom Arena",
    center = player.location,
    mode = GameMode.SOLO,
    radius = 100
)
GameManager.registerArena(arena)
```

### Arena Properties

- **Center**: Spawn location and zone center
- **Radius**: Initial safe zone size
- **Mode**: Associated game mode configuration
- **Boundaries**: Automatic chunk loading/unloading

## Adding Game Modes

### 1. Define Mode Config

Add to `config.yml`:

```yaml
modes:
  custom_mode:
    teamSize: 3
    lootTables:
      common:
        items:
          - material: STONE_SWORD
            amount: 1
      rare:
        items:
          - material: IRON_SWORD
            amount: 1
    weaponRestrictions: []
    specialRules: ["custom_rule"]
    winConditions: "last_team_standing"
    zoneShrinkSpeed: 1.2
    rewards:
      win: 300
      top3: 150
```

### 2. Implement Special Rules

In `GameInstance.kt`, add rule handling:

```kotlin
when (rule) {
    "custom_rule" -> applyCustomRule()
    // Add your custom logic
}
```

### 3. Register Mode

Add to `modeRotation` in config.yml and update `GameMode` enum if needed.

## API for Developers

### Accessing Game Data

```kotlin
// Get player stats
val stats = DatabaseManager.loadPlayerStats(player.uniqueId)

// Get active games
val games = GameManager.activeGames

// Check if player is in game
val inGame = GameManager.activeGames.any { it.players.contains(player.uniqueId) }
```

### Creating Custom Events

```kotlin
// Fire custom achievement event
server.pluginManager.callEvent(CustomAchievementEvent(player, achievement))

// Listen for game events
@EventHandler
fun onGameStart(event: GameStartEvent) {
    // Custom logic
}
```

### Database Schema

#### player_stats Table
```sql
uuid TEXT PRIMARY KEY,
wins INTEGER,
kills INTEGER,
deaths INTEGER,
games_played INTEGER,
damage_dealt REAL,
first_win INTEGER,
ten_kill_game INTEGER,
win_streak INTEGER,
equipped_emote TEXT,
equipped_trail TEXT,
equipped_weapon_skin TEXT,
equipped_death_effect TEXT,
equipped_kill_message TEXT,
owned_cosmetics TEXT,
unlocked_achievements TEXT,
achievement_progress TEXT,
structures_placed INTEGER,
obtained_weapons TEXT,
kills_in_time INTEGER,
distance_moved REAL
```

#### game_history Table
```sql
id TEXT PRIMARY KEY,
winner TEXT,
players TEXT,
duration INTEGER,
timestamp INTEGER
```

### Extending Cosmetics

Add new cosmetic types:

1. Create cosmetic data class
2. Add to `CosmeticManager.kt`
3. Update database schema
4. Add GUI handling in `CosmeticsGui.kt`

```kotlin
data class NewCosmetic(
    val id: String,
    val name: String,
    val rarity: Rarity,
    val effect: String
)
```

## Database Schema

### Player Statistics
- Comprehensive tracking of wins, kills, deaths
- Achievement progress and unlocked cosmetics
- Performance metrics (damage dealt, structures placed)

### Game History
- Past game results and participants
- Duration and winner tracking
- Used for statistics and replay features

### Batch Operations
Use `DatabaseManager.batchUpdateStats()` for efficient bulk updates during game end.

## Performance Optimization

### Entity Management
- Automatic cleanup of old items/projectiles
- Configurable entity limits per arena
- Hologram entity pooling

### Chunk Loading
- Preload arena chunks on game start
- Unload chunks after game end
- Configurable chunk radius

### Database Operations
- Asynchronous saves using executor service
- Batch updates for multiple players
- Connection pooling with HikariCP

## Troubleshooting

### Common Issues

**High CPU Usage**
- Reduce `zoneShrinkSpeed` in config
- Increase `entityCleanupInterval`
- Lower `maxEntitiesPerArena`

**Memory Leaks**
- Check for unreleased entity references
- Monitor hologram entity count
- Ensure proper chunk unloading

**Database Errors**
- Verify SQLite file permissions
- Check connection pool settings
- Monitor for concurrent access issues

### Debug Mode

Enable debug logging in config.yml:
```yaml
debug: true
```

This provides detailed console output for:
- Game state changes
- Player actions
- Performance metrics
- Database operations

## Contributing Guidelines

### Code Style
- Kotlin idiomatic style
- 4-space indentation
- Comprehensive documentation
- Null safety throughout

### Testing
- Unit tests for core logic
- Integration tests for game flows
- Performance benchmarks

### Pull Request Process
1. Create feature branch from `develop`
2. Implement changes with tests
3. Update documentation
4. Submit PR with detailed description
5. Code review and merge

## API Reference

### Key Classes

- `GameInstance`: Represents an active game
- `PlayerData`: Player game session data
- `Arena`: Game arena configuration
- `Cosmetic`: Cosmetic item data
- `Achievement`: Achievement definition

### Events

- `GameStartEvent`: Fired when game begins
- `GameEndEvent`: Fired when game ends
- `PlayerEliminatedEvent`: Fired when player dies
- `AchievementUnlockEvent`: Fired when achievement unlocked

For complete API documentation, see the JavaDoc comments in source code.
package dev.vintage.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.vintage.SkyFallBR
import org.bukkit.Bukkit
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

object DatabaseManager {
    private lateinit var dataSource: HikariDataSource
    private val executor = Executors.newCachedThreadPool()

    fun init(plugin: SkyFallBR) {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:sqlite:${plugin.dataFolder}/database.db"
        config.maximumPoolSize = 10
        config.minimumIdle = 2
        config.connectionTimeout = 30000
        config.idleTimeout = 600000
        config.maxLifetime = 1800000
        dataSource = HikariDataSource(config)

        createTables()
    }

    private fun createTables() {
        useConnection { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        uuid TEXT PRIMARY KEY,
                        wins INTEGER DEFAULT 0,
                        kills INTEGER DEFAULT 0,
                        deaths INTEGER DEFAULT 0,
                        games_played INTEGER DEFAULT 0,
                        damage_dealt REAL DEFAULT 0.0,
                        first_win INTEGER DEFAULT 0,
                        ten_kill_game INTEGER DEFAULT 0,
                        win_streak INTEGER DEFAULT 0,
                        equipped_emote TEXT,
                        equipped_trail TEXT,
                        equipped_weapon_skin TEXT,
                        equipped_death_effect TEXT,
                        equipped_kill_message TEXT,
                        owned_cosmetics TEXT,
                        unlocked_achievements TEXT,
                        achievement_progress TEXT,
                        structures_placed INTEGER DEFAULT 0,
                        obtained_weapons TEXT,
                        kills_in_time INTEGER DEFAULT 0,
                        distance_moved REAL DEFAULT 0.0
                    )
                """.trimIndent())
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS game_history (
                        id TEXT PRIMARY KEY,
                        winner TEXT,
                        players TEXT,
                        duration INTEGER,
                        timestamp INTEGER DEFAULT (strftime('%s', 'now'))
                    )
                """.trimIndent())
            }
        }
    }

    private fun <T> useConnection(block: (Connection) -> T): T {
        return dataSource.connection.use(block)
    }

    fun loadPlayerStats(uuid: UUID): PlayerStats {
        return useConnection { conn ->
            val stmt = conn.prepareStatement("SELECT * FROM player_stats WHERE uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()
            if (rs.next()) {
                PlayerStats(
                    uuid = uuid,
                    wins = rs.getInt("wins"),
                    kills = rs.getInt("kills"),
                    deaths = rs.getInt("deaths"),
                    gamesPlayed = rs.getInt("games_played"),
                    damageDealt = rs.getDouble("damage_dealt"),
                    firstWin = rs.getBoolean("first_win"),
                    tenKillGame = rs.getBoolean("ten_kill_game"),
                    winStreak = rs.getInt("win_streak"),
                    equippedEmote = rs.getString("equipped_emote"),
                    equippedTrail = rs.getString("equipped_trail"),
                    equippedWeaponSkin = rs.getString("equipped_weapon_skin"),
                    equippedDeathEffect = rs.getString("equipped_death_effect"),
                    equippedKillMessage = rs.getString("equipped_kill_message"),
                    ownedCosmetics = rs.getString("owned_cosmetics")?.split(",")?.toMutableSet() ?: mutableSetOf(),
                    unlockedAchievements = rs.getString("unlocked_achievements")?.split(",")?.toMutableSet() ?: mutableSetOf(),
                    achievementProgress = rs.getString("achievement_progress")?.split(",")?.associate {
                        val parts = it.split(":")
                        parts[0] to parts[1].toInt()
                    }?.toMutableMap() ?: mutableMapOf(),
                    structuresPlaced = rs.getInt("structures_placed"),
                    obtainedWeapons = rs.getString("obtained_weapons")?.split(",")?.toMutableSet() ?: mutableSetOf(),
                    killsInTime = rs.getInt("kills_in_time"),
                    distanceMoved = rs.getDouble("distance_moved")
                )
            } else {
                PlayerStats(uuid)
            }
        }
    }

    fun savePlayerStats(stats: PlayerStats) {
        executor.submit {
            try {
                useConnection { conn ->
                    val stmt = conn.prepareStatement("""
                        INSERT OR REPLACE INTO player_stats (
                            uuid, wins, kills, deaths, games_played, damage_dealt, first_win, ten_kill_game, win_streak,
                            equipped_emote, equipped_trail, equipped_weapon_skin, equipped_death_effect, equipped_kill_message,
                            owned_cosmetics, unlocked_achievements, achievement_progress, structures_placed, obtained_weapons, kills_in_time, distance_moved
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent())
                    stmt.setString(1, stats.uuid.toString())
                    stmt.setInt(2, stats.wins)
                    stmt.setInt(3, stats.kills)
                    stmt.setInt(4, stats.deaths)
                    stmt.setInt(5, stats.gamesPlayed)
                    stmt.setDouble(6, stats.damageDealt)
                    stmt.setBoolean(7, stats.firstWin)
                    stmt.setBoolean(8, stats.tenKillGame)
                    stmt.setInt(9, stats.winStreak)
                    stmt.setString(10, stats.equippedEmote)
                    stmt.setString(11, stats.equippedTrail)
                    stmt.setString(12, stats.equippedWeaponSkin)
                    stmt.setString(13, stats.equippedDeathEffect)
                    stmt.setString(14, stats.equippedKillMessage)
                    stmt.setString(15, stats.ownedCosmetics.joinToString(","))
                    stmt.setString(16, stats.unlockedAchievements.joinToString(","))
                    stmt.setString(17, stats.achievementProgress.entries.joinToString(",") { "${it.key}:${it.value}" })
                    stmt.setInt(18, stats.structuresPlaced)
                    stmt.setString(19, stats.obtainedWeapons.joinToString(","))
                    stmt.setInt(20, stats.killsInTime)
                    stmt.setDouble(21, stats.distanceMoved)
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                SkyFallBR.instance.logger.warning("Failed to save player stats for ${stats.uuid}: ${e.message}")
            }
        }
    }

    fun batchUpdateStats(updates: List<Pair<UUID, (PlayerStats) -> Unit>>) {
        executor.submit {
            try {
                useConnection { conn ->
                    val stmt = conn.prepareStatement("""
                        UPDATE player_stats SET
                            wins = ?, kills = ?, deaths = ?, games_played = ?, damage_dealt = ?, first_win = ?, ten_kill_game = ?, win_streak = ?,
                            equipped_emote = ?, equipped_trail = ?, equipped_weapon_skin = ?, equipped_death_effect = ?, equipped_kill_message = ?,
                            owned_cosmetics = ?, unlocked_achievements = ?, achievement_progress = ?, structures_placed = ?, obtained_weapons = ?, kills_in_time = ?, distance_moved = ?
                        WHERE uuid = ?
                    """.trimIndent())
                    updates.forEach { (uuid, update) ->
                        val stats = loadPlayerStats(uuid)
                        update(stats)
                        stmt.setInt(1, stats.wins)
                        stmt.setInt(2, stats.kills)
                        stmt.setInt(3, stats.deaths)
                        stmt.setInt(4, stats.gamesPlayed)
                        stmt.setDouble(5, stats.damageDealt)
                        stmt.setBoolean(6, stats.firstWin)
                        stmt.setBoolean(7, stats.tenKillGame)
                        stmt.setInt(8, stats.winStreak)
                        stmt.setString(9, stats.equippedEmote)
                        stmt.setString(10, stats.equippedTrail)
                        stmt.setString(11, stats.equippedWeaponSkin)
                        stmt.setString(12, stats.equippedDeathEffect)
                        stmt.setString(13, stats.equippedKillMessage)
                        stmt.setString(14, stats.ownedCosmetics.joinToString(","))
                        stmt.setString(15, stats.unlockedAchievements.joinToString(","))
                        stmt.setString(16, stats.achievementProgress.entries.joinToString(",") { "${it.key}:${it.value}" })
                        stmt.setInt(17, stats.structuresPlaced)
                        stmt.setString(18, stats.obtainedWeapons.joinToString(","))
                        stmt.setInt(19, stats.killsInTime)
                        stmt.setDouble(20, stats.distanceMoved)
                        stmt.setString(21, uuid.toString())
                        stmt.addBatch()
                    }
                    stmt.executeBatch()
                }
            } catch (e: Exception) {
                SkyFallBR.instance.logger.warning("Failed to batch update stats: ${e.message}")
            }
        }
    }

    fun getLeaderboard(type: String, limit: Int): List<Pair<String, Double>> {
        val column = when (type.lowercase()) {
            "wins" -> "wins"
            "kills" -> "kills"
            "kd" -> "CAST(kills AS REAL) / CASE WHEN deaths = 0 THEN 1 ELSE deaths END"
            "winrate" -> "CAST(wins AS REAL) / CASE WHEN games_played = 0 THEN 1 ELSE games_played END * 100"
            else -> "wins"
        }
        return useConnection { conn ->
            val stmt = conn.prepareStatement("SELECT uuid, $column as value FROM player_stats ORDER BY value DESC LIMIT ?")
            stmt.setInt(1, limit)
            val rs = stmt.executeQuery()
            val results = mutableListOf<Pair<String, Double>>()
            while (rs.next()) {
                val uuid = rs.getString("uuid")
                val name = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name ?: "Unknown"
                results.add(name to rs.getDouble("value"))
            }
            results
        }
    }

    fun getRecentGames(limit: Int): List<GameHistory> {
        return useConnection { conn ->
            val stmt = conn.prepareStatement("SELECT * FROM game_history ORDER BY timestamp DESC LIMIT ?")
            stmt.setInt(1, limit)
            val rs = stmt.executeQuery()
            val games = mutableListOf<GameHistory>()
            while (rs.next()) {
                games.add(GameHistory(
                    id = rs.getString("id"),
                    winner = rs.getString("winner"),
                    players = rs.getString("players").split(","),
                    duration = rs.getInt("duration")
                ))
            }
            games
        }
    }

    fun saveGameHistory(gameId: String, winner: String?, players: List<String>, duration: Long) {
        executor.submit {
            try {
                useConnection { conn ->
                    val stmt = conn.prepareStatement("INSERT INTO game_history (id, winner, players, duration) VALUES (?, ?, ?, ?)")
                    stmt.setString(1, gameId)
                    stmt.setString(2, winner)
                    stmt.setString(3, players.joinToString(","))
                    stmt.setLong(4, duration)
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                SkyFallBR.instance.logger.warning("Failed to save game history: ${e.message}")
            }
        }
    }

    fun shutdown() {
        executor.shutdown()
        dataSource.close()
    }
}

data class GameHistory(
    val id: String,
    val winner: String?,
    val players: List<String>,
    val duration: Long
)
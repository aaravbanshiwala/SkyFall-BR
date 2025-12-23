package dev.vintage.data

import dev.vintage.SkyFallBR
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object StatsManager {
    private val cachedStats: MutableMap<UUID, PlayerStats> = mutableMapOf()
    private val pendingUpdates: MutableList<Pair<UUID, (PlayerStats) -> Unit>> = mutableListOf()

    fun init() {
        DatabaseManager.init(SkyFallBR.instance)
    }

    fun loadStats(uuid: UUID): PlayerStats {
        return cachedStats.getOrPut(uuid) { DatabaseManager.loadPlayerStats(uuid) }
    }

    fun saveStats(uuid: UUID) {
        cachedStats[uuid]?.let { DatabaseManager.savePlayerStats(it) }
    }

    fun updateStats(uuid: UUID, update: (PlayerStats) -> Unit) {
        val stats = loadStats(uuid)
        update(stats)
        cachedStats[uuid] = stats
        pendingUpdates.add(uuid to update)
        if (pendingUpdates.size >= 10) {
            batchSave()
        }
    }

    private fun batchSave() {
        if (pendingUpdates.isNotEmpty()) {
            DatabaseManager.batchUpdateStats(pendingUpdates.toList())
            pendingUpdates.clear()
        }
    }

    fun saveAllStats() {
        batchSave()
        cachedStats.forEach { (uuid, _) -> saveStats(uuid) }
    }

    fun getLeaderboard(type: String, limit: Int = 10): List<Pair<String, Double>> {
        return DatabaseManager.getLeaderboard(type, limit)
    }

    fun getRecentGames(limit: Int = 10): List<GameHistory> {
        return DatabaseManager.getRecentGames(limit)
    }

    fun saveGameHistory(gameId: String, winner: String?, players: List<String>, duration: Long) {
        DatabaseManager.saveGameHistory(gameId, winner, players, duration)
    }

    fun getPlayerName(uuid: String): String {
        return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).name ?: "Unknown"
    }

    fun shutdown() {
        batchSave()
        DatabaseManager.shutdown()
    }
}
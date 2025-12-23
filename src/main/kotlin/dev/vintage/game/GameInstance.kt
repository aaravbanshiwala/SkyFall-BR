package dev.vintage.game

import dev.vintage.achievement.AchievementManager
import dev.vintage.arena.Arena
import dev.vintage.building.BuildManager
import dev.vintage.core.ConfigManager
import dev.vintage.cosmetics.CosmeticManager
import dev.vintage.data.PlayerData
import dev.vintage.data.StatsManager
import dev.vintage.supplydrop.SupplyDropManager
import dev.vintage.ui.GameScoreboard
import dev.vintage.utils.MessageUtil
import dev.vintage.zone.Zone
import dev.vintage.zone.ZoneManager
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max

class GameInstance(
    val id: String,
    val arena: Arena,
    val modeConfig: GameModeConfig = ConfigManager.modeConfigs[arena.mode] ?: GameModeConfig(1, emptyMap(), emptyList(), emptyList(), "last_standing", 1.0, emptyMap()),
    val maxPlayers: Int = ConfigManager.maxPlayersPerArena
) {
    val players: MutableSet<UUID> = mutableSetOf()
    val teams: MutableMap<String, MutableSet<UUID>> = mutableMapOf()
    val zone = Zone(arena.center!!, ConfigManager.zonesInitialRadius)
    val zoneManager = ZoneManager(this)
    val supplyDropManager = SupplyDropManager(this)
    val buildManager = BuildManager(this)
    val scoreboard = GameScoreboard(this)
    var winner: UUID? = null
    var startTime: Long = 0
    var endTime: Long = 0
    var minPlayers: Int = 2
    private val loadedChunks: MutableSet<Chunk> = mutableSetOf()
    private val entityCleanupTask: BukkitTask? = null
    private val hologramEntities: MutableList<WeakReference<Entity>> = mutableListOf()

    init {
        preloadChunks()
        startEntityCleanup()
    }

    fun startGame() {
        startTime = System.currentTimeMillis()
        zoneManager.start()
        supplyDropManager.start()
        scoreboard.start()
        players.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            player?.let {
                CosmeticManager.applyActiveCosmetics(it)
                it.inventory.clear()
                giveStartingItems(it)
            }
        }
        MessageUtil.broadcastToGame(this, "game_started")
    }

    fun endGame() {
        endTime = System.currentTimeMillis()
        zoneManager.stop()
        supplyDropManager.stop()
        scoreboard.stop()
        buildManager.clearAll()
        clearEntities()
        unloadChunks()
        saveStats()
        players.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            player?.let {
                CosmeticManager.removeActiveCosmetics(it)
                it.teleport(ConfigManager.lobbyLocation ?: it.world.spawnLocation)
            }
        }
        GameManager.playerData.clear()
    }

    private fun preloadChunks() {
        val center = arena.center ?: return
        val radius = ConfigManager.zonesInitialRadius.toInt() / 16 + 2
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val chunk = center.world.getChunkAt(center.chunkX + x, center.chunkZ + z)
                if (!chunk.isLoaded) {
                    chunk.load(true)
                    loadedChunks.add(chunk)
                }
            }
        }
    }

    private fun unloadChunks() {
        loadedChunks.forEach { chunk ->
            if (chunk.isLoaded) {
                chunk.unload(true)
            }
        }
        loadedChunks.clear()
    }

    private fun startEntityCleanup() {
        object : BukkitRunnable() {
            override fun run() {
                val world = arena.center?.world ?: return
                val entities = world.entities
                val items = entities.filterIsInstance<Item>().filter { it.ticksLived > ConfigManager.despawnOldItemsTicks }
                val projectiles = entities.filterIsInstance<Projectile>().filter { it.ticksLived > ConfigManager.despawnOldProjectilesTicks }
                items.forEach { it.remove() }
                projectiles.forEach { it.remove() }
                val hologramCount = hologramEntities.count { it.get() != null }
                if (hologramCount > ConfigManager.hologramLimit) {
                    hologramEntities.removeAll { ref ->
                        val entity = ref.get()
                        if (entity != null && entity.ticksLived > 200) {
                            entity.remove()
                            true
                        } else false
                    }
                }
                val totalEntities = entities.size
                if (totalEntities > ConfigManager.entityLimitPerArena && ConfigManager.enablePerformanceWarnings) {
                    Bukkit.getLogger().warning("[SkyFallBR] High entity count in arena ${arena.id}: $totalEntities entities")
                }
            }
        }.runTaskTimer(SkyFallBR.instance, 0L, 600L)
    }

    private fun clearEntities() {
        val world = arena.center?.world ?: return
        world.entities.filter { it !is Player }.forEach { it.remove() }
        hologramEntities.clear()
    }

    fun addHologram(entity: Entity) {
        hologramEntities.add(WeakReference(entity))
    }

    private fun giveStartingItems(player: Player) {
        player.inventory.addItem(org.bukkit.inventory.ItemStack(Material.WOOD_SWORD))
        player.inventory.addItem(org.bukkit.inventory.ItemStack(Material.BOW))
        player.inventory.addItem(org.bukkit.inventory.ItemStack(Material.ARROW, 5))
    }

    fun addPlayer(uuid: UUID): Boolean {
        if (players.size >= maxPlayers) return false
        players.add(uuid)
        val teamId = when {
            modeConfig.teamSize == 1 -> uuid.toString()
            else -> assignTeam(uuid)
        }
        GameManager.playerData[uuid] = PlayerData(uuid, teamId)
        scoreboard.addPlayer(uuid)
        return true
    }

    fun removePlayer(uuid: UUID) {
        players.remove(uuid)
        teams.values.forEach { it.remove(uuid) }
        scoreboard.removePlayer(uuid)
        if (players.size <= 1 && startTime > 0) {
            winner = players.firstOrNull()
            endGame()
        }
    }

    private fun assignTeam(uuid: UUID): String {
        val availableTeams = teams.filter { it.value.size < modeConfig.teamSize }
        return if (availableTeams.isNotEmpty()) {
            val team = availableTeams.keys.first()
            teams[team]!!.add(uuid)
            team
        } else {
            val newTeam = "team_${teams.size + 1}"
            teams[newTeam] = mutableSetOf(uuid)
            newTeam
        }
    }

    fun getTeam(uuid: UUID): String? = GameManager.playerData[uuid]?.teamId

    fun getPlayerData(uuid: UUID): PlayerData = GameManager.playerData[uuid] ?: PlayerData(uuid)

    private fun saveStats() {
        val updates = players.map { uuid ->
            uuid to { stats: dev.vintage.data.PlayerStats ->
                val data = getPlayerData(uuid)
                stats.gamesPlayed++
                stats.kills += data.gameKills
                stats.deaths += data.deaths
                stats.damageDealt += data.gameDamage
                if (data.placement == 1) {
                    stats.wins++
                    stats.winStreak++
                    if (stats.wins == 1) stats.firstWin = true
                } else {
                    stats.winStreak = 0
                }
                if (data.gameKills >= 10) stats.tenKillGame = true
            }
        }
        StatsManager.batchUpdateStats(updates)
        players.forEach { uuid ->
            AchievementManager.checkAchievements(Bukkit.getPlayer(uuid))
        }
        val winnerName = winner?.let { Bukkit.getOfflinePlayer(it).name }
        val playerNames = players.mapNotNull { Bukkit.getOfflinePlayer(it).name }
        StatsManager.saveGameHistory(id, winnerName, playerNames, (endTime - startTime) / 1000)
    }
}
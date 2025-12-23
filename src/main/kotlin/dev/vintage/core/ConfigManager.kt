package dev.vintage.core

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import org.bukkit.Bukkit
import dev.vintage.game.GameMode
import dev.vintage.game.GameModeConfig
import dev.vintage.game.LootItem
import dev.vintage.zone.ZonePhaseConfig
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.potion.PotionType

object ConfigManager {
    lateinit var plugin: JavaPlugin
    var lobbyLocation: Location? = null
    lateinit var zonesInitialRadius: Double
    lateinit var zonesPhases: List<ZonePhaseConfig>
    lateinit var messages: FileConfiguration
    var enableBuilding: Boolean = false
    var headshotMultiplier: Double = 2.0
    var criticalChance: Double = 0.1
    var criticalMultiplier: Double = 1.5
    var damageFalloff: Double = 0.05
    var armorEffectiveness: Double = 0.5
    var shieldRegeneration: Double = 1.0
    var supplyDropInterval: Int = 120
    var supplyDropOpeningTime: Int = 8
    var supplyDropAnnouncementTime: Int = 30
    lateinit var modeConfigs: Map<GameMode, GameModeConfig>
    var modeRotation: List<GameMode> = emptyList()

    var maxPlayersPerArena: Int = 100
    var particleDensity: Double = 1.0
    var zoneUpdateTicks: Long = 2L
    var scoreboardUpdateTicks: Long = 20L
    var hologramLimit: Int = 50
    var entityLimitPerArena: Int = 500
    var despawnOldItemsTicks: Long = 6000L
    var despawnOldProjectilesTicks: Long = 1200L
    var batchParticleSpawning: Boolean = true
    var enablePerformanceWarnings: Boolean = true
    var performanceWarningThreshold: Long = 50L

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
        plugin.saveDefaultConfig()
        val config = plugin.config
        zonesInitialRadius = config.getDouble("zones.initialRadius")
        zonesPhases = config.getConfigurationSection("zones.phases")?.getKeys(false)?.map { key ->
            val section = config.getConfigurationSection("zones.phases.$key")!!
            ZonePhaseConfig(
                waitTime = section.getInt("waitTime"),
                shrinkTime = section.getInt("shrinkTime"),
                damagePerSecond = section.getDouble("damagePerSecond"),
                nextRadius = section.getDouble("nextRadius")
            )
        } ?: emptyList()
        val worldName = config.getString("lobby.world")
        if (worldName != null) {
            val world = Bukkit.getWorld(worldName)
            if (world != null) {
                val x = config.getDouble("lobby.x")
                val y = config.getDouble("lobby.y")
                val z = config.getDouble("lobby.z")
                lobbyLocation = Location(world, x, y, z)
            }
        }
        messages = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, "messages.yml"))
        enableBuilding = config.getBoolean("enableBuilding")
        headshotMultiplier = config.getDouble("combat.headshotMultiplier", 2.0)
        criticalChance = config.getDouble("combat.criticalChance", 0.1)
        criticalMultiplier = config.getDouble("combat.criticalMultiplier", 1.5)
        damageFalloff = config.getDouble("combat.damageFalloff", 0.05)
        armorEffectiveness = config.getDouble("combat.armorEffectiveness", 0.5)
        shieldRegeneration = config.getDouble("combat.shieldRegeneration", 1.0)
        supplyDropInterval = config.getInt("supplyDrop.interval", 120)
        supplyDropOpeningTime = config.getInt("supplyDrop.openingTime", 8)
        supplyDropAnnouncementTime = config.getInt("supplyDrop.announcementTime", 30)
        modeConfigs = loadModeConfigs(config)
        modeRotation = config.getStringList("modeRotation").mapNotNull { GameMode.valueOf(it.uppercase()) }

        maxPlayersPerArena = config.getInt("performance.maxPlayersPerArena", 100)
        particleDensity = config.getDouble("performance.particleDensity", 1.0)
        zoneUpdateTicks = config.getLong("performance.zoneUpdateTicks", 2L)
        scoreboardUpdateTicks = config.getLong("performance.scoreboardUpdateTicks", 20L)
        hologramLimit = config.getInt("performance.hologramLimit", 50)
        entityLimitPerArena = config.getInt("performance.entityLimitPerArena", 500)
        despawnOldItemsTicks = config.getLong("performance.despawnOldItemsTicks", 6000L)
        despawnOldProjectilesTicks = config.getLong("performance.despawnOldProjectilesTicks", 1200L)
        batchParticleSpawning = config.getBoolean("performance.batchParticleSpawning", true)
        enablePerformanceWarnings = config.getBoolean("performance.enablePerformanceWarnings", true)
        performanceWarningThreshold = config.getLong("performance.performanceWarningThreshold", 50L)
    }

    private fun loadModeConfigs(config: FileConfiguration): Map<GameMode, GameModeConfig> {
        val modesSection = config.getConfigurationSection("modes") ?: return emptyMap()
        return modesSection.getKeys(false).associate { key ->
            val mode = GameMode.valueOf(key.uppercase())
            val section = modesSection.getConfigurationSection(key)!!
            val teamSize = section.getInt("teamSize")
            val lootTables = section.getConfigurationSection("lootTables")?.getKeys(false)?.associate { tableKey ->
                val tableSection = section.getConfigurationSection("lootTables.$tableKey")!!
                tableKey to tableSection.getList("items")?.map { item ->
                    val itemMap = item as Map<*, *>
                    LootItem(
                        material = Material.valueOf(itemMap["material"] as String),
                        amount = itemMap["amount"] as Int,
                        enchantments = (itemMap["enchantments"] as? Map<*, *>)?.mapKeys { Enchantment.getByName(it.key as String)!! }?.mapValues { it.value as Int } ?: emptyMap(),
                        potionType = (itemMap["potionType"] as? String)?.let { PotionType.valueOf(it) }
                    )
                } ?: emptyList()
            } ?: emptyMap()
            val weaponRestrictions = section.getStringList("weaponRestrictions").map { Material.valueOf(it) }
            val specialRules = section.getStringList("specialRules")
            val winConditions = section.getString("winConditions")
            val zoneShrinkSpeed = section.getDouble("zoneShrinkSpeed", 1.0)
            val rewards = section.getConfigurationSection("rewards")?.getKeys(false)?.associate {
                it to section.getInt("rewards.$it")
            } ?: emptyMap()
            mode to GameModeConfig(teamSize, lootTables, weaponRestrictions, specialRules, winConditions, zoneShrinkSpeed, rewards)
        }
    }

    fun saveLobby() {
        plugin.config.set("lobby.world", lobbyLocation?.world?.name)
        plugin.config.set("lobby.x", lobbyLocation?.x)
        plugin.config.set("lobby.y", lobbyLocation?.y)
        plugin.config.set("lobby.z", lobbyLocation?.z)
        plugin.saveConfig()
    }

    fun getMessage(key: String): String = messages.getString(key) ?: key
}
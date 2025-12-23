package dev.vintage

import org.bukkit.plugin.java.JavaPlugin
import dev.vintage.core.ConfigManager
import dev.vintage.data.DatabaseManager
import dev.vintage.command.CommandManager
import dev.vintage.listener.*

class SkyFallBR : JavaPlugin() {

    override fun onEnable() {
        ConfigManager.init(this)
        DatabaseManager.init(this)
        getCommand("br")?.setExecutor(CommandManager())
        server.pluginManager.registerEvents(AchievementListener(), this)
        server.pluginManager.registerEvents(CombatListener(), this)
        server.pluginManager.registerEvents(CosmeticsListener(), this)
        server.pluginManager.registerEvents(HarvestingListener(), this)
        server.pluginManager.registerEvents(LobbyListener(), this)
        server.pluginManager.registerEvents(SupplyDropListener(), this)
    }

    override fun onDisable() {
    }
}
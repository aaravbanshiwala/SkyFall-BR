package dev.vintage.command

import dev.vintage.data.PlayerStats
import dev.vintage.data.StatsManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class StatsGui(private val targetUuid: UUID, private val viewer: Player) {
    private val stats: PlayerStats = StatsManager.loadStats(targetUuid)
    private val inventory: Inventory = Bukkit.createInventory(null, 27, "Stats - ${StatsManager.getPlayerName(targetUuid.toString())}")

    init {
        setupItems()
        viewer.openInventory(inventory)
    }

    private fun setupItems() {
        val head = ItemStack(Material.PLAYER_HEAD)
        val meta = head.itemMeta as SkullMeta
        meta.owningPlayer = Bukkit.getOfflinePlayer(targetUuid)
        meta.displayName(MessageUtil.parseMessage("&e${StatsManager.getPlayerName(targetUuid.toString())}", emptyMap()).toString())
        head.itemMeta = meta
        inventory.setItem(4, head)

        inventory.setItem(10, createStatItem(Material.DIAMOND_SWORD, "&aWins", stats.wins.toString()))
        inventory.setItem(11, createStatItem(Material.IRON_SWORD, "&cKills", stats.kills.toString()))
        inventory.setItem(12, createStatItem(Material.BONE, "&4Deaths", stats.deaths.toString()))
        inventory.setItem(13, createStatItem(Material.CLOCK, "&eGames Played", stats.gamesPlayed.toString()))
        inventory.setItem(14, createStatItem(Material.REDSTONE, "&6Damage Dealt", "%.1f".format(stats.damageDealt)))
        inventory.setItem(15, createStatItem(Material.COMPASS, "&bK/D Ratio", "%.2f".format(stats.kdRatio)))
        inventory.setItem(16, createStatItem(Material.EXPERIENCE_BOTTLE, "&dWin Rate", "%.1f%%".format(stats.winRate)))

        var slot = 18
        if (stats.firstWin) inventory.setItem(slot++, createAchievementItem("&6First Win"))
        if (stats.tenKillGame) inventory.setItem(slot++, createAchievementItem("&6Ten Kill Game"))
        if (stats.winStreak > 0) inventory.setItem(slot, createAchievementItem("&6Win Streak: ${stats.winStreak}"))
    }

    private fun createStatItem(material: Material, name: String, value: String): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(MessageUtil.parseMessage("$name: &f$value", emptyMap()).toString())
        item.itemMeta = meta
        return item
    }

    private fun createAchievementItem(name: String): ItemStack {
        val item = ItemStack(Material.GOLD_BLOCK)
        val meta = item.itemMeta
        meta.displayName(MessageUtil.parseMessage(name, emptyMap()).toString())
        item.itemMeta = meta
        return item
    }
}
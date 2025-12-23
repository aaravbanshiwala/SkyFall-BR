package dev.vintage.listener

import dev.vintage.core.ConfigManager
import dev.vintage.data.PlayerData
import dev.vintage.game.GameManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class HarvestingListener : Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val game = GameManager.playerData[player.uniqueId]?.currentGame ?: return
        val data = game.getPlayerData(player.uniqueId)
        val block = event.block
        val multiplier = if (game.arena.mode == dev.vintage.game.GameMode.LTM_HIGH_EXPLOSIVES) 2 else 1
        when (block.type) {
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG -> {
                data.addResource("wood", 1 * multiplier)
            }
            Material.STONE, Material.COBBLESTONE -> {
                data.addResource("stone", 1 * multiplier)
            }
            Material.IRON_ORE -> {
                data.addResource("metal", 1 * multiplier)
            }
        }
        player.sendActionBar(data.getActionBarText())
    }
}
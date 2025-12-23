package dev.vintage.listener

import dev.vintage.game.GameManager
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class SupplyDropListener : Listener {
    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (entity is ArmorStand && entity.customName == "Supply Drop") {
            val game = GameManager.getPlayerData(player.uniqueId).currentGame ?: return
            game.supplyDropManager.handleOpening(player, entity)
        }
    }
}
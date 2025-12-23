package dev.vintage.listener

import dev.vintage.command.CosmeticsGui
import dev.vintage.cosmetics.CosmeticManager
import dev.vintage.cosmetics.CosmeticType
import dev.vintage.game.GameManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent

class CosmeticsListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.view.title == "Cosmetics Menu") {
            event.isCancelled = true
            val player = event.whoClicked as? org.bukkit.entity.Player ?: return
            val gui = CosmeticsGui(player)
            gui.handleClick(event.slot)
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val data = GameManager.getPlayerData(player.uniqueId)
        val trailId = data.equippedTrail
        if (trailId != null) {
            val trail = CosmeticManager.getTrail(trailId)
            if (trail != null) {
                CosmeticManager.applyTrail(player, trail)
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val data = GameManager.getPlayerData(player.uniqueId)
        val deathEffectId = data.equippedDeathEffect
        if (deathEffectId != null) {
            val effect = CosmeticManager.getDeathEffect(deathEffectId)
            if (effect != null) {
                CosmeticManager.applyDeathEffect(player, effect)
            }
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? org.bukkit.entity.Player ?: return
        val attacker = event.damager as? org.bukkit.entity.Player ?: return
        if (event.finalDamage >= victim.health) {
            val data = GameManager.getPlayerData(attacker.uniqueId)
            val killMessageId = data.equippedKillMessage
            if (killMessageId != null) {
                val killMessage = CosmeticManager.getKillMessage(killMessageId)
                if (killMessage != null) {
                    val message = CosmeticManager.getKillMessage(killMessage, attacker.name, victim.name)
                    attacker.server.broadcastMessage(message)
                }
            }
        }
    }
}
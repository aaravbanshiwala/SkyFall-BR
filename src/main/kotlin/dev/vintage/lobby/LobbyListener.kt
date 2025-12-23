package dev.vintage.lobby

import dev.vintage.game.GameManager
import dev.vintage.game.GameState
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class LobbyListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (LobbyManager.isInLobby(player)) {
            LobbyManager.joinLobby(player)
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (LobbyManager.isInLobby(player)) {
            val game = GameManager.playerData[player.uniqueId]?.currentGame
            game?.removePlayer(player.uniqueId)
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity is Player && LobbyManager.isInLobby(event.entity as Player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (event.entity is Player && LobbyManager.isInLobby(event.entity as Player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!LobbyManager.isInLobby(player)) return
        val item = event.item ?: return
        when (item.type) {
            Material.STONE -> {
                // Leave item
                val game = GameManager.playerData[player.uniqueId]?.currentGame
                game?.removePlayer(player.uniqueId)
                LobbyManager.leaveLobby(player)
            }
            Material.PAPER -> {
                // Stats item
                // Handle stats display
            }
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (LobbyManager.isInLobby(event.player)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked is Player && LobbyManager.isInLobby(event.whoClicked as Player)) {
            event.isCancelled = true
        }
    }
}
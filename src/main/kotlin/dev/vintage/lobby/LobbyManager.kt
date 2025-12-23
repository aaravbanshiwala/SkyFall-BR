package dev.vintage.lobby

import dev.vintage.core.ConfigManager
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import java.util.UUID

object LobbyManager {
    private val leaveItem: ItemStack by lazy {
        val material = Material.valueOf(ConfigManager.getMessage("lobby.leaveItem.material").uppercase())
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.setDisplayName(ConfigManager.getMessage("lobby.leaveItem.name"))
        item.itemMeta = meta
        item
    }

    private val statsItem: ItemStack by lazy {
        val material = Material.valueOf(ConfigManager.getMessage("lobby.statsItem.material").uppercase())
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.setDisplayName(ConfigManager.getMessage("lobby.statsItem.name"))
        item.itemMeta = meta
        item
    }

    fun joinLobby(player: Player) {
        val lobby = ConfigManager.lobbyLocation ?: return
        player.teleport(lobby)
        player.inventory.clear()
        player.inventory.setItem(0, leaveItem.clone())
        player.inventory.setItem(1, statsItem.clone())
        player.foodLevel = 20
        player.health = 20.0
        MessageUtil.sendMessage(player, "joined_lobby")
    }

    fun leaveLobby(player: Player) {
        player.inventory.clear()
        MessageUtil.sendMessage(player, "left_lobby")
    }

    fun isInLobby(player: Player): Boolean {
        val lobby = ConfigManager.lobbyLocation ?: return false
        return player.location.world == lobby.world && player.location.distance(lobby) < 50
    }
}
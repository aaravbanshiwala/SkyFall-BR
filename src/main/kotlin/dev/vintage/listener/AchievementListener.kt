package dev.vintage.listener

import dev.vintage.achievement.AchievementGui
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class AchievementListener : Listener {

    @EventHandler

    fun onInventoryClick(event: InventoryClickEvent) {

        if (event.view.title == "Achievements") {

            event.isCancelled = true

            val player = event.whoClicked as? org.bukkit.entity.Player ?: return

            val gui = AchievementGui(player)

            gui.handleClick(event.slot)

        }

    }

}
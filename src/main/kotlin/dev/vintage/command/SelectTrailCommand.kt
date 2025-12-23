package dev.vintage.command

import dev.vintage.cosmetics.CosmeticManager
import dev.vintage.cosmetics.CosmeticType
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SelectTrailCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "player_only")
            return true
        }
        if (args.isEmpty()) {
            MessageUtil.sendMessage(sender, "usage_selecttrail")
            return true
        }
        val trailId = args[0]
        val trail = CosmeticManager.getTrail(trailId)
        if (trail == null) {
            MessageUtil.sendMessage(sender, "trail_not_found")
            return true
        }
        val data = GameManager.getPlayerData(sender.uniqueId)
        if (!data.ownedCosmetics.contains(trailId)) {
            MessageUtil.sendMessage(sender, "cosmetic_locked")
            return true
        }
        CosmeticManager.equipCosmetic(sender, CosmeticType.TRAIL, trailId)
        MessageUtil.sendMessage(sender, "trail_equipped", mapOf("trail" to trail.name))
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size == 1) {
            return CosmeticManager.getAllTrails().keys.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
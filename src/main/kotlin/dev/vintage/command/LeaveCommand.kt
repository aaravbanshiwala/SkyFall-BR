package dev.vintage.command

import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LeaveCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) return true
        if (!sender.hasPermission("skyfall.play")) {
            MessageUtil.sendMessage(sender, "no_permission")
            return true
        }
        val playerData = GameManager.getPlayerData(sender.uniqueId)
        val game = playerData.currentGame ?: run {
            MessageUtil.sendMessage(sender, "not_in_game")
            return true
        }
        game.removePlayer(sender.uniqueId)
        playerData.currentGame = null
        MessageUtil.sendMessage(sender, "left_game")
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
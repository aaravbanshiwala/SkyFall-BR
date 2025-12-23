package dev.vintage.command

import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender

class ListCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (!sender.hasPermission("skyfall.play")) {
            MessageUtil.sendMessage(sender, "no_permission")
            return true
        }
        val arenas = GameManager.arenas
        if (arenas.isEmpty()) {
            MessageUtil.sendMessage(sender, "no_arenas")
            return true
        }
        val arenaList = arenas.joinToString(", ") { it.id }
        MessageUtil.sendMessage(sender, "arena_list", mapOf("arenas" to arenaList))
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
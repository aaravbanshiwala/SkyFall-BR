package dev.vintage.command

import dev.vintage.command.StatsGui
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class StatsCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "player_only")
            return true
        }
        val targetUuid = if (args.isNotEmpty()) {
            val targetPlayer = Bukkit.getPlayer(args[0])
            targetPlayer?.uniqueId ?: run {
                MessageUtil.sendMessage(sender, "player_not_found")
                return true
            }
        } else {
            sender.uniqueId
        }
        StatsGui(targetUuid, sender)
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.size == 1) {
            Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[0], ignoreCase = true) }
        } else emptyList()
    }
}
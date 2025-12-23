package dev.vintage.command

import dev.vintage.data.StatsManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender

class LeaderboardCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        val type = args.getOrNull(0)?.lowercase() ?: "wins"
        if (type !in listOf("wins", "kills", "kd")) {
            MessageUtil.sendMessage(sender, "invalid_leaderboard_type")
            return true
        }
        val leaderboard = StatsManager.getLeaderboard(type)
        MessageUtil.sendMessage(sender, "leaderboard_header", mapOf("type" to type.uppercase()))
        leaderboard.forEachIndexed { index, (uuid, value) ->
            val name = StatsManager.getPlayerName(uuid)
            val displayValue = if (type == "kd") "%.2f" else "%.0f"
            MessageUtil.sendMessage(sender, "leaderboard_entry", mapOf(
                "rank" to (index + 1).toString(),
                "name" to name,
                "value" to displayValue.format(value)
            ))
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.size == 1) {
            listOf("wins", "kills", "kd").filter { it.startsWith(args[0], ignoreCase = true) }
        } else emptyList()
    }
}
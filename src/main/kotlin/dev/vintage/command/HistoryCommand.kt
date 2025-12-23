package dev.vintage.command

import dev.vintage.data.StatsManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat
import java.util.*

class HistoryCommand : Command {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        val history = StatsManager.getRecentGames()
        MessageUtil.sendMessage(sender, "game_history_header")
        history.forEach { game ->
            val date = dateFormat.format(Date(game.date))
            val players = game.players.joinToString(", ")
            MessageUtil.sendMessage(sender, "game_history_entry", mapOf(
                "date" to date,
                "winner" to (game.winner ?: "None"),
                "players" to players,
                "duration" to "${game.duration}s"
            ))
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
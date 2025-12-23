package dev.vintage.command

import dev.vintage.core.ConfigManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetLobbyCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "only_players")
            return true
        }
        ConfigManager.lobbyLocation = sender.location
        ConfigManager.saveLobby()
        MessageUtil.sendMessage(sender, "lobby_set")
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
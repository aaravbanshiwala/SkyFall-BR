package dev.vintage.command

import dev.vintage.cosmetics.CosmeticManager
import dev.vintage.cosmetics.CosmeticType
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CosmeticsCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "player_only")
            return true
        }
        CosmeticsGui(sender).open()
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
package dev.vintage.command

import dev.vintage.core.ConfigManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender

class ReloadCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        ConfigManager.init(dev.vintage.SkyFallBR.instance)
        MessageUtil.sendMessage(sender, "config_reloaded")
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
package dev.vintage.command

import org.bukkit.command.CommandSender

interface Command {
    fun execute(sender: CommandSender, args: Array<String>): Boolean
    fun tabComplete(sender: CommandSender, args: Array<String>): List<String>
}
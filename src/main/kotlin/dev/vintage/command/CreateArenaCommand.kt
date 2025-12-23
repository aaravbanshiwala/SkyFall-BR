package dev.vintage.command

import dev.vintage.arena.Arena
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender

class CreateArenaCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty()) return false
        val name = args[0]
        if (GameManager.arenas.any { it.id == name }) {
            MessageUtil.sendMessage(sender, "arena_exists")
            return true
        }
        val arena = Arena(name)
        GameManager.registerArena(arena)
        MessageUtil.sendMessage(sender, "arena_created", mapOf("name" to name))
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
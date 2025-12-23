package dev.vintage.command

import dev.vintage.utils.MessageUtil
import dev.vintage.game.GameMode
import dev.vintage.game.GameManager
import org.bukkit.command.CommandSender

class AdminCommand : Command {
    private val subCommands = mutableMapOf<String, Command>()

    init {
        subCommands["reload"] = ReloadCommand()
        subCommands["setlobby"] = SetLobbyCommand()
        subCommands["createarena"] = CreateArenaCommand()
        subCommands["setmode"] = SetModeCommand()
    }

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (!sender.hasPermission("skyfall.admin")) {
            MessageUtil.sendMessage(sender, "no_permission")
            return true
        }
        if (args.isEmpty()) return false
        val sub = args[0].lowercase()
        return subCommands[sub]?.execute(sender, args.drop(1).toTypedArray()) ?: false
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size == 1) {
            return subCommands.keys.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        val sub = args[0].lowercase()
        return subCommands[sub]?.tabComplete(sender, args.drop(1).toTypedArray()) ?: emptyList()
    }
}

class SetModeCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2) {
            MessageUtil.sendMessage(sender, "usage_setmode")
            return true
        }
        val arenaId = args[0]
        val modeStr = args[1].uppercase()
        val mode = try {
            GameMode.valueOf(modeStr)
        } catch (e: IllegalArgumentException) {
            MessageUtil.sendMessage(sender, "invalid_mode")
            return true
        }
        val arena = GameManager.arenas.find { it.id == arenaId }
        if (arena == null) {
            MessageUtil.sendMessage(sender, "arena_not_found")
            return true
        }
        arena.mode = mode
        MessageUtil.sendMessage(sender, "mode_set", mapOf("arena" to arenaId, "mode" to mode.name))
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> GameManager.arenas.map { it.id }
            2 -> GameMode.values().map { it.name }
            else -> emptyList()
        }
    }
}
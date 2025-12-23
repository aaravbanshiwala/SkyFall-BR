package dev.vintage.command

import dev.vintage.core.ConfigManager
import dev.vintage.game.GameManager
import dev.vintage.party.PartyManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JoinCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "only_players")
            return true
        }
        if (!sender.hasPermission("skyfall.play")) {
            MessageUtil.sendMessage(sender, "no_permission")
            return true
        }
        val arenaName = args.getOrNull(0)
        val arena = if (arenaName != null) {
            GameManager.arenas.find { it.id == arenaName }
        } else {
            GameManager.arenas.firstOrNull()
        }
        if (arena == null) {
            MessageUtil.sendMessage(sender, "arena_not_found")
            return true
        }
        val game = GameManager.activeGames.find { it.arena == arena } ?: GameManager.createGame(arena)
        val playerData = GameManager.getPlayerData(sender.uniqueId)
        if (playerData.currentGame != null) {
            MessageUtil.sendMessage(sender, "already_in_game")
            return true
        }
        val party = PartyManager.getPlayerParty(sender.uniqueId)
        if (party != null) {
            if (party.leader != sender.uniqueId) {
                MessageUtil.sendMessage(sender, "only_leader_can_join")
                return true
            }
            party.members.forEach { member ->
                val memberPlayer = Bukkit.getPlayer(member)
                if (memberPlayer != null) {
                    val memberData = GameManager.getPlayerData(member)
                    if (memberData.currentGame != null) continue
                    game.addPlayer(member, party.id)
                    memberData.currentGame = game
                    MessageUtil.sendMessage(memberPlayer, "joined_game", mapOf("arena" to arena.id))
                }
            }
        } else {
            game.addPlayer(sender.uniqueId)
            playerData.currentGame = game
            MessageUtil.sendMessage(sender, "joined_game", mapOf("arena" to arena.id))
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size == 1) {
            return GameManager.arenas.map { it.id }.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
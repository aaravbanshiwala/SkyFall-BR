package dev.vintage.command

import dev.vintage.party.PartyManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PartyCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) return true
        if (args.isEmpty()) {
            MessageUtil.sendMessage(sender, "party_usage")
            return true
        }
        when (args[0].lowercase()) {
            "create" -> {
                if (PartyManager.getPlayerParty(sender.uniqueId) != null) {
                    MessageUtil.sendMessage(sender, "already_in_party")
                    return true
                }
                PartyManager.createParty(sender)
            }
            "invite" -> {
                val party = PartyManager.getPlayerParty(sender.uniqueId)
                if (party == null || party.leader != sender.uniqueId) {
                    MessageUtil.sendMessage(sender, "not_leader")
                    return true
                }
                val targetName = args.getOrNull(1)
                if (targetName == null) {
                    MessageUtil.sendMessage(sender, "specify_player")
                    return true
                }
                val target = Bukkit.getPlayer(targetName)
                if (target == null) {
                    MessageUtil.sendMessage(sender, "player_not_found")
                    return true
                }
                PartyManager.invitePlayer(party.id, sender, target)
            }
            "accept" -> {
                if (PartyManager.getPlayerParty(sender.uniqueId) != null) {
                    MessageUtil.sendMessage(sender, "already_in_party")
                    return true
                }
                PartyManager.acceptInvite(sender)
            }
            "leave" -> {
                PartyManager.leaveParty(sender)
            }
            "kick" -> {
                val party = PartyManager.getPlayerParty(sender.uniqueId)
                if (party == null || party.leader != sender.uniqueId) {
                    MessageUtil.sendMessage(sender, "not_leader")
                    return true
                }
                val targetName = args.getOrNull(1)
                if (targetName == null) {
                    MessageUtil.sendMessage(sender, "specify_player")
                    return true
                }
                val target = Bukkit.getPlayer(targetName)
                if (target == null) {
                    MessageUtil.sendMessage(sender, "player_not_found")
                    return true
                }
                PartyManager.kickPlayer(party.id, sender, target)
            }
            "chat" -> {
                val message = args.drop(1).joinToString(" ")
                if (message.isBlank()) {
                    MessageUtil.sendMessage(sender, "specify_message")
                    return true
                }
                PartyManager.sendPartyMessage(sender, message)
            }
            else -> MessageUtil.sendMessage(sender, "unknown_party_command")
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (sender !is Player) return emptyList()
        return when (args.size) {
            1 -> listOf("create", "invite", "accept", "leave", "kick", "chat").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> if (args[0].lowercase() == "invite" || args[0].lowercase() == "kick") {
                Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
            } else emptyList()
            else -> emptyList()
        }
    }
}
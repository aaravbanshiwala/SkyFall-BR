package dev.vintage.command

import dev.vintage.cosmetics.CosmeticManager
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class EmoteCommand : Command {
    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (sender !is Player) {
            MessageUtil.sendMessage(sender, "player_only")
            return true
        }
        if (args.isEmpty()) {
            MessageUtil.sendMessage(sender, "usage_emote")
            return true
        }
        val emoteId = args[0]
        val emote = CosmeticManager.getEmote(emoteId)
        if (emote == null) {
            MessageUtil.sendMessage(sender, "emote_not_found")
            return true
        }
        val data = GameManager.getPlayerData(sender.uniqueId)
        if (!data.ownedCosmetics.contains(emoteId)) {
            MessageUtil.sendMessage(sender, "cosmetic_locked")
            return true
        }
        CosmeticManager.applyEmote(sender, emote)
        return true
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        if (args.size == 1) {
            return CosmeticManager.getAllEmotes().keys.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
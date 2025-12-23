package dev.vintage.utils

import dev.vintage.SkyFallBR
import dev.vintage.core.ConfigManager
import dev.vintage.game.GameInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object MessageUtil {
    private val miniMessage = MiniMessage.miniMessage()

    fun sendMessage(sender: CommandSender, key: String, placeholders: Map<String, Any> = emptyMap()) {
        val message = ConfigManager.getMessage(key)
        val component = parseMessage(message, placeholders)
        if (sender is Player) {
            sender.sendMessage(component)
        } else {
            SkyFallBR.instance.logger.info(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component))
        }
    }

    fun broadcastToGame(game: GameInstance, key: String, placeholders: Map<String, Any> = emptyMap()) {
        val message = ConfigManager.getMessage(key)
        val component = parseMessage(message, placeholders)
        game.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { it.sendMessage(component) }
    }

    fun sendTitle(player: Player, titleKey: String, subtitleKey: String, placeholders: Map<String, Any> = emptyMap()) {
        val title = parseMessage(ConfigManager.getMessage(titleKey), placeholders)
        val subtitle = parseMessage(ConfigManager.getMessage(subtitleKey), placeholders)
        player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle))
    }

    private fun parseMessage(message: String, placeholders: Map<String, Any>): Component {
        val resolvers = placeholders.map { (key, value) ->
            Placeholder.unparsed(key, value.toString())
        }.toTypedArray()
        return miniMessage.deserialize(message, TagResolver.resolver(resolvers))
    }
}
package dev.vintage.game

import dev.vintage.SkyFallBR
import dev.vintage.core.ConfigManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

object VoteManager {
    private val votes: MutableMap<UUID, GameMode> = mutableMapOf()
    private var votingActive = false
    private var nextMode: GameMode? = null

    fun startVoting() {
        votes.clear()
        votingActive = true
        val modes = ConfigManager.modeRotation
        Bukkit.getOnlinePlayers().forEach { player ->
            MessageUtil.sendMessage(player, "voting_started")
            // Send vote options, perhaps via GUI or chat
        }
        object : BukkitRunnable() {
            override fun run() {
                endVoting()
            }
        }.runTaskLater(SkyFallBR.instance, 6000L) // 5 minutes
    }

    fun vote(player: Player, mode: GameMode) {
        if (!votingActive) return
        votes[player.uniqueId] = mode
        MessageUtil.sendMessage(player, "vote_cast", mapOf("mode" to mode.name))
    }

    private fun endVoting() {
        votingActive = false
        val voteCounts = votes.values.groupingBy { it }.eachCount()
        nextMode = voteCounts.maxByOrNull { it.value }?.key ?: ConfigManager.modeRotation.first()
        MessageUtil.broadcast("next_mode", mapOf("mode" to nextMode!!.name))
    }

    fun getNextMode(): GameMode? = nextMode
}
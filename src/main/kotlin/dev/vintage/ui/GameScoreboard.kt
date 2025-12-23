package dev.vintage.ui

import dev.vintage.SkyFallBR
import dev.vintage.data.PlayerData
import dev.vintage.game.GameInstance
import dev.vintage.game.GameState
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.util.UUID

class GameScoreboard(private val game: GameInstance) {
    private val scoreboards: MutableMap<UUID, Scoreboard> = mutableMapOf()
    private val teams: MutableMap<String, Team> = mutableMapOf()
    private val teamColors = listOf(ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.PURPLE)
    private val updateTask: BukkitRunnable = object : BukkitRunnable() {
        override fun run() {
            game.players.forEach { updateScoreboard(it) }
        }
    }

    fun start() {
        updateTask.runTaskTimer(SkyFallBR.instance, 0L, 20L)
    }

    fun stop() {
        updateTask.cancel()
        scoreboards.clear()
        teams.clear()
    }

    fun addPlayer(uuid: UUID) {
        val player = Bukkit.getPlayer(uuid) ?: return
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective = scoreboard.registerNewObjective("game", "dummy", MessageUtil.getMessage("scoreboard_title"))
        objective.displaySlot = DisplaySlot.SIDEBAR
        scoreboards[uuid] = scoreboard
        player.scoreboard = scoreboard
        val teamId = game.getPlayerData(uuid).teamId ?: uuid.toString()
        val team = scoreboard.registerNewTeam(teamId)
        team.color = teamColors[game.teams.keys.indexOf(teamId) % teamColors.size]
        team.addEntry(player.name)
        teams[teamId] = team
    }

    fun removePlayer(uuid: UUID) {
        scoreboards.remove(uuid)
        val player = Bukkit.getPlayer(uuid)
        player?.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        val teamId = game.getPlayerData(uuid).teamId
        teams.remove(teamId)
    }

    private fun updateScoreboard(uuid: UUID) {
        val scoreboard = scoreboards[uuid] ?: return
        val objective = scoreboard.getObjective("game") ?: return
        val player = Bukkit.getPlayer(uuid) ?: return
        val data = game.getPlayerData(uuid)

        objective.displayName = MessageUtil.getMessage("scoreboard_title")
        clearScores(objective)

        when (game.getState()) {
            GameState.WAITING, GameState.STARTING -> {
                val playersCount = game.players.size
                val maxPlayers = game.arena.maxPlayers
                val waitingText = if (game.getState() == GameState.STARTING) {
                    val timeLeft = (game.startTime - System.currentTimeMillis()) / 1000
                    MessageUtil.getMessage("scoreboard_starting_in").replace("{time}", timeLeft.toString())
                } else {
                    MessageUtil.getMessage("scoreboard_waiting")
                }
                objective.getScore(MessageUtil.getMessage("scoreboard_players").replace("{current}", playersCount.toString()).replace("{max}", maxPlayers.toString())).score = 3
                objective.getScore(waitingText).score = 2
                objective.getScore("").score = 1
            }
            GameState.ACTIVE -> {
                val alive = game.players.size
                val kills = data.gameKills
                val zoneTime = game.zoneManager.getShrinkTimeLeft()
                val resources = "${data.wood}/${data.stone}/${data.metal}"
                objective.getScore(MessageUtil.getMessage("scoreboard_alive").replace("{count}", alive.toString())).score = 6
                objective.getScore(MessageUtil.getMessage("scoreboard_kills").replace("{kills}", kills.toString())).score = 5
                objective.getScore(MessageUtil.getMessage("scoreboard_zone").replace("{time}", zoneTime.toString())).score = 4
                objective.getScore(MessageUtil.getMessage("scoreboard_resources").replace("{resources}", resources)).score = 3
                objective.getScore(MessageUtil.getMessage("scoreboard_team").replace("{team}", data.teamId ?: "Solo")).score = 2
                objective.getScore("").score = 1
            }
            else -> {
            }
        }
    }

    private fun clearScores(objective: Objective) {
        objective.scoreboard.entries.forEach { objective.scoreboard.resetScores(it) }
    }

    private fun GameInstance.getState(): GameState {
        return when {
            winner != null -> GameState.ENDING
            startTime > 0 -> GameState.ACTIVE
            players.size >= arena.minPlayers -> GameState.STARTING
            else -> GameState.WAITING
        }
    }
}
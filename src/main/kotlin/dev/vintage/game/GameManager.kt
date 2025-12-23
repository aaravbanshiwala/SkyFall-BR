package dev.vintage.game

import dev.vintage.arena.Arena
import dev.vintage.data.PlayerData
import java.util.UUID

object GameManager {
    val activeGames: MutableList<GameInstance> = mutableListOf()
    val arenas: MutableList<Arena> = mutableListOf()
    val playerData: MutableMap<UUID, PlayerData> = mutableMapOf()

    fun createGame(arena: Arena): GameInstance {
        val game = GameInstance(id = UUID.randomUUID().toString(), arena = arena)
        activeGames.add(game)
        return game
    }

    fun startGame(game: GameInstance) {
        game.startGame()
    }

    fun stopGame(game: GameInstance) {
        game.endGame()
        activeGames.remove(game)
    }

    fun registerArena(arena: Arena) {
        arenas.add(arena)
    }

    fun getPlayerData(uuid: UUID): PlayerData = playerData.getOrPut(uuid) { PlayerData(uuid) }
}
package dev.vintage.arena

import dev.vintage.game.GameMode
import org.bukkit.Location

data class Arena(

    val id: String,

    var center: Location? = null,

    var initialRadius: Double = 200.0,

    var mode: GameMode = GameMode.SOLO

)
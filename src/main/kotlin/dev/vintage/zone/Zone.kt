package dev.vintage.zone

import org.bukkit.Location

enum class ZonePhase {

    SAFE,

    SHRINKING,

    DAMAGING

}

data class Zone(

    var currentCenter: Location,

    var currentRadius: Double,

    var nextCenter: Location? = null,

    var nextRadius: Double? = null,

    var phase: ZonePhase = ZonePhase.SAFE,

    var shrinkTimer: Int = 0,

    var damageTimer: Int = 0

)
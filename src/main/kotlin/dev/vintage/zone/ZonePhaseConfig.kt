package dev.vintage.zone

data class ZonePhaseConfig(
    val waitTime: Int,
    val shrinkTime: Int,
    val damagePerSecond: Double,
    val nextRadius: Double
)
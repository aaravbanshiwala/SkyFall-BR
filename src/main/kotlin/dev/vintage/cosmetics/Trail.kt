package dev.vintage.cosmetics

import org.bukkit.Particle

data class Trail(
    override val id: String,
    override val name: String,
    val particle: Particle,
    val pattern: String,
    val colors: List<String>,
    override val rarity: Rarity,
    override val unlockedBy: String
) : Cosmetic
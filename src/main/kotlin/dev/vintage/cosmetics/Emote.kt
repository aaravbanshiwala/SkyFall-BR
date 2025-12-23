package dev.vintage.cosmetics

data class Emote(
    override val id: String,
    override val name: String,
    val command: String,
    val duration: Int,
    override val rarity: Rarity,
    override val unlockedBy: String
) : Cosmetic
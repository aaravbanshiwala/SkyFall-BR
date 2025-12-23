package dev.vintage.cosmetics

data class KillMessage(
    override val id: String,
    override val name: String,
    val templates: List<String>,
    override val rarity: Rarity,
    override val unlockedBy: String
) : Cosmetic
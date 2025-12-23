package dev.vintage.cosmetics

data class DeathEffect(
    override val id: String,
    override val name: String,
    val effect: String,
    override val rarity: Rarity,
    override val unlockedBy: String
) : Cosmetic
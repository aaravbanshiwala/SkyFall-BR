package dev.vintage.cosmetics

enum class Rarity {
    COMMON, RARE, LEGENDARY
}

enum class CosmeticType {
    EMOTE, TRAIL, WEAPON_SKIN, DEATH_EFFECT, KILL_MESSAGE
}

interface Cosmetic {
    val id: String
    val name: String
    val rarity: Rarity
    val unlockedBy: String
}
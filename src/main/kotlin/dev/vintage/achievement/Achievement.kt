package dev.vintage.achievement

import dev.vintage.cosmetics.Rarity
import dev.vintage.data.PlayerData
import org.bukkit.entity.Player

enum class AchievementTier {
    BRONZE, SILVER, GOLD, PLATINUM
}

enum class AchievementCategory {
    COMBAT, SURVIVAL, BUILDING, LOOT, SPECIAL
}

interface AchievementCriteria {
    fun check(player: Player, data: PlayerData): Boolean
    fun getProgress(player: Player, data: PlayerData): Int
    fun getMaxProgress(): Int
}

data class AchievementReward(
    val xp: Int = 0,
    val cosmetics: List<String> = emptyList(),
    val title: String? = null
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val criteria: AchievementCriteria,
    val reward: AchievementReward,
    val isSecret: Boolean = false,
    val isSeasonal: Boolean = false,
    val event: String? = null
)
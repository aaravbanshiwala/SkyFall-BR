package dev.vintage.game

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.potion.PotionType

data class GameModeConfig(
    val teamSize: Int,
    val lootTables: Map<String, List<LootItem>>,
    val weaponRestrictions: List<Material>,
    val specialRules: List<String>,
    val winConditions: String,
    val zoneShrinkSpeed: Double,
    val rewards: Map<String, Int>
)

data class LootItem(
    val material: Material,
    val amount: Int,
    val enchantments: Map<Enchantment, Int> = emptyMap(),
    val potionType: PotionType? = null
)
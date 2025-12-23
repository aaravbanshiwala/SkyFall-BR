package dev.vintage.game

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.potion.PotionType

data class LootItem(
    val material: Material,
    val amount: Int,
    val enchantments: Map<Enchantment, Int> = emptyMap(),
    val potionType: PotionType? = null
)
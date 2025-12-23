package dev.vintage.cosmetics

import org.bukkit.Material

data class WeaponSkin(
    override val id: String,
    override val name: String,
    val weaponType: Material,
    val customModelData: Int,
    val texture: String,
    override val rarity: Rarity,
    override val unlockedBy: String
) : Cosmetic
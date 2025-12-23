package dev.vintage.building

import org.bukkit.Material

enum class BuildingMaterial(
    val health: Int,
    val buildTime: Int,
    val cost: Map<String, Int>,
    val blockTypes: List<Material>
) {
    WOOD(50, 5, mapOf("wood" to 10), listOf(Material.OAK_PLANKS, Material.SPRUCE_PLANKS)),
    STONE(100, 10, mapOf("stone" to 15), listOf(Material.STONE, Material.COBBLESTONE)),
    METAL(150, 15, mapOf("metal" to 20), listOf(Material.IRON_BLOCK, Material.GOLD_BLOCK))
}
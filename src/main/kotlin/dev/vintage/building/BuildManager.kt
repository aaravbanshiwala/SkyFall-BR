package dev.vintage.building

import dev.vintage.data.PlayerData
import dev.vintage.game.GameInstance
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class BuildManager(private val game: GameInstance) {
    private val structures: MutableMap<String, MutableList<PlacedStructure>> = mutableMapOf()

    fun openBuildMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, "Build Menu")
        BuildingMaterial.values().forEachIndexed { index, material ->
            val item = ItemStack(material.blockTypes.first())
            val meta = item.itemMeta
            meta?.setDisplayName(material.name)
            item.itemMeta = meta
            inventory.setItem(index, item)
        }
        player.openInventory(inventory)
    }

    fun handleInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val item = event.currentItem ?: return
        val material = BuildingMaterial.values().find { it.blockTypes.contains(item.type) } ?: return
        val playerData = game.getPlayerData(player.uniqueId)
        if (canAfford(playerData, material)) {
            deductCost(playerData, material)
            // Placeholder for placement logic
        }
        event.isCancelled = true
    }

    fun validatePlacement(player: Player, location: org.bukkit.Location, structureType: StructureType): Boolean {
        // Check if location is within zone and not obstructed
        return game.zone.currentRadius >= location.distance(game.zone.currentCenter) && location.block.type == Material.AIR
    }

    fun placeStructure(player: Player, location: org.bukkit.Location, structureType: StructureType, material: BuildingMaterial) {
        if (!validatePlacement(player, location, structureType)) return
        val placed = PlacedStructure(player.uniqueId, location, structureType, material)
        structures.getOrPut(player.uniqueId.toString()) { mutableListOf() }.add(placed)
        // Build the structure
        buildStructure(location, structureType, material)
    }

    fun editStructure(player: Player, structureId: String) {
        // Placeholder for editing
    }

    fun removeStructure(player: Player, structureId: String) {
        val playerStructures = structures[player.uniqueId.toString()] ?: return
        val structure = playerStructures.find { it.id == structureId } ?: return
        // Remove blocks
        removeBlocks(structure.location, structure.type)
        playerStructures.remove(structure)
    }

    private fun buildStructure(location: org.bukkit.Location, type: StructureType, material: BuildingMaterial) {
        // Implement building logic based on patterns
    }

    private fun removeBlocks(location: org.bukkit.Location, type: StructureType) {
        // Implement removal logic
    }

    private fun canAfford(playerData: PlayerData, material: BuildingMaterial): Boolean {
        return material.cost.all { (resource, amount) ->
            when (resource) {
                "wood" -> playerData.wood >= amount
                "stone" -> playerData.stone >= amount
                "metal" -> playerData.metal >= amount
                else -> false
            }
        }
    }

    private fun deductCost(playerData: PlayerData, material: BuildingMaterial) {
        material.cost.forEach { (resource, amount) ->
            when (resource) {
                "wood" -> playerData.wood -= amount
                "stone" -> playerData.stone -= amount
                "metal" -> playerData.metal -= amount
            }
        }
    }
}

data class PlacedStructure(
    val owner: java.util.UUID,
    val location: org.bukkit.Location,
    val type: StructureType,
    val material: BuildingMaterial,
    val id: String = java.util.UUID.randomUUID().toString()
)
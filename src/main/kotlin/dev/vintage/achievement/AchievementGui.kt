package dev.vintage.achievement

import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class AchievementGui(private val player: Player) {
    private val inventory: Inventory = Bukkit.createInventory(null, 54, "Achievements")
    private var currentCategory: AchievementCategory = AchievementCategory.COMBAT

    init {
        setupItems()
    }

    fun open() {
        player.openInventory(inventory)
    }

    private fun setupItems() {
        inventory.clear()
        val data = GameManager.getPlayerData(player.uniqueId)
        val categoryAchievements = AchievementManager.getAllAchievements().values.filter { (it.category == currentCategory) && (!it.isSecret || data.unlockedAchievements.contains(it.id)) }
        categoryAchievements.forEachIndexed { index, achievement ->
            val progress = achievement.criteria.getProgress(player, data)
            val max = achievement.criteria.getMaxProgress()
            val completed = data.unlockedAchievements.contains(achievement.id)
            val material = when {
                completed -> Material.LIME_DYE
                progress > 0 -> Material.YELLOW_DYE
                else -> Material.GRAY_DYE
            }
            val item = ItemStack(material)
            val meta = item.itemMeta
            meta?.displayName(MessageUtil.parseMessage("&e${achievement.name}", emptyMap()).toString())
            val lore = mutableListOf(
                "&7${achievement.description}",
                "&7Tier: ${achievement.tier.name}",
                if (completed) "&aCompleted" else "&7Progress: $progress/$max"
            )
            if (completed) {
                lore.add("&aRewards claimed")
            }
            meta?.lore = lore.map { MessageUtil.parseMessage(it, emptyMap()).toString() }
            item.itemMeta = meta
            inventory.setItem(index, item)
        }
        // Category buttons
        inventory.setItem(45, createCategoryItem(Material.IRON_SWORD, "Combat", AchievementCategory.COMBAT))
        inventory.setItem(46, createCategoryItem(Material.SHIELD, "Survival", AchievementCategory.SURVIVAL))
        inventory.setItem(47, createCategoryItem(Material.CRAFTING_TABLE, "Building", AchievementCategory.BUILDING))
        inventory.setItem(48, createCategoryItem(Material.CHEST, "Loot", AchievementCategory.LOOT))
        inventory.setItem(49, createCategoryItem(Material.NETHER_STAR, "Special", AchievementCategory.SPECIAL))
        // Completion percentage
        val percentage = AchievementManager.getCompletionPercentage(player)
        val item = ItemStack(Material.BOOK)
        val meta = item.itemMeta
        meta?.displayName(MessageUtil.parseMessage("&eCompletion: ${"%.1f".format(percentage)}%", emptyMap()).toString())
        item.itemMeta = meta
        inventory.setItem(53, item)
    }

    private fun createCategoryItem(material: Material, name: String, category: AchievementCategory): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.displayName(MessageUtil.parseMessage("&e$name", emptyMap()).toString())
        item.itemMeta = meta
        return item
    }

    fun handleClick(slot: Int) {
        when (slot) {
            45 -> { currentCategory = AchievementCategory.COMBAT; setupItems() }
            46 -> { currentCategory = AchievementCategory.SURVIVAL; setupItems() }
            47 -> { currentCategory = AchievementCategory.BUILDING; setupItems() }
            48 -> { currentCategory = AchievementCategory.LOOT; setupItems() }
            49 -> { currentCategory = AchievementCategory.SPECIAL; setupItems() }
            else -> {
                // Show detailed progress if clicked
            }
        }
    }
}
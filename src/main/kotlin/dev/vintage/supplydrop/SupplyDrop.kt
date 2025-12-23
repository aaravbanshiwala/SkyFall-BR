package dev.vintage.supplydrop

import dev.vintage.core.ConfigManager
import dev.vintage.game.GameInstance
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class SupplyDrop(private val game: GameInstance, private val location: Location) {
    lateinit var armorStand: ArmorStand
    private var openingPlayer: Player? = null
    private var openingTask: BukkitRunnable? = null

    fun spawn() {
        armorStand = location.world.spawn(location, ArmorStand::class.java) {
            it.isVisible = false
            it.isMarker = true
            it.customName = "§aSupply Drop"
            it.isCustomNameVisible = true
            it.setGravity(true)
        }
    }

    fun startOpening(player: Player) {
        if (openingPlayer != null) return
        openingPlayer = player
        openingTask = object : BukkitRunnable() {
            var timeLeft = ConfigManager.supplyDropOpeningTime
            override fun run() {
                if (timeLeft <= 0) {
                    openDrop()
                    cancel()
                } else {
                    MessageUtil.sendMessage(openingPlayer!!, "supply_drop_opening", mapOf("time" to timeLeft.toString()))
                    timeLeft--
                }
            }
        }
        openingTask?.runTaskTimer(Bukkit.getPluginManager().getPlugin("SkyFallBR")!!, 0L, 20L)
    }

    private fun openDrop() {
        val lootTable = game.modeConfig.lootTables["rare"] ?: ConfigManager.modeConfigs[game.arena.mode]?.lootTables?.get("rare") ?: emptyList()
        lootTable.forEach { item ->
            val itemStack = ItemStack(item.material, item.amount)
            item.enchantments.forEach { (ench, level) ->
                itemStack.addEnchantment(ench, level)
            }
            if (item.potionType != null) {
                // Assuming potion meta
                val meta = itemStack.itemMeta as org.bukkit.inventory.meta.PotionMeta
                meta.basePotionData = org.bukkit.potion.PotionData(item.potionType!!)
                itemStack.itemMeta = meta
            }
            location.world.dropItem(location, itemStack)
        }
        armorStand.remove()
        openingTask?.cancel()
    }
}
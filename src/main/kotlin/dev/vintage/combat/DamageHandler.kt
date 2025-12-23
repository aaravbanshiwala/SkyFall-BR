package dev.vintage.combat

import dev.vintage.core.ConfigManager
import dev.vintage.data.PlayerData
import dev.vintage.game.GameManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.random.Random

object DamageHandler {
    fun calculateDamage(attacker: Player?, victim: Player, baseDamage: Double, distance: Double = 0.0, isProjectile: Boolean = false, hitLocation: Location? = null): Double {
        var damage = baseDamage
        val weapon = attacker?.inventory?.itemInMainHand
        val armorValue = victim.inventory.armorContents.sumOf { getArmorValue(it) }
        val armorReduction = armorValue * ConfigManager.armorEffectiveness
        damage -= armorReduction
        if (isProjectile) {
            val falloff = ConfigManager.damageFalloff * distance
            damage = max(0.0, damage - falloff)
        }
        if (isHeadshot(hitLocation, victim)) {
            damage *= ConfigManager.headshotMultiplier
        }
        if (Random.nextDouble() < ConfigManager.criticalChance) {
            damage *= ConfigManager.criticalMultiplier
        }
        return max(0.0, damage)
    }

    fun isHeadshot(hitLocation: Location?, victim: Player): Boolean {
        if (hitLocation == null) return false
        val eyeLocation = victim.eyeLocation
        return hitLocation.y >= eyeLocation.y - 0.5 && hitLocation.y <= eyeLocation.y + 0.5
    }

    fun applyDamageIndicator(attacker: Player?, victim: Player, damage: Double, isCritical: Boolean = false) {
        val color = if (isCritical) "§6" else "§f"
        attacker?.let {
            val hologram = it.world.spawn(it.location.add(0.0, 2.0, 0.0), org.bukkit.entity.ArmorStand::class.java) {
                it.isVisible = false
                it.isMarker = true
                it.customName = "$color+${damage.toInt()}"
                it.isCustomNameVisible = true
                it.setGravity(false)
            }
            it.world.scheduler.runTaskLater(it.server.pluginManager.getPlugin("SkyFallBR")!!, Runnable {
                hologram.remove()
            }, 40L)
            GameManager.activeGames.find { game -> game.players.contains(attacker.uniqueId) }?.addHologram(hologram)
        }
        val victimHologram = victim.world.spawn(victim.location.add(0.0, 2.0, 0.0), org.bukkit.entity.ArmorStand::class.java) {
            it.isVisible = false
            it.isMarker = true
            it.customName = "§c-${damage.toInt()}"
            it.isCustomNameVisible = true
            it.setGravity(false)
        }
        victim.world.scheduler.runTaskLater(victim.server.pluginManager.getPlugin("SkyFallBR")!!, Runnable {
            victimHologram.remove()
        }, 40L)
        GameManager.activeGames.find { game -> game.players.contains(victim.uniqueId) }?.addHologram(victimHologram)
    }

    fun trackDamage(attacker: Player?, victim: Player, damage: Double, isHeadshot: Boolean) {
        attacker?.let {
            val attackerData = GameManager.getPlayerData(it.uniqueId)
            attackerData.damageDealt += damage
            if (isHeadshot) attackerData.headshots++
        }
        val victimData = GameManager.getPlayerData(victim.uniqueId)
        victimData.damageTaken += damage
    }

    private fun getArmorValue(item: ItemStack?): Double {
        if (item == null) return 0.0
        return when (item.type) {
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS -> 1.0
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS -> 2.0
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS -> 3.0
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS -> 4.0
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS -> 5.0
            else -> 0.0
        }
    }
}
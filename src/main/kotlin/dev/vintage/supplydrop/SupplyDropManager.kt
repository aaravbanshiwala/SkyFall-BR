package dev.vintage.supplydrop

import dev.vintage.SkyFallBR
import dev.vintage.building.BuildManager
import dev.vintage.core.ConfigManager
import dev.vintage.game.GameInstance
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class SupplyDropManager(private val game: GameInstance) {
    val activeDrops: MutableList<SupplyDrop> = mutableListOf()
    private var dropTask: BukkitRunnable? = null
    private var nextDropTime = 0

    fun start() {
        scheduleNextDrop()
    }

    fun stop() {
        dropTask?.cancel()
        activeDrops.forEach { it.armorStand.remove() }
        activeDrops.clear()
    }

    private fun scheduleNextDrop() {
        val interval = ConfigManager.supplyDropInterval * 20L
        val announcementTime = ConfigManager.supplyDropAnnouncementTime * 20L

        // Announcement
        Bukkit.getScheduler().runTaskLater(SkyFallBR.instance, Runnable {
            MessageUtil.broadcastToGame(game, "supply_drop_announcement", mapOf("time" to (ConfigManager.supplyDropAnnouncementTime).toString()))
            game.zoneManager.announceSupplyDrop()
        }, interval - announcementTime)

        dropTask = object : BukkitRunnable() {
            override fun run() {
                spawnDrop()
                scheduleNextDrop()
            }
        }
        dropTask?.runTaskLater(SkyFallBR.instance, interval)
    }

    fun getNextDropTime(): Int {
        return nextDropTime / 20
    }

    private fun spawnDrop() {
        val location = findSafeLocation()
        if (location != null) {
            val drop = SupplyDrop(game, location)
            activeDrops.add(drop)
            drop.spawn()
            MessageUtil.broadcastToGame(game, "supply_drop_spawned")
        }
    }

    private fun findSafeLocation(): Location? {
        val center = game.zone.currentCenter
        val radius = game.zone.currentRadius
        for (i in 0..50) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val distance = Random.nextDouble(0.0, radius)
            val x = center.x + distance * Math.cos(angle)
            val z = center.z + distance * Math.sin(angle)
            val y = center.world.getHighestBlockYAt(x.toInt(), z.toInt()) + 50.0
            val location = Location(center.world, x, y, z)
            if (isLocationSafe(location)) {
                return location
            }
        }
        return null
    }

    private fun isLocationSafe(location: Location): Boolean {
        // Check if no buildings nearby
        val nearbyStructures = game.buildManager.structures.values.flatten().filter {
            it.location.distance(location) < 10.0
        }
        return nearbyStructures.isEmpty() && location.block.type.isAir
    }

    fun handleOpening(player: org.bukkit.entity.Player, armorStand: org.bukkit.entity.ArmorStand) {
        val drop = activeDrops.find { it.armorStand == armorStand }
        drop?.startOpening(player)
    }
}
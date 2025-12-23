package dev.vintage.zone

import dev.vintage.SkyFallBR
import dev.vintage.core.ConfigManager
import dev.vintage.game.GameInstance
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class ZoneManager(private val game: GameInstance) {
    val zoneBossBar: BossBar = Bukkit.createBossBar("Zone", BarColor.PURPLE, BarStyle.SOLID)
    val gameTimerBossBar: BossBar = Bukkit.createBossBar("Game Time", BarColor.GREEN, BarStyle.SOLID)
    val supplyDropBossBar: BossBar = Bukkit.createBossBar("Supply Drop Incoming", BarColor.YELLOW, BarStyle.SOLID)
    private var currentPhaseIndex = 0
    private var phaseTimer = 0
    private var shrinkProgress = 0.0
    private var gameStartTime: Long = 0L
    private var supplyDropAnnounced = false
    private var particleBatch: MutableList<Pair<Location, Particle.DustOptions>> = mutableListOf()

    private val task = object : BukkitRunnable() {
        override fun run() {
            val startTime = System.nanoTime()
            updateZone()
            applyDamage()
            updateBossBars()
            spawnParticles()
            updateCompasses()
            playWarningSounds()
            val endTime = System.nanoTime()
            val duration = (endTime - startTime) / 1_000_000
            if (duration > ConfigManager.performanceWarningThreshold && ConfigManager.enablePerformanceWarnings) {
                SkyFallBR.instance.logger.warning("[SkyFallBR] Zone update took ${duration}ms for game ${game.id}")
            }
        }
    }

    fun start() {
        gameStartTime = System.currentTimeMillis()
        zoneBossBar.isVisible = true
        gameTimerBossBar.isVisible = true
        task.runTaskTimer(SkyFallBR.instance, 0L, ConfigManager.zoneUpdateTicks)
    }

    fun stop() {
        task.cancel()
        zoneBossBar.isVisible = false
        gameTimerBossBar.isVisible = false
        supplyDropBossBar.isVisible = false
        flushParticleBatch()
    }

    private fun updateZone() {
        val phases = ConfigManager.zonesPhases
        if (currentPhaseIndex >= phases.size) return
        val phase = phases[currentPhaseIndex]
        val shrinkSpeed = game.modeConfig.zoneShrinkSpeed
        when (game.zone.phase) {
            ZonePhase.SAFE -> {
                phaseTimer++
                if (phaseTimer >= phase.waitTime * 20) {
                    game.zone.phase = ZonePhase.SHRINKING
                    game.zone.nextRadius = phase.nextRadius
                    game.zone.nextCenter = calculateRandomCenter()
                    game.zone.shrinkTimer = (phase.shrinkTime * 20 / shrinkSpeed).toInt()
                    shrinkProgress = 0.0
                    phaseTimer = 0
                }
            }
            ZonePhase.SHRINKING -> {
                shrinkProgress += 1.0 / game.zone.shrinkTimer
                if (shrinkProgress >= 1.0) {
                    game.zone.currentRadius = game.zone.nextRadius!!
                    game.zone.currentCenter = game.zone.nextCenter!!
                    game.zone.phase = ZonePhase.DAMAGING
                    game.zone.damageTimer = 0
                } else {
                    val startRadius = if (currentPhaseIndex == 0) ConfigManager.zonesInitialRadius else phases[currentPhaseIndex - 1].nextRadius
                    game.zone.currentRadius = startRadius + (phase.nextRadius - startRadius) * shrinkProgress
                }
            }
            ZonePhase.DAMAGING -> {
                if (currentPhaseIndex < phases.size - 1) {
                    currentPhaseIndex++
                    game.zone.phase = ZonePhase.SAFE
                    phaseTimer = 0
                }
            }
        }
    }

    private fun calculateRandomCenter(): Location {
        val currentCenter = game.zone.currentCenter
        val maxOffset = game.zone.currentRadius * 0.5
        val offsetX = Random.nextDouble(-maxOffset, maxOffset)
        val offsetZ = Random.nextDouble(-maxOffset, maxOffset)
        return Location(currentCenter.world, currentCenter.x + offsetX, currentCenter.y, currentCenter.z + offsetZ)
    }

    private fun applyDamage() {
        if (game.zone.phase != ZonePhase.DAMAGING) return
        val damage = ConfigManager.zonesPhases.getOrNull(currentPhaseIndex)?.damagePerSecond ?: 0.0
        game.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
            if (isOutsideZone(player.location)) {
                player.damage(damage / 10.0)
            }
        }
    }

    private fun updateBossBars() {
        val zoneProgress = when (game.zone.phase) {
            ZonePhase.SAFE -> 1.0
            ZonePhase.SHRINKING -> 1.0 - shrinkProgress
            ZonePhase.DAMAGING -> 0.0
        }
        zoneBossBar.progress = zoneProgress.coerceIn(0.0, 1.0)
        val phaseText = when (game.zone.phase) {
            ZonePhase.SAFE -> "Safe"
            ZonePhase.SHRINKING -> "Shrinking"
            ZonePhase.DAMAGING -> "Damaging"
        }
        zoneBossBar.title = "Zone: $phaseText | Radius: ${game.zone.currentRadius.toInt()}"

        val elapsed = (System.currentTimeMillis() - gameStartTime) / 1000.0
        val maxTime = 1200.0
        gameTimerBossBar.progress = (elapsed / maxTime).coerceIn(0.0, 1.0)
        gameTimerBossBar.title = "Game Time: ${elapsed.toInt()}s"

        if (supplyDropAnnounced) {
            val timeLeft = game.supplyDropManager.getNextDropTime()
            if (timeLeft > 0) {
                supplyDropBossBar.progress = (timeLeft / 30.0).coerceIn(0.0, 1.0)
                supplyDropBossBar.title = "Supply Drop in: ${timeLeft}s"
                supplyDropBossBar.isVisible = true
            } else {
                supplyDropBossBar.isVisible = false
                supplyDropAnnounced = false
            }
        }
    }

    fun announceSupplyDrop() {
        supplyDropAnnounced = true
    }

    fun getShrinkTimeLeft(): Int {
        return if (game.zone.phase == ZonePhase.SHRINKING) {
            ((1.0 - shrinkProgress) * game.zone.shrinkTimer / 20).toInt()
        } else 0
    }

    private fun spawnParticles() {
        if (ConfigManager.particleDensity <= 0.0) return
        val world = game.arena.center?.world ?: return
        val center = game.zone.currentCenter
        val radius = game.zone.currentRadius
        val dustOptions = org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f)
        val particleCount = (36 * ConfigManager.particleDensity).toInt().coerceAtLeast(1)
        for (i in 0 until particleCount) {
            val angle = i * (360.0 / particleCount) * Math.PI / 180.0
            val x = center.x + radius * Math.cos(angle)
            val z = center.z + radius * Math.sin(angle)
            val location = Location(world, x, center.y, z)
            if (ConfigManager.batchParticleSpawning) {
                particleBatch.add(location to dustOptions)
                if (particleBatch.size >= 100) {
                    flushParticleBatch()
                }
            } else {
                world.spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions)
            }
        }
        if (!ConfigManager.batchParticleSpawning) flushParticleBatch()
    }

    private fun flushParticleBatch() {
        val world = game.arena.center?.world ?: return
        particleBatch.forEach { (location, dustOptions) ->
            world.spawnParticle(Particle.REDSTONE, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions)
        }
        particleBatch.clear()
    }

    private fun updateCompasses() {
        val center = game.zone.currentCenter
        game.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
            player.compassTarget = center
        }
    }

    private fun playWarningSounds() {
        if (game.zone.phase == ZonePhase.SAFE && phaseTimer >= (ConfigManager.zonesPhases.getOrNull(currentPhaseIndex)?.waitTime ?: 0) * 20 - 200) {
            game.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f)
            }
        }
    }

    private fun isOutsideZone(location: Location): Boolean {
        val center = game.zone.currentCenter
        val dx = location.x - center.x
        val dz = location.z - center.z
        val distance = Math.sqrt(dx * dx + dz * dz)
        return distance > game.zone.currentRadius
    }
}
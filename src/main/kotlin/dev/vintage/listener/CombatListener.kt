package dev.vintage.listener

import dev.vintage.combat.DamageHandler
import dev.vintage.core.ConfigManager
import dev.vintage.data.PlayerData
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.max
import kotlin.random.Random

class CombatListener : Listener {

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val victim = event.entity as? Player ?: return
        val attacker = when (val damager = event.damager) {
            is Player -> damager
            is org.bukkit.entity.Projectile -> damager.shooter as? Player
            else -> null
        } ?: return

        val game = GameManager.playerData[victim.uniqueId]?.currentGame ?: return
        if (game != GameManager.playerData[attacker.uniqueId]?.currentGame) return

        if (game.getTeam(attacker.uniqueId) == game.getTeam(victim.uniqueId)) {
            event.isCancelled = true
            return
        }

        // Weapon restrictions
        if (game.modeConfig.weaponRestrictions.contains(attacker.inventory.itemInMainHand.type)) {
            event.isCancelled = true
            MessageUtil.sendMessage(attacker, "weapon_restricted")
            return
        }

        val distance = if (event.damager is org.bukkit.entity.Projectile) attacker.location.distance(victim.location) else 0.0
        val isProjectile = event.damager is org.bukkit.entity.Projectile
        val hitLocation = event.damager.location
        val isHeadshot = DamageHandler.isHeadshot(hitLocation, victim)
        val isCritical = Random.nextDouble() < ConfigManager.criticalChance
        var damage = DamageHandler.calculateDamage(attacker, victim, event.damage, distance, isProjectile, hitLocation)

        // One-shot for snipers
        if (game.arena.mode == dev.vintage.game.GameMode.LTM_SNIPERS && isProjectile) {
            damage = victim.health + victim.absorptionAmount
        }

        event.damage = damage

        DamageHandler.applyDamageIndicator(attacker, victim, damage, isCritical)
        DamageHandler.trackDamage(attacker, victim, damage, isHeadshot)

        val data = game.getPlayerData(attacker.uniqueId)
        val eliminationsLeft = max(0, game.players.size - 1 - data.gameKills)
        attacker.sendActionBar("§c$eliminationsLeft eliminations left")
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player
        val game = GameManager.playerData[player.uniqueId]?.currentGame ?: return
        if (event.finalDamage >= player.health && !game.getPlayerData(player.uniqueId).isDowned) {
            event.isCancelled = true
            player.health = 0.5
            val data = game.getPlayerData(player.uniqueId)
            data.isDowned = true
            data.downedTime = System.currentTimeMillis()
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 600, 1))
            MessageUtil.sendMessage(player, "entered_downed")
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val victim = event.entity
        val game = GameManager.playerData[victim.uniqueId]?.currentGame ?: return
        val data = game.getPlayerData(victim.uniqueId)
        if (data.isDowned) {
            data.deaths++
            data.placement = game.players.size
            MessageUtil.sendTitle(victim, "death_title", "death_subtitle", mapOf("killer" to "Downed"))
            game.removePlayer(victim.uniqueId)
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        val player = event.player
        val game = GameManager.playerData[player.uniqueId]?.currentGame ?: return
        val target = player.getTargetEntity(3) as? Player ?: return
        if (game.getTeam(player.uniqueId) == game.getTeam(target.uniqueId) && game.getPlayerData(target.uniqueId).isDowned) {
            val data = game.getPlayerData(target.uniqueId)
            data.isDowned = false
            target.health = 10.0
            target.removePotionEffect(PotionEffectType.SLOW)
            MessageUtil.sendMessage(target, "revived")
            MessageUtil.sendMessage(player, "revived_teammate", mapOf("player" to target.name))
        }
    }
}
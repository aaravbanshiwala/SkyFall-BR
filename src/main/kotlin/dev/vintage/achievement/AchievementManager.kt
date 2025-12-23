package dev.vintage.achievement

import dev.vintage.SkyFallBR
import dev.vintage.cosmetics.CosmeticManager
import dev.vintage.data.PlayerData
import dev.vintage.data.StatsManager
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.scheduler.BukkitRunnable

object AchievementManager {
    private val achievements = mutableMapOf<String, Achievement>()

    fun init() {
        loadAchievements()
    }

    private fun loadAchievements() {
        achievements["first_blood"] = Achievement(
            id = "first_blood",
            name = "First Blood",
            description = "Get your first elimination",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.BRONZE,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.kills >= 1
                override fun getProgress(player: Player, data: PlayerData): Int = data.kills.coerceAtMost(1)
                override fun getMaxProgress(): Int = 1
            },
            reward = AchievementReward(cosmetics = listOf("emote_default"))
        )
        achievements["victory_royale"] = Achievement(
            id = "victory_royale",
            name = "Victory Royale",
            description = "Win your first game",
            category = AchievementCategory.SURVIVAL,
            tier = AchievementTier.GOLD,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.placement == 1
                override fun getProgress(player: Player, data: PlayerData): Int = if (data.placement == 1) 1 else 0
                override fun getMaxProgress(): Int = 1
            },
            reward = AchievementReward(cosmetics = listOf("trail_victory"))
        )
        achievements["sharp_shooter"] = Achievement(
            id = "sharp_shooter",
            name = "Sharp Shooter",
            description = "Get 5 headshots in one game",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.SILVER,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.headshots >= 5
                override fun getProgress(player: Player, data: PlayerData): Int = data.headshots.coerceAtMost(5)
                override fun getMaxProgress(): Int = 5
            },
            reward = AchievementReward(cosmetics = listOf("weapon_skin_sniper"))
        )
        achievements["builder"] = Achievement(
            id = "builder",
            name = "Builder",
            description = "Place 100 structures in one game",
            category = AchievementCategory.BUILDING,
            tier = AchievementTier.SILVER,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.structuresPlaced >= 100
                override fun getProgress(player: Player, data: PlayerData): Int = data.structuresPlaced.coerceAtMost(100)
                override fun getMaxProgress(): Int = 100
            },
            reward = AchievementReward(cosmetics = listOf("emote_builder"))
        )
        achievements["survivor"] = Achievement(
            id = "survivor",
            name = "Survivor",
            description = "Survive to top 10 without killing anyone",
            category = AchievementCategory.SURVIVAL,
            tier = AchievementTier.GOLD,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.placement <= 10 && data.gameKills == 0
                override fun getProgress(player: Player, data: PlayerData): Int = if (data.placement <= 10 && data.gameKills == 0) 1 else 0
                override fun getMaxProgress(): Int = 1
            },
            reward = AchievementReward(cosmetics = listOf("trail_survivor"))
        )
        achievements["arsenal"] = Achievement(
            id = "arsenal",
            name = "Arsenal",
            description = "Obtain all weapon types in one game",
            category = AchievementCategory.LOOT,
            tier = AchievementTier.PLATINUM,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.obtainedWeapons.size >= 5 // assume 5 types
                override fun getProgress(player: Player, data: PlayerData): Int = data.obtainedWeapons.size.coerceAtMost(5)
                override fun getMaxProgress(): Int = 5
            },
            reward = AchievementReward(cosmetics = listOf("death_effect_arsenal"))
        )
        achievements["hot_drop"] = Achievement(
            id = "hot_drop",
            name = "Hot Drop",
            description = "Get 3 kills within 2 minutes",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.GOLD,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.killsInTime >= 3
                override fun getProgress(player: Player, data: PlayerData): Int = data.killsInTime.coerceAtMost(3)
                override fun getMaxProgress(): Int = 3
            },
            reward = AchievementReward(cosmetics = listOf("kill_message_hot"))
        )
        achievements["long_shot"] = Achievement(
            id = "long_shot",
            name = "Long Shot",
            description = "Eliminate from 100+ blocks away",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.SILVER,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.longestEliminationDistance >= 100
                override fun getProgress(player: Player, data: PlayerData): Int = if (data.longestEliminationDistance >= 100) 1 else 0
                override fun getMaxProgress(): Int = 1
            },
            reward = AchievementReward(cosmetics = listOf("emote_longshot"))
        )
        // Add secret one
        achievements["secret_noob"] = Achievement(
            id = "secret_noob",
            name = "Secret: Noob",
            description = "Die without moving",
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.BRONZE,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.deaths >= 1 && data.distanceMoved == 0.0
                override fun getProgress(player: Player, data: PlayerData): Int = if (data.deaths >= 1 && data.distanceMoved == 0.0) 1 else 0
                override fun getMaxProgress(): Int = 1
            },
            reward = AchievementReward(cosmetics = listOf("emote_noob")),
            isSecret = true
        )
        // Seasonal achievements
        achievements["winter_warrior"] = Achievement(
            id = "winter_warrior",
            name = "Winter Warrior",
            description = "Win a game during winter season",
            category = AchievementCategory.SURVIVAL,
            tier = AchievementTier.GOLD,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.placement == 1 && data.seasonWins.contains("winter")
                override fun getProgress(player: Player, data: PlayerData): Int = if (data.placement == 1 && data.seasonWins.contains("winter")) 1 else 0
                override fun getMaxProgress(): Int = 1
            },
            reward = AchievementReward(cosmetics = listOf("trail_winter")),
            isSeasonal = true,
            event = "winter"
        )
        achievements["halloween_spirit"] = Achievement(
            id = "halloween_spirit",
            name = "Halloween Spirit",
            description = "Get 10 kills in a Halloween event game",
            category = AchievementCategory.COMBAT,
            tier = AchievementTier.SILVER,
            criteria = object : AchievementCriteria {
                override fun check(player: Player, data: PlayerData): Boolean = data.eventKills >= 10 && data.currentEvent == "halloween"
                override fun getProgress(player: Player, data: PlayerData): Int = data.eventKills.coerceAtMost(10)
                override fun getMaxProgress(): Int = 10
            },
            reward = AchievementReward(cosmetics = listOf("death_effect_halloween")),
            isSeasonal = true,
            event = "halloween"
        )
    }

    fun checkAchievements(player: Player) {
        val data = GameManager.getPlayerData(player.uniqueId)
        achievements.values.forEach { achievement ->
            if (!data.unlockedAchievements.contains(achievement.id) && achievement.criteria.check(player, data)) {
                unlockAchievement(player, achievement)
            }
        }
    }

    private fun unlockAchievement(player: Player, achievement: Achievement) {
        val data = GameManager.getPlayerData(player.uniqueId)
        data.unlockedAchievements.add(achievement.id)
        // Give rewards
        achievement.reward.cosmetics.forEach { cosmeticId ->
            CosmeticManager.unlockCosmetic(player, cosmeticId)
        }
        // Notifications
        MessageUtil.sendMessage(player, "achievement_unlocked", mapOf("achievement" to achievement.name))
        if (achievement.tier == AchievementTier.GOLD || achievement.tier == AchievementTier.PLATINUM) {
            MessageUtil.broadcastToGame(data.currentGame!!, "achievement_broadcast", mapOf("player" to player.name, "achievement" to achievement.name))
        }
        // Firework
        launchFirework(player)
        // Toast notification
        MessageUtil.sendTitle(player, "achievement_title", "achievement_subtitle", mapOf("achievement" to achievement.name))
    }

    private fun launchFirework(player: Player) {
        val firework = player.world.spawnEntity(player.location, EntityType.FIREWORK) as Firework
        val meta = firework.fireworkMeta
        meta.addEffect(FireworkEffect.builder().withColor(Color.GREEN).with(FireworkEffect.Type.BALL_LARGE).build())
        meta.power = 1
        firework.fireworkMeta = meta
        object : BukkitRunnable() {
            override fun run() {
                firework.detonate()
            }
        }.runTaskLater(SkyFallBR.instance, 20L)
    }

    fun getAllAchievements(): Map<String, Achievement> = achievements

    fun getAchievement(id: String): Achievement? = achievements[id]

    fun getCompletionPercentage(player: Player): Double {
        val data = GameManager.getPlayerData(player.uniqueId)
        val total = achievements.size
        val unlocked = data.unlockedAchievements.size
        return if (total > 0) (unlocked.toDouble() / total) * 100 else 0.0
    }

    fun getLeaderboard(): List<Pair<String, Int>> {
        val players = Bukkit.getOnlinePlayers().map { it.uniqueId to GameManager.getPlayerData(it.uniqueId).unlockedAchievements.size }
        return players.sortedByDescending { it.second }.map { StatsManager.getPlayerName(it.first.toString()) to it.second }
    }
}
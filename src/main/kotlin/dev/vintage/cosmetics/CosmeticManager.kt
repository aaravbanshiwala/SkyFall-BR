package dev.vintage.cosmetics

import dev.vintage.SkyFallBR
import dev.vintage.core.ConfigManager
import dev.vintage.data.DatabaseManager
import dev.vintage.data.PlayerData
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object CosmeticManager {
    private lateinit var cosmeticsConfig: YamlConfiguration
    private val emotes = mutableMapOf<String, Emote>()
    private val trails = mutableMapOf<String, Trail>()
    private val weaponSkins = mutableMapOf<String, WeaponSkin>()
    private val deathEffects = mutableMapOf<String, DeathEffect>()
    private val killMessages = mutableMapOf<String, KillMessage>()

    fun init() {
        val cosmeticsFile = File(SkyFallBR.instance.dataFolder, "cosmetics.yml")
        if (!cosmeticsFile.exists()) {
            SkyFallBR.instance.saveResource("cosmetics.yml", false)
        }
        cosmeticsConfig = YamlConfiguration.loadConfiguration(cosmeticsFile)
        loadCosmetics()
    }

    private fun loadCosmetics() {
        cosmeticsConfig.getConfigurationSection("emotes")?.getKeys(false)?.forEach { id ->
            val section = cosmeticsConfig.getConfigurationSection("emotes.$id")!!
            emotes[id] = Emote(
                id = id,
                name = section.getString("name")!!,
                command = section.getString("command")!!,
                duration = section.getInt("duration"),
                rarity = Rarity.valueOf(section.getString("rarity")!!.uppercase()),
                unlockedBy = section.getString("unlockedBy")!!
            )
        }
        cosmeticsConfig.getConfigurationSection("trails")?.getKeys(false)?.forEach { id ->
            val section = cosmeticsConfig.getConfigurationSection("trails.$id")!!
            trails[id] = Trail(
                id = id,
                name = section.getString("name")!!,
                particle = org.bukkit.Particle.valueOf(section.getString("particle")!!),
                pattern = section.getString("pattern")!!,
                colors = section.getStringList("colors"),
                rarity = Rarity.valueOf(section.getString("rarity")!!.uppercase()),
                unlockedBy = section.getString("unlockedBy")!!
            )
        }
        cosmeticsConfig.getConfigurationSection("weaponSkins")?.getKeys(false)?.forEach { id ->
            val section = cosmeticsConfig.getConfigurationSection("weaponSkins.$id")!!
            weaponSkins[id] = WeaponSkin(
                id = id,
                name = section.getString("name")!!,
                weaponType = org.bukkit.Material.valueOf(section.getString("weaponType")!!),
                customModelData = section.getInt("customModelData"),
                texture = section.getString("texture")!!,
                rarity = Rarity.valueOf(section.getString("rarity")!!.uppercase()),
                unlockedBy = section.getString("unlockedBy")!!
            )
        }
        cosmeticsConfig.getConfigurationSection("deathEffects")?.getKeys(false)?.forEach { id ->
            val section = cosmeticsConfig.getConfigurationSection("deathEffects.$id")!!
            deathEffects[id] = DeathEffect(
                id = id,
                name = section.getString("name")!!,
                effect = section.getString("effect")!!,
                rarity = Rarity.valueOf(section.getString("rarity")!!.uppercase()),
                unlockedBy = section.getString("unlockedBy")!!
            )
        }
        cosmeticsConfig.getConfigurationSection("killMessages")?.getKeys(false)?.forEach { id ->
            val section = cosmeticsConfig.getConfigurationSection("killMessages.$id")!!
            killMessages[id] = KillMessage(
                id = id,
                name = section.getString("name")!!,
                templates = section.getStringList("templates"),
                rarity = Rarity.valueOf(section.getString("rarity")!!.uppercase()),
                unlockedBy = section.getString("unlockedBy")!!
            )
        }
    }

    fun getEmote(id: String): Emote? = emotes[id]
    fun getTrail(id: String): Trail? = trails[id]
    fun getWeaponSkin(id: String): WeaponSkin? = weaponSkins[id]
    fun getDeathEffect(id: String): DeathEffect? = deathEffects[id]
    fun getKillMessage(id: String): KillMessage? = killMessages[id]

    fun getAllEmotes(): Map<String, Emote> = emotes
    fun getAllTrails(): Map<String, Trail> = trails
    fun getAllWeaponSkins(): Map<String, WeaponSkin> = weaponSkins
    fun getAllDeathEffects(): Map<String, DeathEffect> = deathEffects
    fun getAllKillMessages(): Map<String, KillMessage> = killMessages

    fun isOwned(player: Player, cosmeticId: String): Boolean {
        val data = dev.vintage.game.GameManager.getPlayerData(player.uniqueId)
        return data.ownedCosmetics.contains(cosmeticId)
    }

    fun unlockCosmetic(player: Player, cosmeticId: String) {
        val data = dev.vintage.game.GameManager.getPlayerData(player.uniqueId)
        data.ownedCosmetics.add(cosmeticId)
        DatabaseManager.savePlayerStats(DatabaseManager.loadPlayerStats(player.uniqueId).apply {
            ownedCosmetics = data.ownedCosmetics
        })
    }

    fun equipCosmetic(player: Player, type: CosmeticType, id: String) {
        val data = dev.vintage.game.GameManager.getPlayerData(player.uniqueId)
        when (type) {
            CosmeticType.EMOTE -> data.equippedEmote = id
            CosmeticType.TRAIL -> data.equippedTrail = id
            CosmeticType.WEAPON_SKIN -> data.equippedWeaponSkin = id
            CosmeticType.DEATH_EFFECT -> data.equippedDeathEffect = id
            CosmeticType.KILL_MESSAGE -> data.equippedKillMessage = id
        }
        DatabaseManager.savePlayerStats(DatabaseManager.loadPlayerStats(player.uniqueId).apply {
            equippedEmote = data.equippedEmote
            equippedTrail = data.equippedTrail
            equippedWeaponSkin = data.equippedWeaponSkin
            equippedDeathEffect = data.equippedDeathEffect
            equippedKillMessage = data.equippedKillMessage
        })
    }

    fun applyEmote(player: Player, emote: Emote) {
        player.performCommand(emote.command)
    }

    fun applyTrail(player: Player, trail: Trail) {
        // Implement trail logic, e.g., spawn particles
    }

    fun applyWeaponSkin(item: org.bukkit.inventory.ItemStack, skin: WeaponSkin) {
        val meta = item.itemMeta
        meta?.setCustomModelData(skin.customModelData)
        item.itemMeta = meta
    }

    fun applyDeathEffect(player: Player, effect: DeathEffect) {
        // Implement death effect, e.g., particles or sounds
    }

    fun getKillMessage(killMessage: KillMessage, killer: String, victim: String): String {
        val template = killMessage.templates.random()
        return template.replace("{killer}", killer).replace("{victim}", victim)
    }
}
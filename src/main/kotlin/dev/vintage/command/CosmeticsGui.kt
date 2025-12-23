package dev.vintage.command

import dev.vintage.cosmetics.*
import dev.vintage.game.GameManager
import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class CosmeticsGui(private val player: Player) {
    private val inventory: Inventory = Bukkit.createInventory(null, 54, "Cosmetics Menu")
    private var currentCategory: CosmeticType = CosmeticType.EMOTE

    init {
        setupItems()
    }

    fun open() {
        player.openInventory(inventory)
    }

    private fun setupItems() {
        inventory.clear()
        val data = GameManager.getPlayerData(player.uniqueId)
        when (currentCategory) {
            CosmeticType.EMOTE -> {
                CosmeticManager.getAllEmotes().values.forEachIndexed { index, emote ->
                    val item = ItemStack(Material.DIAMOND)
                    val meta = item.itemMeta
                    meta?.displayName(MessageUtil.parseMessage("&e${emote.name}", emptyMap()).toString())
                    meta?.lore = listOf(
                        "&7Rarity: ${emote.rarity.name}",
                        if (data.ownedCosmetics.contains(emote.id)) "&aOwned" else "&cLocked",
                        if (data.equippedEmote == emote.id) "&6Equipped" else "&7Click to equip"
                    ).map { MessageUtil.parseMessage(it, emptyMap()).toString() }
                    item.itemMeta = meta
                    inventory.setItem(index, item)
                }
            }
            CosmeticType.TRAIL -> {
                CosmeticManager.getAllTrails().values.forEachIndexed { index, trail ->
                    val item = ItemStack(Material.FEATHER)
                    val meta = item.itemMeta
                    meta?.displayName(MessageUtil.parseMessage("&e${trail.name}", emptyMap()).toString())
                    meta?.lore = listOf(
                        "&7Rarity: ${trail.rarity.name}",
                        if (data.ownedCosmetics.contains(trail.id)) "&aOwned" else "&cLocked",
                        if (data.equippedTrail == trail.id) "&6Equipped" else "&7Click to equip"
                    ).map { MessageUtil.parseMessage(it, emptyMap()).toString() }
                    item.itemMeta = meta
                    inventory.setItem(index, item)
                }
            }
            CosmeticType.WEAPON_SKIN -> {
                CosmeticManager.getAllWeaponSkins().values.forEachIndexed { index, skin ->
                    val item = ItemStack(skin.weaponType)
                    val meta = item.itemMeta
                    meta?.displayName(MessageUtil.parseMessage("&e${skin.name}", emptyMap()).toString())
                    meta?.lore = listOf(
                        "&7Rarity: ${skin.rarity.name}",
                        if (data.ownedCosmetics.contains(skin.id)) "&aOwned" else "&cLocked",
                        if (data.equippedWeaponSkin == skin.id) "&6Equipped" else "&7Click to equip"
                    ).map { MessageUtil.parseMessage(it, emptyMap()).toString() }
                    item.itemMeta = meta
                    inventory.setItem(index, item)
                }
            }
            CosmeticType.DEATH_EFFECT -> {
                CosmeticManager.getAllDeathEffects().values.forEachIndexed { index, effect ->
                    val item = ItemStack(Material.TNT)
                    val meta = item.itemMeta
                    meta?.displayName(MessageUtil.parseMessage("&e${effect.name}", emptyMap()).toString())
                    meta?.lore = listOf(
                        "&7Rarity: ${effect.rarity.name}",
                        if (data.ownedCosmetics.contains(effect.id)) "&aOwned" else "&cLocked",
                        if (data.equippedDeathEffect == effect.id) "&6Equipped" else "&7Click to equip"
                    ).map { MessageUtil.parseMessage(it, emptyMap()).toString() }
                    item.itemMeta = meta
                    inventory.setItem(index, item)
                }
            }
            CosmeticType.KILL_MESSAGE -> {
                CosmeticManager.getAllKillMessages().values.forEachIndexed { index, message ->
                    val item = ItemStack(Material.PAPER)
                    val meta = item.itemMeta
                    meta?.displayName(MessageUtil.parseMessage("&e${message.name}", emptyMap()).toString())
                    meta?.lore = listOf(
                        "&7Rarity: ${message.rarity.name}",
                        if (data.ownedCosmetics.contains(message.id)) "&aOwned" else "&cLocked",
                        if (data.equippedKillMessage == message.id) "&6Equipped" else "&7Click to equip"
                    ).map { MessageUtil.parseMessage(it, emptyMap()).toString() }
                    item.itemMeta = meta
                    inventory.setItem(index, item)
                }
            }
        }
        // Category buttons
        inventory.setItem(45, createCategoryItem(Material.DIAMOND, "Emotes", CosmeticType.EMOTE))
        inventory.setItem(46, createCategoryItem(Material.FEATHER, "Trails", CosmeticType.TRAIL))
        inventory.setItem(47, createCategoryItem(Material.IRON_SWORD, "Weapon Skins", CosmeticType.WEAPON_SKIN))
        inventory.setItem(48, createCategoryItem(Material.TNT, "Death Effects", CosmeticType.DEATH_EFFECT))
        inventory.setItem(49, createCategoryItem(Material.PAPER, "Kill Messages", CosmeticType.KILL_MESSAGE))
    }

    private fun createCategoryItem(material: Material, name: String, type: CosmeticType): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta
        meta?.displayName(MessageUtil.parseMessage("&e$name", emptyMap()).toString())
        item.itemMeta = meta
        return item
    }

    fun handleClick(slot: Int) {
        when (slot) {
            45 -> { currentCategory = CosmeticType.EMOTE; setupItems() }
            46 -> { currentCategory = CosmeticType.TRAIL; setupItems() }
            47 -> { currentCategory = CosmeticType.WEAPON_SKIN; setupItems() }
            48 -> { currentCategory = CosmeticType.DEATH_EFFECT; setupItems() }
            49 -> { currentCategory = CosmeticType.KILL_MESSAGE; setupItems() }
            else -> {
                val data = GameManager.getPlayerData(player.uniqueId)
                val cosmetics = when (currentCategory) {
                    CosmeticType.EMOTE -> CosmeticManager.getAllEmotes().values.toList()
                    CosmeticType.TRAIL -> CosmeticManager.getAllTrails().values.toList()
                    CosmeticType.WEAPON_SKIN -> CosmeticManager.getAllWeaponSkins().values.toList()
                    CosmeticType.DEATH_EFFECT -> CosmeticManager.getAllDeathEffects().values.toList()
                    CosmeticType.KILL_MESSAGE -> CosmeticManager.getAllKillMessages().values.toList()
                }
                if (slot < cosmetics.size) {
                    val cosmetic = cosmetics[slot]
                    if (data.ownedCosmetics.contains(cosmetic.id)) {
                        CosmeticManager.equipCosmetic(player, currentCategory, cosmetic.id)
                        MessageUtil.sendMessage(player, "cosmetic_equipped", mapOf("cosmetic" to cosmetic.name))
                        setupItems()
                    } else {
                        MessageUtil.sendMessage(player, "cosmetic_locked")
                    }
                }
            }
        }
    }
}
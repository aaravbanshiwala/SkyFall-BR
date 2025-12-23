package dev.vintage.party

import dev.vintage.utils.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object PartyManager {
    val parties: MutableMap<String, Party> = mutableMapOf()
    val invites: MutableMap<UUID, String> = mutableMapOf()

    fun createParty(leader: Player): Party {
        val party = Party(leader = leader.uniqueId)
        parties[party.id] = party
        MessageUtil.sendMessage(leader, "party_created")
        return party
    }

    fun disbandParty(partyId: String) {
        val party = parties[partyId] ?: return
        party.members.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            player?.let { MessageUtil.sendMessage(it, "party_disbanded") }
        }
        parties.remove(partyId)
    }

    fun invitePlayer(partyId: String, inviter: Player, invited: Player) {
        val party = parties[partyId] ?: return
        if (party.members.size >= party.maxSize) {
            MessageUtil.sendMessage(inviter, "party_full")
            return
        }
        if (invites.containsKey(invited.uniqueId)) {
            MessageUtil.sendMessage(inviter, "already_invited")
            return
        }
        invites[invited.uniqueId] = partyId
        MessageUtil.sendMessage(inviter, "invite_sent", mapOf("player" to invited.name))
        MessageUtil.sendMessage(invited, "invite_received", mapOf("player" to invited.name))
    }

    fun acceptInvite(player: Player) {
        val partyId = invites[player.uniqueId] ?: run {
            MessageUtil.sendMessage(player, "no_invite")
            return
        }
        val party = parties[partyId] ?: return
        if (party.members.size >= party.maxSize) {
            MessageUtil.sendMessage(player, "party_full")
            invites.remove(player.uniqueId)
            return
        }
        party.members.add(player.uniqueId)
        invites.remove(player.uniqueId)
        MessageUtil.sendMessage(player, "joined_party")
        party.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
            MessageUtil.sendMessage(it, "player_joined_party", mapOf("player" to player.name))
        }
    }

    fun leaveParty(player: Player) {
        val party = getPlayerParty(player.uniqueId) ?: return
        party.members.remove(player.uniqueId)
        if (party.members.isEmpty()) {
            parties.remove(party.id)
        } else if (party.leader == player.uniqueId) {
            party.leader = party.members.first()
            val newLeader = Bukkit.getPlayer(party.leader)
            newLeader?.let { MessageUtil.sendMessage(it, "promoted_to_leader") }
        }
        MessageUtil.sendMessage(player, "left_party")
        party.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
            MessageUtil.sendMessage(it, "player_left_party", mapOf("player" to player.name))
        }
    }

    fun kickPlayer(partyId: String, kicker: Player, kicked: Player) {
        val party = parties[partyId] ?: return
        if (party.leader != kicker.uniqueId) {
            MessageUtil.sendMessage(kicker, "not_leader")
            return
        }
        if (!party.members.contains(kicked.uniqueId)) return
        party.members.remove(kicked.uniqueId)
        MessageUtil.sendMessage(kicked, "kicked_from_party")
        MessageUtil.sendMessage(kicker, "kicked_player", mapOf("player" to kicked.name))
        party.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
            MessageUtil.sendMessage(it, "player_kicked", mapOf("player" to kicked.name))
        }
    }

    fun getPlayerParty(uuid: UUID): Party? = parties.values.find { it.members.contains(uuid) }

    fun sendPartyMessage(player: Player, message: String) {
        val party = getPlayerParty(player.uniqueId) ?: return
        val formatted = "§d[Party] §f${player.name}: $message"
        party.members.mapNotNull { Bukkit.getPlayer(it) }.forEach {
            it.sendMessage(formatted)
        }
    }
}
package dev.vintage.party

import java.util.UUID

data class Party(
    val id: String = UUID.randomUUID().toString(),
    var leader: UUID,
    val members: MutableList<UUID> = mutableListOf(leader),
    val maxSize: Int = 4,
    var chatChannel: String = "party"
)
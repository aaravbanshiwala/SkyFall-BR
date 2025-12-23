package dev.vintage.building

enum class StructureType(
    val patterns: List<String>,
    val rotation: Boolean,
    val dimensions: Triple<Int, Int, Int>
) {
    WALL(listOf("XXX", "XXX", "XXX"), true, Triple(3, 3, 1)),
    TOWER(listOf("X", "X", "X"), false, Triple(1, 3, 1)),
    HOUSE(listOf("XXX", "X X", "XXX"), true, Triple(3, 3, 3))
}
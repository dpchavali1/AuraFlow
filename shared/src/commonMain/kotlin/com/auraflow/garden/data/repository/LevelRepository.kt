package com.auraflow.garden.data.repository

import com.auraflow.garden.data.model.Level
import com.auraflow.garden.data.model.LevelNode
import com.auraflow.garden.data.model.LevelPoint
import com.auraflow.garden.data.model.Node
import com.auraflow.garden.data.model.NodeColor
import com.auraflow.garden.data.model.StarThresholds
import com.auraflow.garden.data.model.WorldType
import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.json.Json

class LevelRepository {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadLevel(stageId: Int): Level {
        // Phase 2: return hardcoded sample levels for stages 1-5
        // Phase 8+: load from JSON resources or procedural generation
        return getSampleLevel(stageId)
    }

    fun levelToNodes(level: Level): List<Node> {
        return level.nodes.map { ln ->
            Node(
                id = ln.id,
                colorType = NodeColor.valueOf(ln.color.uppercase()),
                position = Offset(ln.x, ln.y),
                isPressureNode = ln.isPressureNode,
                pressureDurationMs = ln.pressureDurationMs,
                movementPath = ln.movementPath?.map { Offset(it.x, it.y) },
                movementSpeedDps = ln.movementSpeedDps,
                pairedNodeId = ln.pairedNodeId,
            )
        }
    }

    fun parseLevelJson(jsonString: String): Level {
        return json.decodeFromString<Level>(jsonString)
    }

    private fun getSampleLevel(stageId: Int): Level {
        return when (stageId) {
            1 -> Level(
                stageId = 1,
                world = WorldType.WHISPERING_MEADOW,
                title = "First Light",
                maxEnergy = 100f,
                // With distance*30 formula: violet pair costs ~21.6, teal pair costs ~18 → ~60% remains
                starThresholds = StarThresholds(oneStar = 0.3f, twoStar = 0.5f, threeStar = 0.6f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.3f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.7f, "n1"),
                    LevelNode("n3", "TEAL", 0.5f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.5f, 0.8f, "n3"),
                ),
            )
            2 -> Level(
                stageId = 2,
                world = WorldType.WHISPERING_MEADOW,
                title = "Morning Dew",
                maxEnergy = 120f,
                // Optimal cost ~60/120 → ~50% remaining. threeStar slightly below optimal.
                starThresholds = StarThresholds(oneStar = 0.25f, twoStar = 0.40f, threeStar = 0.48f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.25f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.75f, "n1"),
                    LevelNode("n3", "TEAL", 0.3f, 0.6f, "n4"),
                    LevelNode("n4", "TEAL", 0.7f, 0.4f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.15f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.85f, "n5"),
                ),
            )
            3 -> Level(
                stageId = 3,
                world = WorldType.WHISPERING_MEADOW,
                title = "Tangled Roots",
                maxEnergy = 120f,
                energyCostMultiplier = 1.1f,
                // Optimal cost ~78/120 → ~35% remaining.
                starThresholds = StarThresholds(oneStar = 0.2f, twoStar = 0.28f, threeStar = 0.33f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.25f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.75f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.75f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.25f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                ),
                narratorIntro = "Roots weave and cross. Connect them all.",
            )
            4 -> Level(
                stageId = 4,
                world = WorldType.WHISPERING_MEADOW,
                title = "Petal Dance",
                maxEnergy = 150f,
                // 4 pairs, optimal cost ~91/150 → ~39% remaining.
                starThresholds = StarThresholds(oneStar = 0.2f, twoStar = 0.30f, threeStar = 0.37f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.3f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.7f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.7f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.3f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.1f, 0.5f, "n6"),
                    LevelNode("n6", "ROSE", 0.9f, 0.5f, "n5"),
                    LevelNode("n7", "AMBER", 0.5f, 0.1f, "n8"),
                    LevelNode("n8", "AMBER", 0.5f, 0.9f, "n7"),
                ),
            )
            5 -> Level(
                stageId = 5,
                world = WorldType.WHISPERING_MEADOW,
                title = "Guardian's Test",
                maxEnergy = 150f,
                energyCostMultiplier = 1.1f,
                parTime = 45,
                starThresholds = StarThresholds(oneStar = 0.2f, twoStar = 0.35f, threeStar = 0.5f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.7f, 0.7f, "n1"),
                    LevelNode("n3", "TEAL", 0.7f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.3f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.15f, 0.55f, "n6"),
                    LevelNode("n6", "ROSE", 0.85f, 0.45f, "n5"),
                ),
                narratorIntro = "The Warden watches. Show your worth.",
            )

            // ── WHISPERING MEADOW: levels 6-15 ──────────────────────────────

            6 -> Level(
                stageId = 6,
                world = WorldType.WHISPERING_MEADOW,
                title = "Crossing Paths",
                maxEnergy = 160f,
                // 4 pairs, optimal ~93 → 42% remaining
                starThresholds = StarThresholds(oneStar = 0.18f, twoStar = 0.28f, threeStar = 0.40f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.15f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.85f, "n5"),
                    LevelNode("n7", "AMBER", 0.15f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.85f, 0.5f, "n7"),
                ),
                narratorIntro = "Paths cross. Choose wisely.",
            )
            7 -> Level(
                stageId = 7,
                world = WorldType.WHISPERING_MEADOW,
                title = "Petal Ring",
                maxEnergy = 170f,
                // 4 pairs arranged around center, optimal ~99 → 42%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.27f, threeStar = 0.39f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.5f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.5f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.1f, 0.5f, "n4"),
                    LevelNode("n4", "TEAL", 0.9f, 0.5f, "n3"),
                    LevelNode("n5", "ROSE", 0.2f, 0.2f, "n6"),
                    LevelNode("n6", "ROSE", 0.8f, 0.8f, "n5"),
                    LevelNode("n7", "AMBER", 0.8f, 0.2f, "n8"),
                    LevelNode("n8", "AMBER", 0.2f, 0.8f, "n7"),
                ),
            )
            8 -> Level(
                stageId = 8,
                world = WorldType.WHISPERING_MEADOW,
                title = "Star Points",
                maxEnergy = 200f,
                // 5 pairs, optimal ~112 → 44%
                starThresholds = StarThresholds(oneStar = 0.18f, twoStar = 0.30f, threeStar = 0.41f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.5f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.5f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.1f, 0.4f, "n4"),
                    LevelNode("n4", "TEAL", 0.9f, 0.6f, "n3"),
                    LevelNode("n5", "ROSE", 0.9f, 0.4f, "n6"),
                    LevelNode("n6", "ROSE", 0.1f, 0.6f, "n5"),
                    LevelNode("n7", "AMBER", 0.2f, 0.85f, "n8"),
                    LevelNode("n8", "AMBER", 0.8f, 0.15f, "n7"),
                    LevelNode("n9", "CORAL", 0.3f, 0.5f, "n10"),
                    LevelNode("n10", "CORAL", 0.7f, 0.5f, "n9"),
                ),
            )
            9 -> Level(
                stageId = 9,
                world = WorldType.WHISPERING_MEADOW,
                title = "The Knot",
                maxEnergy = 220f,
                energyCostMultiplier = 1.1f,
                // 5 pairs, optimal ~124 → 43.5%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.28f, threeStar = 0.41f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.15f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.15f, 0.5f, "n6"),
                    LevelNode("n6", "ROSE", 0.85f, 0.5f, "n5"),
                    LevelNode("n7", "AMBER", 0.5f, 0.1f, "n8"),
                    LevelNode("n8", "AMBER", 0.5f, 0.9f, "n7"),
                    LevelNode("n9", "CORAL", 0.35f, 0.35f, "n10"),
                    LevelNode("n10", "CORAL", 0.65f, 0.65f, "n9"),
                ),
                narratorIntro = "Every path has its tangle.",
            )
            10 -> Level(
                stageId = 10,
                world = WorldType.WHISPERING_MEADOW,
                title = "Meadow's Heart",
                maxEnergy = 280f,
                energyCostMultiplier = 1.1f,
                parTime = 40,
                // 6 pairs all meadow colors, optimal ~168 → 40%
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.37f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.25f, 0.25f, "n10"),
                    LevelNode("n10", "CORAL", 0.75f, 0.75f, "n9"),
                    LevelNode("n11", "INDIGO", 0.75f, 0.25f, "n12"),
                    LevelNode("n12", "INDIGO", 0.25f, 0.75f, "n11"),
                ),
                narratorIntro = "The heart of the meadow blooms.",
                narratorOutro = "You've mastered the meadow. Crystal Caverns await.",
            )
            11 -> Level(
                stageId = 11,
                world = WorldType.WHISPERING_MEADOW,
                title = "Pressure Point",
                maxEnergy = 140f,
                // 3 pairs + 1 pressure pair, optimal ~76 → 45.7%
                starThresholds = StarThresholds(oneStar = 0.18f, twoStar = 0.30f, threeStar = 0.43f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.3f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.7f, "n1"),
                    LevelNode("n3", "TEAL", 0.5f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.5f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.15f, 0.6f, "n6"),
                    LevelNode("n6", "ROSE", 0.85f, 0.4f, "n5"),
                    LevelNode("n7", "AMBER", 0.3f, 0.5f, "n8", isPressureNode = true, pressureDurationMs = 1500),
                    LevelNode("n8", "AMBER", 0.7f, 0.5f, "n7"),
                ),
                narratorIntro = "Some flowers must be held to bloom.",
            )
            12 -> Level(
                stageId = 12,
                world = WorldType.WHISPERING_MEADOW,
                title = "Butterfly",
                maxEnergy = 185f,
                // 4 pairs + 1 pressure pair, optimal ~103 → 44.3%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.29f, threeStar = 0.42f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.25f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.75f, "n1"),
                    LevelNode("n3", "TEAL", 0.15f, 0.75f, "n4"),
                    LevelNode("n4", "TEAL", 0.85f, 0.25f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.25f, 0.5f, "n8", isPressureNode = true, pressureDurationMs = 1800),
                    LevelNode("n8", "AMBER", 0.75f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.4f, 0.4f, "n10"),
                    LevelNode("n10", "CORAL", 0.6f, 0.6f, "n9"),
                ),
            )
            13 -> Level(
                stageId = 13,
                world = WorldType.WHISPERING_MEADOW,
                title = "Storm Web",
                maxEnergy = 260f,
                energyCostMultiplier = 1.2f,
                // 5 pairs extreme corners, optimal ~162 → 37.7%
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.24f, threeStar = 0.35f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.3f, 0.3f, "n10"),
                    LevelNode("n10", "CORAL", 0.7f, 0.7f, "n9"),
                ),
                narratorIntro = "The storm bends every stem.",
            )
            14 -> Level(
                stageId = 14,
                world = WorldType.WHISPERING_MEADOW,
                title = "Autumn Dance",
                maxEnergy = 210f,
                energyCostMultiplier = 1.1f,
                // 5 pairs, 2 pressure nodes, optimal ~119 → 43.3%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.28f, threeStar = 0.41f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.2f, "n2", isPressureNode = true, pressureDurationMs = 1500),
                    LevelNode("n2", "VIOLET", 0.8f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6", isPressureNode = true, pressureDurationMs = 2000),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.1f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.9f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.35f, 0.5f, "n10"),
                    LevelNode("n10", "CORAL", 0.65f, 0.5f, "n9"),
                ),
            )
            15 -> Level(
                stageId = 15,
                world = WorldType.WHISPERING_MEADOW,
                title = "Final Bloom",
                maxEnergy = 280f,
                energyCostMultiplier = 1.2f,
                parTime = 45,
                // 6 pairs, optimal ~170 → 39.3%
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.36f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.15f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.85f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.15f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.25f, 0.6f, "n10"),
                    LevelNode("n10", "CORAL", 0.75f, 0.4f, "n9"),
                    LevelNode("n11", "INDIGO", 0.75f, 0.6f, "n12"),
                    LevelNode("n12", "INDIGO", 0.25f, 0.4f, "n11"),
                ),
                narratorIntro = "Every petal in its place.",
                narratorOutro = "The meadow rests. The caverns call.",
            )

            // ── CRYSTAL CAVERNS: levels 16-25 ────────────────────────────────

            16 -> Level(
                stageId = 16,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "First Crystals",
                maxEnergy = 100f,
                // 2 pairs, fresh world start, optimal ~38 → 62%
                starThresholds = StarThresholds(oneStar = 0.28f, twoStar = 0.45f, threeStar = 0.58f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.25f, 0.3f, "n2"),
                    LevelNode("n2", "VIOLET", 0.75f, 0.7f, "n1"),
                    LevelNode("n3", "TEAL", 0.75f, 0.3f, "n4"),
                    LevelNode("n4", "TEAL", 0.25f, 0.7f, "n3"),
                ),
                narratorIntro = "The caverns glitter. A new challenge begins.",
            )
            17 -> Level(
                stageId = 17,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Prism Pairs",
                maxEnergy = 130f,
                // 3 pairs, optimal ~65 → 50%
                starThresholds = StarThresholds(oneStar = 0.20f, twoStar = 0.33f, threeStar = 0.47f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.25f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.75f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.25f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.75f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.2f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.8f, "n5"),
                ),
            )
            18 -> Level(
                stageId = 18,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Refraction",
                maxEnergy = 170f,
                // 4 pairs, optimal ~91 → 46.5%
                starThresholds = StarThresholds(oneStar = 0.18f, twoStar = 0.30f, threeStar = 0.43f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.85f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.15f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.3f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.7f, 0.5f, "n7"),
                ),
                narratorIntro = "Light bends in the dark.",
            )
            19 -> Level(
                stageId = 19,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Crystal Formation",
                maxEnergy = 190f,
                energyCostMultiplier = 1.1f,
                // 4 pairs, optimal ~104 → 45.3%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.29f, threeStar = 0.42f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.15f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.15f, 0.5f, "n6"),
                    LevelNode("n6", "ROSE", 0.85f, 0.5f, "n5"),
                    LevelNode("n7", "AMBER", 0.5f, 0.2f, "n8"),
                    LevelNode("n8", "AMBER", 0.5f, 0.8f, "n7"),
                ),
            )
            20 -> Level(
                stageId = 20,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Cave Heart",
                maxEnergy = 240f,
                energyCostMultiplier = 1.1f,
                parTime = 45,
                // 5 pairs, optimal ~132 → 45%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.29f, threeStar = 0.42f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.15f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.85f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.15f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.1f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.9f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.35f, 0.35f, "n10"),
                    LevelNode("n10", "CORAL", 0.65f, 0.65f, "n9"),
                ),
                narratorIntro = "Deep in the cavern, the heart pulses.",
            )
            21 -> Level(
                stageId = 21,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Echo Chamber",
                maxEnergy = 250f,
                energyCostMultiplier = 1.1f,
                // 5 pairs extreme, optimal ~144 → 42.4%
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.28f, threeStar = 0.40f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.35f, 0.35f, "n10"),
                    LevelNode("n10", "CORAL", 0.65f, 0.65f, "n9"),
                ),
            )
            22 -> Level(
                stageId = 22,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Stalactite Hall",
                maxEnergy = 270f,
                energyCostMultiplier = 1.2f,
                // 5 pairs, optimal ~158 → 41.5%
                starThresholds = StarThresholds(oneStar = 0.16f, twoStar = 0.26f, threeStar = 0.38f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.3f, 0.4f, "n10"),
                    LevelNode("n10", "CORAL", 0.7f, 0.6f, "n9"),
                ),
                narratorIntro = "Watch your step among the spires.",
            )
            23 -> Level(
                stageId = 23,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Crystal Maze",
                maxEnergy = 290f,
                energyCostMultiplier = 1.1f,
                // 6 pairs, optimal ~168 → 42.1%
                starThresholds = StarThresholds(oneStar = 0.16f, twoStar = 0.27f, threeStar = 0.39f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.15f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.25f, 0.3f, "n10"),
                    LevelNode("n10", "CORAL", 0.75f, 0.7f, "n9"),
                    LevelNode("n11", "INDIGO", 0.75f, 0.3f, "n12"),
                    LevelNode("n12", "INDIGO", 0.25f, 0.7f, "n11"),
                ),
            )
            24 -> Level(
                stageId = 24,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Shattered Lens",
                maxEnergy = 310f,
                energyCostMultiplier = 1.25f,
                // 6 pairs near-corners, optimal ~189 → 39%
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.36f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.12f, 0.12f, "n2"),
                    LevelNode("n2", "VIOLET", 0.88f, 0.88f, "n1"),
                    LevelNode("n3", "TEAL", 0.88f, 0.12f, "n4"),
                    LevelNode("n4", "TEAL", 0.12f, 0.88f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.2f, 0.55f, "n10"),
                    LevelNode("n10", "CORAL", 0.8f, 0.45f, "n9"),
                    LevelNode("n11", "INDIGO", 0.8f, 0.55f, "n12"),
                    LevelNode("n12", "INDIGO", 0.2f, 0.45f, "n11"),
                ),
                narratorIntro = "The lens fractures, but the light finds a way.",
            )
            25 -> Level(
                stageId = 25,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Crystal Crown",
                maxEnergy = 330f,
                energyCostMultiplier = 1.2f,
                parTime = 40,
                // 7 pairs all colors (no PEARL), optimal ~201 → 39%
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.36f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.06f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.94f, "n5"),
                    LevelNode("n7", "AMBER", 0.06f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.94f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.2f, 0.6f, "n10"),
                    LevelNode("n10", "CORAL", 0.8f, 0.4f, "n9"),
                    LevelNode("n11", "INDIGO", 0.8f, 0.6f, "n12"),
                    LevelNode("n12", "INDIGO", 0.2f, 0.4f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.35f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.65f, "n13"),
                ),
                narratorIntro = "The crown of the cavern. Claim it.",
                narratorOutro = "The Crystal Crown is yours. What lies above?",
            )

            // ── CRYSTAL CAVERNS: levels 26-30 (hard) ────────────────────────

            26 -> Level(
                stageId = 26,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Crystal Weave",
                maxEnergy = 320f,
                energyCostMultiplier = 1.2f,
                // 6 pairs all crossing, optimal ~188 → 41%
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.26f, threeStar = 0.38f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.28f, 0.22f, "n10"),
                    LevelNode("n10", "CORAL", 0.72f, 0.78f, "n9"),
                    LevelNode("n11", "INDIGO", 0.72f, 0.22f, "n12"),
                    LevelNode("n12", "INDIGO", 0.28f, 0.78f, "n11"),
                ),
                narratorIntro = "The weave tightens. Find the thread.",
            )
            27 -> Level(
                stageId = 27,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Gem Cathedral",
                maxEnergy = 310f,
                energyCostMultiplier = 1.2f,
                // 6 pairs, 2 pressure nodes, must plan connection order
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.37f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.15f, "n2", isPressureNode = true, pressureDurationMs = 1500),
                    LevelNode("n2", "VIOLET", 0.85f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.85f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.15f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6", isPressureNode = true, pressureDurationMs = 2000),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.1f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.9f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.3f, 0.5f, "n10"),
                    LevelNode("n10", "CORAL", 0.7f, 0.5f, "n9"),
                    LevelNode("n11", "INDIGO", 0.5f, 0.3f, "n12"),
                    LevelNode("n12", "INDIGO", 0.5f, 0.7f, "n11"),
                ),
                narratorIntro = "Even stone must hold its shape.",
            )
            28 -> Level(
                stageId = 28,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Prism Storm",
                maxEnergy = 370f,
                energyCostMultiplier = 1.25f,
                // 7 pairs, all spread wide, optimal ~222 → 40%
                starThresholds = StarThresholds(oneStar = 0.14f, twoStar = 0.24f, threeStar = 0.37f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.08f, 0.12f, "n2"),
                    LevelNode("n2", "VIOLET", 0.92f, 0.88f, "n1"),
                    LevelNode("n3", "TEAL", 0.92f, 0.12f, "n4"),
                    LevelNode("n4", "TEAL", 0.08f, 0.88f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.07f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.93f, "n5"),
                    LevelNode("n7", "AMBER", 0.07f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.93f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.22f, 0.35f, "n10"),
                    LevelNode("n10", "CORAL", 0.78f, 0.65f, "n9"),
                    LevelNode("n11", "INDIGO", 0.78f, 0.35f, "n12"),
                    LevelNode("n12", "INDIGO", 0.22f, 0.65f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.38f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.62f, "n13"),
                ),
                narratorIntro = "Light fractures in seven directions.",
            )
            29 -> Level(
                stageId = 29,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Crystal Labyrinth",
                maxEnergy = 360f,
                energyCostMultiplier = 1.3f,
                // 7 pairs + 3 pressure — order matters critically
                starThresholds = StarThresholds(oneStar = 0.13f, twoStar = 0.23f, threeStar = 0.35f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.12f, 0.12f, "n2", isPressureNode = true, pressureDurationMs = 1500),
                    LevelNode("n2", "VIOLET", 0.88f, 0.88f, "n1"),
                    LevelNode("n3", "TEAL", 0.88f, 0.12f, "n4"),
                    LevelNode("n4", "TEAL", 0.12f, 0.88f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6", isPressureNode = true, pressureDurationMs = 1800),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8", isPressureNode = true, pressureDurationMs = 2000),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.25f, 0.25f, "n10"),
                    LevelNode("n10", "CORAL", 0.75f, 0.75f, "n9"),
                    LevelNode("n11", "INDIGO", 0.75f, 0.25f, "n12"),
                    LevelNode("n12", "INDIGO", 0.25f, 0.75f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.42f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.58f, "n13"),
                ),
                narratorIntro = "The labyrinth has no shortcuts.",
            )
            30 -> Level(
                stageId = 30,
                world = WorldType.CRYSTAL_CAVERNS,
                title = "Cavern King",
                maxEnergy = 430f,
                energyCostMultiplier = 1.3f,
                parTime = 40,
                // 8 pairs — all colors, world boss
                starThresholds = StarThresholds(oneStar = 0.13f, twoStar = 0.22f, threeStar = 0.34f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.08f, 0.08f, "n2"),
                    LevelNode("n2", "VIOLET", 0.92f, 0.92f, "n1"),
                    LevelNode("n3", "TEAL", 0.92f, 0.08f, "n4"),
                    LevelNode("n4", "TEAL", 0.08f, 0.92f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.06f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.94f, "n5"),
                    LevelNode("n7", "AMBER", 0.06f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.94f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.2f, 0.55f, "n10"),
                    LevelNode("n10", "CORAL", 0.8f, 0.45f, "n9"),
                    LevelNode("n11", "INDIGO", 0.8f, 0.55f, "n12"),
                    LevelNode("n12", "INDIGO", 0.2f, 0.45f, "n11"),
                    LevelNode("n13", "EMERALD", 0.35f, 0.35f, "n14"),
                    LevelNode("n14", "EMERALD", 0.65f, 0.65f, "n13"),
                    LevelNode("n15", "PEARL", 0.65f, 0.35f, "n16"),
                    LevelNode("n16", "PEARL", 0.35f, 0.65f, "n15"),
                ),
                narratorIntro = "Claim the throne of crystal.",
                narratorOutro = "The caverns bow. The isles beckon above.",
            )

            // ── FLOATING ISLES: levels 31-45 ─────────────────────────────────

            31 -> Level(
                stageId = 31,
                world = WorldType.FLOATING_ISLES,
                title = "Sky Garden",
                maxEnergy = 110f,
                // 2 pairs — gentle world intro
                starThresholds = StarThresholds(oneStar = 0.30f, twoStar = 0.48f, threeStar = 0.60f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.25f, 0.3f, "n2"),
                    LevelNode("n2", "VIOLET", 0.75f, 0.7f, "n1"),
                    LevelNode("n3", "TEAL", 0.75f, 0.3f, "n4"),
                    LevelNode("n4", "TEAL", 0.25f, 0.7f, "n3"),
                ),
                narratorIntro = "Clouds part. A new garden floats before you.",
            )
            32 -> Level(
                stageId = 32,
                world = WorldType.FLOATING_ISLES,
                title = "Cloud Step",
                maxEnergy = 150f,
                // 3 pairs
                starThresholds = StarThresholds(oneStar = 0.22f, twoStar = 0.36f, threeStar = 0.50f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.25f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.75f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.25f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.75f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.15f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.85f, "n5"),
                ),
            )
            33 -> Level(
                stageId = 33,
                world = WorldType.FLOATING_ISLES,
                title = "Zephyr",
                maxEnergy = 190f,
                energyCostMultiplier = 1.05f,
                // 4 pairs, diagonal spread
                starThresholds = StarThresholds(oneStar = 0.20f, twoStar = 0.33f, threeStar = 0.46f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.85f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.15f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.15f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.85f, 0.5f, "n7"),
                ),
            )
            34 -> Level(
                stageId = 34,
                world = WorldType.FLOATING_ISLES,
                title = "Isle Hop",
                maxEnergy = 195f,
                energyCostMultiplier = 1.1f,
                // 4 pairs + 1 pressure
                starThresholds = StarThresholds(oneStar = 0.18f, twoStar = 0.30f, threeStar = 0.43f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.2f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.8f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.8f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.2f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.1f, "n6", isPressureNode = true, pressureDurationMs = 1500),
                    LevelNode("n6", "ROSE", 0.5f, 0.9f, "n5"),
                    LevelNode("n7", "AMBER", 0.3f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.7f, 0.5f, "n7"),
                ),
                narratorIntro = "Step lightly. The isles are not patient.",
            )
            35 -> Level(
                stageId = 35,
                world = WorldType.FLOATING_ISLES,
                title = "Wind Gate",
                maxEnergy = 230f,
                energyCostMultiplier = 1.1f,
                // 5 pairs, asymmetric positions
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.28f, threeStar = 0.41f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.15f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.85f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.15f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.85f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.25f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.75f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.35f, 0.28f, "n10"),
                    LevelNode("n10", "CORAL", 0.65f, 0.72f, "n9"),
                ),
            )
            36 -> Level(
                stageId = 36,
                world = WorldType.FLOATING_ISLES,
                title = "Sky Ring",
                maxEnergy = 255f,
                energyCostMultiplier = 1.15f,
                // 5 pairs arranged in ring — crossing paths are unavoidable
                starThresholds = StarThresholds(oneStar = 0.17f, twoStar = 0.27f, threeStar = 0.40f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.5f, 0.07f, "n2"),
                    LevelNode("n2", "VIOLET", 0.5f, 0.93f, "n1"),
                    LevelNode("n3", "TEAL", 0.93f, 0.35f, "n4"),
                    LevelNode("n4", "TEAL", 0.07f, 0.65f, "n3"),
                    LevelNode("n5", "ROSE", 0.82f, 0.88f, "n6"),
                    LevelNode("n6", "ROSE", 0.18f, 0.12f, "n5"),
                    LevelNode("n7", "AMBER", 0.18f, 0.88f, "n8"),
                    LevelNode("n8", "AMBER", 0.82f, 0.12f, "n7"),
                    LevelNode("n9", "CORAL", 0.07f, 0.35f, "n10"),
                    LevelNode("n10", "CORAL", 0.93f, 0.65f, "n9"),
                ),
                narratorIntro = "The ring has no beginning. Find the middle.",
            )
            37 -> Level(
                stageId = 37,
                world = WorldType.FLOATING_ISLES,
                title = "Aerial Web",
                maxEnergy = 300f,
                energyCostMultiplier = 1.15f,
                // 6 pairs, dense center with far corners
                starThresholds = StarThresholds(oneStar = 0.16f, twoStar = 0.26f, threeStar = 0.39f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.3f, 0.42f, "n10"),
                    LevelNode("n10", "CORAL", 0.7f, 0.58f, "n9"),
                    LevelNode("n11", "INDIGO", 0.7f, 0.42f, "n12"),
                    LevelNode("n12", "INDIGO", 0.3f, 0.58f, "n11"),
                ),
            )
            38 -> Level(
                stageId = 38,
                world = WorldType.FLOATING_ISLES,
                title = "Cloud Castle",
                maxEnergy = 315f,
                energyCostMultiplier = 1.2f,
                // 6 pairs + 2 pressure, castle-like arrangement
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.37f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.12f, 0.12f, "n2", isPressureNode = true, pressureDurationMs = 1600),
                    LevelNode("n2", "VIOLET", 0.88f, 0.88f, "n1"),
                    LevelNode("n3", "TEAL", 0.88f, 0.12f, "n4", isPressureNode = true, pressureDurationMs = 1600),
                    LevelNode("n4", "TEAL", 0.12f, 0.88f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.28f, 0.28f, "n10"),
                    LevelNode("n10", "CORAL", 0.72f, 0.72f, "n9"),
                    LevelNode("n11", "INDIGO", 0.72f, 0.28f, "n12"),
                    LevelNode("n12", "INDIGO", 0.28f, 0.72f, "n11"),
                ),
                narratorIntro = "The castle towers demand precision.",
            )
            39 -> Level(
                stageId = 39,
                world = WorldType.FLOATING_ISLES,
                title = "Tempest",
                maxEnergy = 330f,
                energyCostMultiplier = 1.25f,
                // 7 pairs, extreme spread, tight budget
                starThresholds = StarThresholds(oneStar = 0.14f, twoStar = 0.24f, threeStar = 0.36f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.08f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.92f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.92f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.08f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.06f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.94f, "n5"),
                    LevelNode("n7", "AMBER", 0.06f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.94f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.2f, 0.32f, "n10"),
                    LevelNode("n10", "CORAL", 0.8f, 0.68f, "n9"),
                    LevelNode("n11", "INDIGO", 0.8f, 0.32f, "n12"),
                    LevelNode("n12", "INDIGO", 0.2f, 0.68f, "n11"),
                    LevelNode("n13", "EMERALD", 0.42f, 0.42f, "n14"),
                    LevelNode("n14", "EMERALD", 0.58f, 0.58f, "n13"),
                ),
                narratorIntro = "The storm will not wait.",
            )
            40 -> Level(
                stageId = 40,
                world = WorldType.FLOATING_ISLES,
                title = "Isle Heart",
                maxEnergy = 390f,
                energyCostMultiplier = 1.25f,
                parTime = 45,
                // 8 pairs — all colors, milestone level
                starThresholds = StarThresholds(oneStar = 0.13f, twoStar = 0.22f, threeStar = 0.34f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.07f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.93f, "n5"),
                    LevelNode("n7", "AMBER", 0.07f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.93f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.22f, 0.5f, "n10"),
                    LevelNode("n10", "CORAL", 0.78f, 0.5f, "n9"),
                    LevelNode("n11", "INDIGO", 0.5f, 0.22f, "n12"),
                    LevelNode("n12", "INDIGO", 0.5f, 0.78f, "n11"),
                    LevelNode("n13", "EMERALD", 0.3f, 0.3f, "n14"),
                    LevelNode("n14", "EMERALD", 0.7f, 0.7f, "n13"),
                    LevelNode("n15", "PEARL", 0.7f, 0.3f, "n16"),
                    LevelNode("n16", "PEARL", 0.3f, 0.7f, "n15"),
                ),
                narratorIntro = "The heart of the isles pulses with all eight energies.",
            )
            41 -> Level(
                stageId = 41,
                world = WorldType.FLOATING_ISLES,
                title = "Vortex",
                maxEnergy = 400f,
                energyCostMultiplier = 1.3f,
                // 7 pairs in spiral formation — routing order matters
                starThresholds = StarThresholds(oneStar = 0.13f, twoStar = 0.22f, threeStar = 0.33f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.5f, 0.06f, "n2"),
                    LevelNode("n2", "VIOLET", 0.5f, 0.94f, "n1"),
                    LevelNode("n3", "TEAL", 0.94f, 0.5f, "n4"),
                    LevelNode("n4", "TEAL", 0.06f, 0.5f, "n3"),
                    LevelNode("n5", "ROSE", 0.85f, 0.2f, "n6"),
                    LevelNode("n6", "ROSE", 0.15f, 0.8f, "n5"),
                    LevelNode("n7", "AMBER", 0.15f, 0.2f, "n8"),
                    LevelNode("n8", "AMBER", 0.85f, 0.8f, "n7"),
                    LevelNode("n9", "CORAL", 0.72f, 0.12f, "n10"),
                    LevelNode("n10", "CORAL", 0.28f, 0.88f, "n9"),
                    LevelNode("n11", "INDIGO", 0.28f, 0.12f, "n12"),
                    LevelNode("n12", "INDIGO", 0.72f, 0.88f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.38f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.62f, "n13"),
                ),
                narratorIntro = "The vortex pulls every path inward.",
            )
            42 -> Level(
                stageId = 42,
                world = WorldType.FLOATING_ISLES,
                title = "Skyfall",
                maxEnergy = 415f,
                energyCostMultiplier = 1.3f,
                // 7 pairs + 3 pressure — need to tackle pressure last
                starThresholds = StarThresholds(oneStar = 0.12f, twoStar = 0.22f, threeStar = 0.33f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2", isPressureNode = true, pressureDurationMs = 1400),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6", isPressureNode = true, pressureDurationMs = 1800),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.25f, 0.38f, "n10", isPressureNode = true, pressureDurationMs = 2200),
                    LevelNode("n10", "CORAL", 0.75f, 0.62f, "n9"),
                    LevelNode("n11", "INDIGO", 0.75f, 0.38f, "n12"),
                    LevelNode("n12", "INDIGO", 0.25f, 0.62f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.42f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.58f, "n13"),
                ),
                narratorIntro = "When isles fall, the sky catches them.",
            )
            43 -> Level(
                stageId = 43,
                world = WorldType.FLOATING_ISLES,
                title = "The Gale",
                maxEnergy = 420f,
                energyCostMultiplier = 1.35f,
                // 7 pairs, all near-extremes, brutal multiplier
                starThresholds = StarThresholds(oneStar = 0.12f, twoStar = 0.21f, threeStar = 0.32f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.07f, 0.08f, "n2"),
                    LevelNode("n2", "VIOLET", 0.93f, 0.92f, "n1"),
                    LevelNode("n3", "TEAL", 0.93f, 0.08f, "n4"),
                    LevelNode("n4", "TEAL", 0.07f, 0.92f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.05f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.95f, "n5"),
                    LevelNode("n7", "AMBER", 0.05f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.95f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.18f, 0.28f, "n10"),
                    LevelNode("n10", "CORAL", 0.82f, 0.72f, "n9"),
                    LevelNode("n11", "INDIGO", 0.82f, 0.28f, "n12"),
                    LevelNode("n12", "INDIGO", 0.18f, 0.72f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.4f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.6f, "n13"),
                ),
                narratorIntro = "The gale strips everything away. Only paths remain.",
            )
            44 -> Level(
                stageId = 44,
                world = WorldType.FLOATING_ISLES,
                title = "Storm Crown",
                maxEnergy = 450f,
                energyCostMultiplier = 1.35f,
                // 8 pairs + 2 pressure — all 8 colors, hardest arrangement
                starThresholds = StarThresholds(oneStar = 0.12f, twoStar = 0.21f, threeStar = 0.31f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.08f, 0.08f, "n2", isPressureNode = true, pressureDurationMs = 1500),
                    LevelNode("n2", "VIOLET", 0.92f, 0.92f, "n1"),
                    LevelNode("n3", "TEAL", 0.92f, 0.08f, "n4"),
                    LevelNode("n4", "TEAL", 0.08f, 0.92f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.06f, "n6", isPressureNode = true, pressureDurationMs = 2000),
                    LevelNode("n6", "ROSE", 0.5f, 0.94f, "n5"),
                    LevelNode("n7", "AMBER", 0.06f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.94f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.22f, 0.55f, "n10"),
                    LevelNode("n10", "CORAL", 0.78f, 0.45f, "n9"),
                    LevelNode("n11", "INDIGO", 0.78f, 0.55f, "n12"),
                    LevelNode("n12", "INDIGO", 0.22f, 0.45f, "n11"),
                    LevelNode("n13", "EMERALD", 0.32f, 0.32f, "n14"),
                    LevelNode("n14", "EMERALD", 0.68f, 0.68f, "n13"),
                    LevelNode("n15", "PEARL", 0.68f, 0.32f, "n16"),
                    LevelNode("n16", "PEARL", 0.32f, 0.68f, "n15"),
                ),
                narratorIntro = "All eight winds converge on the crown.",
            )
            45 -> Level(
                stageId = 45,
                world = WorldType.FLOATING_ISLES,
                title = "Isle Ascension",
                maxEnergy = 460f,
                energyCostMultiplier = 1.4f,
                parTime = 35,
                // 8 pairs, near-extremes, maximum multiplier for this world
                starThresholds = StarThresholds(oneStar = 0.11f, twoStar = 0.20f, threeStar = 0.30f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.06f, 0.06f, "n2"),
                    LevelNode("n2", "VIOLET", 0.94f, 0.94f, "n1"),
                    LevelNode("n3", "TEAL", 0.94f, 0.06f, "n4"),
                    LevelNode("n4", "TEAL", 0.06f, 0.94f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.05f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.95f, "n5"),
                    LevelNode("n7", "AMBER", 0.05f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.95f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.2f, 0.5f, "n10"),
                    LevelNode("n10", "CORAL", 0.8f, 0.5f, "n9"),
                    LevelNode("n11", "INDIGO", 0.5f, 0.2f, "n12"),
                    LevelNode("n12", "INDIGO", 0.5f, 0.8f, "n11"),
                    LevelNode("n13", "EMERALD", 0.28f, 0.28f, "n14"),
                    LevelNode("n14", "EMERALD", 0.72f, 0.72f, "n13"),
                    LevelNode("n15", "PEARL", 0.72f, 0.28f, "n16"),
                    LevelNode("n16", "PEARL", 0.28f, 0.72f, "n15"),
                ),
                narratorIntro = "One last ascent. The sky is almost yours.",
                narratorOutro = "You have risen above the clouds. What hides beneath the waves?",
            )

            // ── DEEP SEA: levels 46-50 ────────────────────────────────────────

            46 -> Level(
                stageId = 46,
                world = WorldType.DEEP_SEA,
                title = "First Fathom",
                maxEnergy = 120f,
                // 2 pairs — gentle world intro
                starThresholds = StarThresholds(oneStar = 0.28f, twoStar = 0.45f, threeStar = 0.58f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.25f, 0.3f, "n2"),
                    LevelNode("n2", "VIOLET", 0.75f, 0.7f, "n1"),
                    LevelNode("n3", "TEAL", 0.75f, 0.3f, "n4"),
                    LevelNode("n4", "TEAL", 0.25f, 0.7f, "n3"),
                ),
                narratorIntro = "The ocean swallows light. Navigate by feeling.",
            )
            47 -> Level(
                stageId = 47,
                world = WorldType.DEEP_SEA,
                title = "Ocean Current",
                maxEnergy = 200f,
                energyCostMultiplier = 1.1f,
                // 4 pairs, current-like diagonal sweep
                starThresholds = StarThresholds(oneStar = 0.18f, twoStar = 0.30f, threeStar = 0.43f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.15f, 0.2f, "n2"),
                    LevelNode("n2", "VIOLET", 0.85f, 0.8f, "n1"),
                    LevelNode("n3", "TEAL", 0.85f, 0.2f, "n4"),
                    LevelNode("n4", "TEAL", 0.15f, 0.8f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.12f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.88f, "n5"),
                    LevelNode("n7", "AMBER", 0.2f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.8f, 0.5f, "n7"),
                ),
            )
            48 -> Level(
                stageId = 48,
                world = WorldType.DEEP_SEA,
                title = "Abyss Gate",
                maxEnergy = 270f,
                energyCostMultiplier = 1.2f,
                // 6 pairs — all crossing, very tight budget
                starThresholds = StarThresholds(oneStar = 0.15f, twoStar = 0.25f, threeStar = 0.37f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2"),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.08f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.92f, "n5"),
                    LevelNode("n7", "AMBER", 0.08f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.92f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.26f, 0.26f, "n10"),
                    LevelNode("n10", "CORAL", 0.74f, 0.74f, "n9"),
                    LevelNode("n11", "INDIGO", 0.74f, 0.26f, "n12"),
                    LevelNode("n12", "INDIGO", 0.26f, 0.74f, "n11"),
                ),
                narratorIntro = "The gate to the deep opens slowly. Earn passage.",
            )
            49 -> Level(
                stageId = 49,
                world = WorldType.DEEP_SEA,
                title = "Pressure Deep",
                maxEnergy = 360f,
                energyCostMultiplier = 1.35f,
                // 7 pairs + 3 pressure — extreme challenge
                starThresholds = StarThresholds(oneStar = 0.12f, twoStar = 0.21f, threeStar = 0.32f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.1f, 0.1f, "n2", isPressureNode = true, pressureDurationMs = 1300),
                    LevelNode("n2", "VIOLET", 0.9f, 0.9f, "n1"),
                    LevelNode("n3", "TEAL", 0.9f, 0.1f, "n4"),
                    LevelNode("n4", "TEAL", 0.1f, 0.9f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.07f, "n6", isPressureNode = true, pressureDurationMs = 1700),
                    LevelNode("n6", "ROSE", 0.5f, 0.93f, "n5"),
                    LevelNode("n7", "AMBER", 0.07f, 0.5f, "n8", isPressureNode = true, pressureDurationMs = 2100),
                    LevelNode("n8", "AMBER", 0.93f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.22f, 0.32f, "n10"),
                    LevelNode("n10", "CORAL", 0.78f, 0.68f, "n9"),
                    LevelNode("n11", "INDIGO", 0.78f, 0.32f, "n12"),
                    LevelNode("n12", "INDIGO", 0.22f, 0.68f, "n11"),
                    LevelNode("n13", "EMERALD", 0.5f, 0.42f, "n14"),
                    LevelNode("n14", "EMERALD", 0.5f, 0.58f, "n13"),
                ),
                narratorIntro = "The pressure here bends light and will alike.",
            )
            50 -> Level(
                stageId = 50,
                world = WorldType.DEEP_SEA,
                title = "The Trench",
                maxEnergy = 450f,
                energyCostMultiplier = 1.4f,
                parTime = 35,
                // 8 pairs — all 8 colors, hardest level so far
                starThresholds = StarThresholds(oneStar = 0.11f, twoStar = 0.19f, threeStar = 0.29f),
                nodes = listOf(
                    LevelNode("n1", "VIOLET", 0.06f, 0.06f, "n2"),
                    LevelNode("n2", "VIOLET", 0.94f, 0.94f, "n1"),
                    LevelNode("n3", "TEAL", 0.94f, 0.06f, "n4"),
                    LevelNode("n4", "TEAL", 0.06f, 0.94f, "n3"),
                    LevelNode("n5", "ROSE", 0.5f, 0.05f, "n6"),
                    LevelNode("n6", "ROSE", 0.5f, 0.95f, "n5"),
                    LevelNode("n7", "AMBER", 0.05f, 0.5f, "n8"),
                    LevelNode("n8", "AMBER", 0.95f, 0.5f, "n7"),
                    LevelNode("n9", "CORAL", 0.2f, 0.52f, "n10"),
                    LevelNode("n10", "CORAL", 0.8f, 0.48f, "n9"),
                    LevelNode("n11", "INDIGO", 0.8f, 0.52f, "n12"),
                    LevelNode("n12", "INDIGO", 0.2f, 0.48f, "n11"),
                    LevelNode("n13", "EMERALD", 0.3f, 0.3f, "n14"),
                    LevelNode("n14", "EMERALD", 0.7f, 0.7f, "n13"),
                    LevelNode("n15", "PEARL", 0.7f, 0.3f, "n16"),
                    LevelNode("n16", "PEARL", 0.3f, 0.7f, "n15"),
                ),
                narratorIntro = "Nothing survives the trench. Except you.",
                narratorOutro = "The trench yields. The deep sea has no end — but you have found its heart.",
            )

            else -> getSampleLevel(1)
        }
    }
}

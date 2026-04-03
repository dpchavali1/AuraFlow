package com.auraflow.garden.data.model

import com.auraflow.garden.data.repository.LevelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LevelTest {

    private val repo = LevelRepository()

    @Test
    fun sampleLevelLoads() {
        val level = repo.loadLevel(1)
        assertEquals(1, level.stageId)
        assertEquals("First Light", level.title)
        assertEquals(WorldType.WHISPERING_MEADOW, level.world)
    }

    @Test
    fun allSampleLevelsHavePairedNodes() {
        for (stageId in 1..5) {
            val level = repo.loadLevel(stageId)
            level.nodes.forEach { node ->
                val paired = level.nodes.find { it.id == node.pairedNodeId }
                assertTrue(paired != null, "Stage $stageId node ${node.id} has no paired node ${node.pairedNodeId}")
                assertEquals(node.color, paired.color, "Stage $stageId paired nodes ${node.id}/${paired.id} have different colors")
            }
        }
    }

    @Test
    fun allSampleLevelsHaveEvenNodeCount() {
        for (stageId in 1..5) {
            val level = repo.loadLevel(stageId)
            assertEquals(0, level.nodes.size % 2, "Stage $stageId has odd number of nodes")
        }
    }

    @Test
    fun levelJsonRoundTrip() {
        val jsonString = """
        {
            "stageId": 99,
            "world": "WHISPERING_MEADOW",
            "title": "Test Stage",
            "maxEnergy": 100.0,
            "energyCostMultiplier": 1.0,
            "parTime": 60,
            "starThresholds": {"oneStar": 0.3, "twoStar": 0.5, "threeStar": 0.8},
            "noIntersectionsRequired": false,
            "nodes": [
                {"id": "n1", "color": "VIOLET", "x": 0.2, "y": 0.3, "pairedNodeId": "n2"},
                {"id": "n2", "color": "VIOLET", "x": 0.8, "y": 0.7, "pairedNodeId": "n1"}
            ]
        }
        """.trimIndent()
        val level = repo.parseLevelJson(jsonString)
        assertEquals(99, level.stageId)
        assertEquals("Test Stage", level.title)
        assertEquals(2, level.nodes.size)
    }

    @Test
    fun levelToNodesConverts() {
        val level = repo.loadLevel(1)
        val nodes = repo.levelToNodes(level)
        assertEquals(level.nodes.size, nodes.size)
        assertEquals(NodeColor.VIOLET, nodes[0].colorType)
        assertEquals(0.2f, nodes[0].position.x)
    }
}

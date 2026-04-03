package com.auraflow.garden.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeColorTest {

    @Test
    fun eachNodeColorHasUniqueShape() {
        val shapes = NodeColor.entries.map { it.shape }
        assertEquals(shapes.size, shapes.toSet().size, "Duplicate NodeShape found among NodeColor entries")
    }

    @Test
    fun eachNodeColorHasUniqueCvdHex() {
        val cvdHexes = NodeColor.entries.map { it.cvdHex }
        assertEquals(cvdHexes.size, cvdHexes.toSet().size, "Duplicate cvdHex found among NodeColor entries")
    }

    @Test
    fun eachNodeColorHasNonEmptySymbol() {
        NodeColor.entries.forEach { color ->
            assertTrue(color.symbol.isNotEmpty(), "${color.name} has empty symbol")
        }
    }

    @Test
    fun eachNodeColorHasValidHex() {
        val hexPattern = Regex("^#[0-9A-Fa-f]{6}$")
        NodeColor.entries.forEach { color ->
            assertTrue(hexPattern.matches(color.hex), "${color.name} hex ${color.hex} is invalid")
            assertTrue(hexPattern.matches(color.cvdHex), "${color.name} cvdHex ${color.cvdHex} is invalid")
        }
    }

    @Test
    fun allNodeShapesAreCovered() {
        val usedShapes = NodeColor.entries.map { it.shape }.toSet()
        assertEquals(NodeShape.entries.toSet(), usedShapes, "Not all NodeShapes are mapped to a NodeColor")
    }
}

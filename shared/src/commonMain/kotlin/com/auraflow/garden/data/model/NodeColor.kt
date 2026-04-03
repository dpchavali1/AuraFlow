package com.auraflow.garden.data.model

enum class NodeColor(
    val hex: String,
    val displayName: String,
    val shape: NodeShape,
    val cvdHex: String,
    val symbol: String,
) {
    VIOLET("#8B5CF6", "Violet", NodeShape.CIRCLE, "#5599FF", "●"),
    TEAL("#00B4D8", "Teal", NodeShape.SQUARE, "#FFCC00", "■"),
    ROSE("#E63946", "Rose", NodeShape.TRIANGLE, "#FF8800", "▲"),
    AMBER("#FFB703", "Amber", NodeShape.DIAMOND, "#FFFFFF", "◆"),
    CORAL("#FF6B6B", "Coral", NodeShape.HEXAGON, "#CC99FF", "⬡"),
    INDIGO("#4338CA", "Indigo", NodeShape.STAR, "#0066CC", "★"),
    EMERALD("#06D6A0", "Emerald", NodeShape.CROSS, "#99FF99", "✚"),
    PEARL("#F0F0F5", "Pearl", NodeShape.PENTAGON, "#DDDDDD", "⬠"),
}

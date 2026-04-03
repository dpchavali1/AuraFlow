package com.auraflow.garden.ui.screens.game.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.auraflow.garden.data.model.GameState
import com.auraflow.garden.data.model.NodeShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GameCanvasWithAccessibility(
    state: GameState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onNodeTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val nodeSizeDp = 44.dp
    val touchTargetDp = 56.dp

    val infiniteTransition = rememberInfiniteTransition(label = "canvas_anim")

    // Pulse for unlinked nodes
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    // Shimmer phase — traveling energy dot along links (0→1)
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
        ),
        label = "shimmer_phase",
    )

    // Node entrance animation — staggered spring pop when level loads
    val entranceProgress = remember { Animatable(0f) }
    LaunchedEffect(state.level?.stageId) {
        entranceProgress.snapTo(0f)
        delay(80L)
        entranceProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        )
    }

    // Pre-allocate drawing paths
    val linkPath = remember { Path() }
    val shapePath = remember { Path() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cw = size.width
            val ch = size.height

            // --- Deep gradient background ---
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF06061A), Color(0xFF0D0D2E), Color(0xFF06061A)),
                ),
                size = size,
            )
            // Subtle center radiance
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x14A78BFA), Color.Transparent),
                    center = Offset(cw * 0.5f, ch * 0.45f),
                    radius = cw * 0.75f,
                ),
                radius = cw * 0.75f,
                center = Offset(cw * 0.5f, ch * 0.45f),
            )

            // --- Links (behind nodes) ---
            for (link in state.links) {
                val sourceNode = state.nodes.find { it.id == link.sourceNodeId }
                val targetNode = state.nodes.find { it.id == link.targetNodeId }
                if (sourceNode == null || targetNode == null) continue

                val from = Offset(sourceNode.position.x * cw, sourceNode.position.y * ch)
                val to = Offset(targetNode.position.x * cw, targetNode.position.y * ch)
                val color = parseHexColor(link.colorType.hex)

                drawRichLink(linkPath, from, to, color, shimmerPhase)
            }

            // --- Nodes ---
            val nodeSizePx = with(density) { nodeSizeDp.toPx() }
            val nodeCount = state.nodes.size.coerceAtLeast(1)
            for ((index, node) in state.nodes.withIndex()) {
                val cx = node.position.x * cw
                val cy = node.position.y * ch

                // Staggered entrance: each node starts 1/nodeCount later in the 0→1 progress
                val staggerOffset = index.toFloat() / nodeCount.toFloat()
                val nodeEntrance = ((entranceProgress.value - staggerOffset) * nodeCount.toFloat())
                    .coerceIn(0f, 1f)
                if (nodeEntrance <= 0f) continue

                val scale = (if (!node.isLinked) pulseScale else 1.0f) * nodeEntrance
                val radius = nodeSizePx / 2f * scale
                val nodeColor = parseHexColor(node.colorType.hex)
                val isSelected = node.id == state.selectedNodeId

                drawRichNode(
                    path = shapePath,
                    center = Offset(cx, cy),
                    radius = radius,
                    color = nodeColor,
                    isSelected = isSelected,
                    isLinked = node.isLinked,
                )

                // Pressure node countdown arc — visible only while unlinked
                if (node.isPressureNode && !node.isLinked) {
                    val progress = state.pressureProgress[node.id] ?: 0f
                    drawPressureArc(
                        center = Offset(cx, cy),
                        radius = radius,
                        progress = progress,
                        color = nodeColor,
                    )
                }

                shapePath.reset()
                drawNodeShape(
                    path = shapePath,
                    center = Offset(cx, cy),
                    radius = radius * 0.5f,
                    shape = node.colorType.shape,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }

        // Accessibility touch targets
        for (node in state.nodes) {
            if (canvasWidthPx <= 0f || canvasHeightPx <= 0f) continue
            val nodeScreenX = node.position.x * canvasWidthPx
            val nodeScreenY = node.position.y * canvasHeightPx
            val touchTargetPx = with(density) { touchTargetDp.toPx() }
            val offsetX = nodeScreenX - touchTargetPx / 2f
            val offsetY = nodeScreenY - touchTargetPx / 2f
            val nodeDescription = "${node.colorType.displayName} ${node.colorType.shape.name.lowercase()} node" +
                if (node.isLinked) ", linked" else ", unlinked"
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(touchTargetDp)
                    .semantics {
                        contentDescription = nodeDescription
                        role = Role.Button
                    }
                    .clickable { onNodeTapped(node.id) },
            )
        }
    }
}

private fun DrawScope.drawRichLink(
    path: Path,
    from: Offset,
    to: Offset,
    color: Color,
    shimmerPhase: Float,
) {
    // Wide soft outer glow
    drawLine(
        color = color.copy(alpha = 0.07f),
        start = from, end = to,
        strokeWidth = 30.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = color.copy(alpha = 0.14f),
        start = from, end = to,
        strokeWidth = 16.dp.toPx(),
        cap = StrokeCap.Round,
    )

    // Main gradient path: color → white shimmer → color
    path.reset()
    path.moveTo(from.x, from.y)
    path.lineTo(to.x, to.y)
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(
                color.copy(alpha = 0.9f),
                Color.White.copy(alpha = 0.75f),
                color.copy(alpha = 0.9f),
            ),
            start = from,
            end = to,
        ),
        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
    )

    // Bright core
    drawLine(
        color = Color.White.copy(alpha = 0.20f),
        start = from, end = to,
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
    )

    // Traveling energy dot
    val dotX = from.x + (to.x - from.x) * shimmerPhase
    val dotY = from.y + (to.y - from.y) * shimmerPhase
    val dotCenter = Offset(dotX, dotY)
    drawCircle(color = color.copy(alpha = 0.45f), radius = 11.dp.toPx(), center = dotCenter)
    drawCircle(color = Color.White.copy(alpha = 0.90f), radius = 4.dp.toPx(), center = dotCenter)
}

private fun DrawScope.drawRichNode(
    path: Path,
    center: Offset,
    radius: Float,
    color: Color,
    isSelected: Boolean,
    isLinked: Boolean,
) {
    // Wide ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.20f), Color.Transparent),
            center = center,
            radius = radius * 3.0f,
        ),
        radius = radius * 3.0f,
        center = center,
    )

    // Mid glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.40f), Color.Transparent),
            center = center,
            radius = radius * 1.8f,
        ),
        radius = radius * 1.8f,
        center = center,
    )

    // Main body: radial gradient with offset highlight for 3D bubble effect
    val highlightCenter = Offset(center.x - radius * 0.22f, center.y - radius * 0.28f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.60f),
                color,
                color.copy(alpha = 0.75f),
            ),
            center = highlightCenter,
            radius = radius * 1.1f,
        ),
        radius = radius,
        center = center,
    )

    // Rim highlight stroke
    drawCircle(
        color = Color.White.copy(alpha = 0.22f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
    )

    // Linked indicator: inner ring
    if (isLinked) {
        drawCircle(
            color = Color.White.copy(alpha = 0.30f),
            radius = radius * 0.72f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }

    // Selection: bright ring + outer pulse ring
    if (isSelected) {
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = radius + 5.dp.toPx(),
            center = center,
            style = Stroke(width = 2.5.dp.toPx()),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
                center = center,
                radius = radius + 18.dp.toPx(),
            ),
            radius = radius + 18.dp.toPx(),
            center = center,
        )
    }
}

private fun DrawScope.drawNodeShape(
    path: Path,
    center: Offset,
    radius: Float,
    shape: NodeShape,
    color: Color,
) {
    path.reset()
    when (shape) {
        NodeShape.CIRCLE -> {
            drawCircle(color = color, radius = radius * 0.6f, center = center)
        }
        NodeShape.SQUARE -> {
            val half = radius * 0.7f
            path.addRect(
                androidx.compose.ui.geometry.Rect(
                    left = center.x - half,
                    top = center.y - half,
                    right = center.x + half,
                    bottom = center.y + half,
                )
            )
            drawPath(path = path, color = color)
        }
        NodeShape.TRIANGLE -> {
            val angle = PI.toFloat() / 2f
            path.moveTo(center.x + radius * cos(angle), center.y - radius * sin(angle))
            for (i in 1..2) {
                val a = angle + i * (2f * PI.toFloat() / 3f)
                path.lineTo(center.x + radius * cos(a), center.y - radius * sin(a))
            }
            path.close()
            drawPath(path = path, color = color)
        }
        NodeShape.DIAMOND -> {
            path.moveTo(center.x, center.y - radius)
            path.lineTo(center.x + radius * 0.7f, center.y)
            path.lineTo(center.x, center.y + radius)
            path.lineTo(center.x - radius * 0.7f, center.y)
            path.close()
            drawPath(path = path, color = color)
        }
        NodeShape.HEXAGON -> {
            for (i in 0..5) {
                val a = i * (PI.toFloat() / 3f) - PI.toFloat() / 6f
                val px = center.x + radius * cos(a)
                val py = center.y + radius * sin(a)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color)
        }
        NodeShape.STAR -> {
            val outerRadius = radius
            val innerRadius = radius * 0.45f
            for (i in 0..9) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val a = i * (PI.toFloat() / 5f) - PI.toFloat() / 2f
                val px = center.x + r * cos(a)
                val py = center.y + r * sin(a)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color)
        }
        NodeShape.CROSS -> {
            val arm = radius * 0.7f
            val thick = radius * 0.28f
            path.addRect(
                androidx.compose.ui.geometry.Rect(
                    left = center.x - thick, top = center.y - arm,
                    right = center.x + thick, bottom = center.y + arm,
                )
            )
            path.addRect(
                androidx.compose.ui.geometry.Rect(
                    left = center.x - arm, top = center.y - thick,
                    right = center.x + arm, bottom = center.y + thick,
                )
            )
            drawPath(path = path, color = color)
        }
        NodeShape.PENTAGON -> {
            for (i in 0..4) {
                val a = i * (2f * PI.toFloat() / 5f) - PI.toFloat() / 2f
                val px = center.x + radius * cos(a)
                val py = center.y + radius * sin(a)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color)
        }
    }
}

/**
 * Draws a sweeping countdown arc around a pressure node.
 * The arc shrinks from full-circle (progress=0) to empty (progress=1).
 * Turns orange/red when below 35% remaining.
 */
private fun DrawScope.drawPressureArc(
    center: Offset,
    radius: Float,
    progress: Float,
    color: Color,
) {
    val arcRadius = radius * 1.52f
    val strokeWidth = 2.8.dp.toPx()
    val remaining = 1f - progress
    val sweepAngle = 360f * remaining
    val warningColor = if (progress > 0.65f) Color(0xFFFF6B35) else color

    // Dim background track
    drawArc(
        color = warningColor.copy(alpha = 0.18f),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
        size = Size(arcRadius * 2f, arcRadius * 2f),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
    // Active arc — remaining time
    if (sweepAngle > 1f) {
        drawArc(
            color = warningColor.copy(alpha = if (progress > 0.65f) 0.95f else 0.70f),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2f, arcRadius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

fun parseHexColor(hex: String): Color {
    val cleanHex = hex.trimStart('#')
    return try {
        val value = cleanHex.toLong(16)
        val r = ((value shr 16) and 0xFF).toFloat() / 255f
        val g = ((value shr 8) and 0xFF).toFloat() / 255f
        val b = (value and 0xFF).toFloat() / 255f
        Color(r, g, b)
    } catch (e: NumberFormatException) {
        Color.Gray
    }
}

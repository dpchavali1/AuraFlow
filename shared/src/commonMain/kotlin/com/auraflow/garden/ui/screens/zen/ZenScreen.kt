package com.auraflow.garden.ui.screens.zen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraflow.garden.data.model.NodeColor
import com.auraflow.garden.ui.screens.game.canvas.parseHexColor
import org.koin.compose.viewmodel.koinViewModel


private fun colorNameToHex(name: String): String =
    NodeColor.entries.find { it.name == name }?.hex ?: "#FFFFFF"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<ZenViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNodeForLink by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zen Garden") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAll() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear all")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Color selector toolbar
            ColorSelectorRow(
                selectedColor = uiState.selectedColor,
                onColorSelected = { viewModel.selectColor(it) },
            )
            Spacer(Modifier.height(8.dp))

            // Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
            ) {
                // Empty state — shown until first node is placed
                if (uiState.nodes.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Your garden awaits",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Pick a color above, then tap the canvas to place nodes.\nTap two same-color nodes to link them.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
                }

                ZenCanvas(
                    uiState = uiState,
                    selectedNodeForLink = selectedNodeForLink,
                    onTap = { normX, normY ->
                        // Find if tap is near a node
                        val tappedNode = uiState.nodes.find { node ->
                            val dx = node.x - normX
                            val dy = node.y - normY
                            kotlin.math.sqrt(dx * dx + dy * dy) < 0.06f
                        }
                        if (tappedNode != null) {
                            val linkSource = selectedNodeForLink
                            if (linkSource != null) {
                                viewModel.linkNodes(linkSource, tappedNode.id)
                                selectedNodeForLink = null
                            } else {
                                selectedNodeForLink = tappedNode.id
                            }
                        } else {
                            selectedNodeForLink = null
                            viewModel.placeNode(normX, normY)
                        }
                    },
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = if (selectedNodeForLink != null) "Tap another node to link" else "Tap to place nodes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp).align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ColorSelectorRow(
    selectedColor: NodeColor,
    onColorSelected: (NodeColor) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NodeColor.entries.forEach { color ->
            val hex = color.hex
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .pointerInput(color) {
                        detectTapGestures { onColorSelected(color) }
                    }
                    .semantics { contentDescription = "${color.displayName} color" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 36.dp else 28.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(hex)),
                )
            }
        }
    }
}

@Composable
private fun ZenCanvas(
    uiState: ZenUiState,
    selectedNodeForLink: String?,
    onTap: (Float, Float) -> Unit,
) {
    val linkPath = remember { androidx.compose.ui.graphics.Path() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val normX = offset.x / size.width
                    val normY = offset.y / size.height
                    onTap(normX, normY)
                }
            },
    ) {
        val w = size.width
        val h = size.height

        // Draw grid
        val gridStep = w / uiState.gridSize
        for (i in 0..uiState.gridSize) {
            val x = i * gridStep
            val y = i * gridStep
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f,
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
            )
        }

        // Draw links
        for (link in uiState.links) {
            val sourceNode = uiState.nodes.find { it.id == link.sourceId } ?: continue
            val targetNode = uiState.nodes.find { it.id == link.targetId } ?: continue
            val hex = colorNameToHex(link.color)
            drawLine(
                color = parseHexColor(hex),
                start = Offset(sourceNode.x * w, sourceNode.y * h),
                end = Offset(targetNode.x * w, targetNode.y * h),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
                alpha = 0.8f,
            )
        }

        // Draw nodes
        for (node in uiState.nodes) {
            val cx = node.x * w
            val cy = node.y * h
            val hex = colorNameToHex(node.color)
            val color = parseHexColor(hex)
            val isSelected = node.id == selectedNodeForLink
            val radius = if (isSelected) 18.dp.toPx() else 14.dp.toPx()

            drawCircle(color = color, radius = radius, center = Offset(cx, cy))
            if (isSelected) {
                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()),
                )
            }
        }
    }
}

package com.auraflow.garden.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraflow.garden.data.model.WorldType
import com.auraflow.garden.game.StageProgress
import com.auraflow.garden.ui.theme.DeepSpace
import com.auraflow.garden.ui.theme.DuskGray
import com.auraflow.garden.ui.theme.IslandSurface
import com.auraflow.garden.ui.theme.LocalWorldTheme
import com.auraflow.garden.ui.theme.LumaGold
import com.auraflow.garden.ui.theme.MoonGray
import com.auraflow.garden.ui.theme.NightSurface
import com.auraflow.garden.ui.theme.StarWhite
import com.auraflow.garden.ui.theme.TwilightRim
import com.auraflow.garden.ui.theme.WorldTheme
import com.auraflow.garden.ui.theme.worldThemeFor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onPlayStage: (Int) -> Unit,
    onOpenZen: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val worldTheme = remember(uiState.currentWorld) {
        worldThemeFor[uiState.currentWorld] ?: worldThemeFor[WorldType.WHISPERING_MEADOW]!!
    }

    CompositionLocalProvider(LocalWorldTheme provides worldTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSpace)
                .semantics { contentDescription = "Home screen" },
        ) {
            // World hero banner
            WorldHeroBanner(
                world = uiState.currentWorld,
                worldTheme = worldTheme,
                onOpenStore = onOpenStore,
                onOpenSettings = onOpenSettings,
            )

            // Progress strip
            ProgressStrip(
                clearedCount = uiState.clearedCount,
                totalStages = uiState.totalStages,
                worldTheme = worldTheme,
            )

            // Continue button
            uiState.nextIncompleteStageId?.let { nextStageId ->
                Button(
                    onClick = { onPlayStage(nextStageId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Continue — Stage $nextStageId")
                }
            }

            // Stage grid
            if (uiState.isLoading) {
                SkeletonGrid(modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.stageProgressList) { stageProgress ->
                        StageCard(
                            stageProgress = stageProgress,
                            worldTheme = worldTheme,
                            onClick = {
                                if (stageProgress.isUnlocked) onPlayStage(stageProgress.stageId)
                            },
                        )
                    }
                }
            }

            // Bottom action bar with Zen Mode
            BottomActionBar(
                worldTheme = worldTheme,
                onOpenZen = onOpenZen,
            )
        }
    }
}

@Composable
private fun WorldHeroBanner(
    world: WorldType,
    worldTheme: WorldTheme,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        worldTheme.bannerGradientStart,
                        worldTheme.surfaceTint,
                        DeepSpace,
                    ),
                )
            ),
    ) {
        // Subtle accent glow at top-left
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            worldTheme.accentPrimary.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    )
                )
        )

        // Top-right icon row
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 4.dp),
        ) {
            IconButton(onClick = onOpenStore) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Store",
                    tint = StarWhite.copy(alpha = 0.8f),
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = StarWhite.copy(alpha = 0.8f),
                )
            }
        }

        // World info at bottom-start
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp),
        ) {
            // World accent tag line
            Text(
                text = world.name.replace('_', ' '),
                style = MaterialTheme.typography.labelSmall,
                color = worldTheme.accentPrimary.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = world.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = worldTheme.accentPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Stages ${world.stageRange.first}–${world.stageRange.last}",
                style = MaterialTheme.typography.bodySmall,
                color = MoonGray,
            )
        }
    }
}

@Composable
private fun ProgressStrip(
    clearedCount: Int,
    totalStages: Int,
    worldTheme: WorldTheme,
) {
    val fraction = if (totalStages > 0) clearedCount.toFloat() / totalStages.toFloat() else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$clearedCount / $totalStages stages cleared",
            style = MaterialTheme.typography.labelLarge,
            color = StarWhite,
            modifier = Modifier.semantics {
                contentDescription = "World progress: $clearedCount of $totalStages stages cleared"
            },
        )
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(TwilightRim),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                worldTheme.accentSecondary,
                                worldTheme.accentPrimary,
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun StageCard(
    stageProgress: StageProgress,
    worldTheme: WorldTheme,
    onClick: () -> Unit,
) {
    val isLocked = !stageProgress.isUnlocked
    val isPerfect = stageProgress.bestStars == 3

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isLocked) Modifier.background(NightSurface)
                else Modifier.background(
                    Brush.radialGradient(
                        colors = listOf(
                            worldTheme.accentPrimary.copy(alpha = if (stageProgress.bestStars > 0) 0.25f else 0.15f),
                            IslandSurface,
                        ),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = when {
                    isLocked -> TwilightRim
                    stageProgress.bestStars > 0 -> worldTheme.accentPrimary.copy(alpha = 0.60f)
                    else -> worldTheme.accentPrimary.copy(alpha = 0.35f)
                },
                shape = RoundedCornerShape(16.dp),
            )
            .alpha(if (isLocked) 0.45f else 1f)
            .clickable(enabled = !isLocked) { onClick() }
            .semantics {
                contentDescription = buildString {
                    append("Stage ${stageProgress.stageId}")
                    if (isLocked) append(", locked")
                    else append(", ${stageProgress.bestStars} stars")
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isLocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = DuskGray,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                Text(
                    text = "${stageProgress.stageId}",
                    style = MaterialTheme.typography.titleLarge,
                    color = StarWhite,
                )
                if (isPerfect) {
                    Text(
                        text = "PERFECT",
                        style = MaterialTheme.typography.labelSmall,
                        color = LumaGold,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            // Stars pinned to bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                for (i in 1..3) {
                    Icon(
                        imageVector = if (i <= stageProgress.bestStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (i <= stageProgress.bestStars) LumaGold else DuskGray,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonGrid(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(15) { index ->
            val shimmerAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 900,
                        delayMillis = index * 40,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "shimmer_$index",
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TwilightRim)
                    .alpha(shimmerAlpha),
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    worldTheme: WorldTheme,
    onOpenZen: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(NightSurface),
        contentAlignment = Alignment.Center,
    ) {
        // Fade-up divider at top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, NightSurface),
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            worldTheme.accentSecondary.copy(alpha = 0.20f),
                            worldTheme.accentPrimary.copy(alpha = 0.20f),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = worldTheme.accentPrimary.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(24.dp),
                )
                .clickable { onOpenZen() },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(worldTheme.accentPrimary, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Zen Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = worldTheme.accentPrimary,
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = worldTheme.accentPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

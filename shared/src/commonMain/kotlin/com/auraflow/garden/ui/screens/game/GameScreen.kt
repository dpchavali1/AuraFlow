package com.auraflow.garden.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import com.auraflow.garden.ui.theme.AuraTeal
import com.auraflow.garden.ui.theme.CosmicViolet
import com.auraflow.garden.ui.theme.DuskGray
import com.auraflow.garden.ui.theme.LumaGold
import com.auraflow.garden.ui.theme.ModalScrim
import com.auraflow.garden.ui.theme.MoonGray
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraflow.garden.data.model.GameStatus
import com.auraflow.garden.ui.theme.NightSurface
import com.auraflow.garden.ui.theme.TwilightRim
import com.auraflow.garden.ui.effects.EffectsLayer
import com.auraflow.garden.ui.narrator.DialogueOverlay
import com.auraflow.garden.ui.narrator.NarratorMessages
import com.auraflow.garden.ui.narrator.NarratorQueue
import com.auraflow.garden.ui.screens.game.canvas.EnergyBar
import com.auraflow.garden.ui.screens.game.canvas.GameCanvasWithAccessibility
import com.auraflow.garden.ui.screens.game.luma.LumaComposable
import com.auraflow.garden.ui.screens.game.luma.LumaViewModel
import com.auraflow.garden.ui.tutorial.TutorialOverlay
import com.auraflow.garden.ui.tutorial.TutorialViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameScreen(
    stageId: Int,
    onBack: () -> Unit,
    onNextLevel: (Int) -> Unit = {},
) {
    val viewModel = koinViewModel<GameViewModel>()
    val lumaViewModel = koinViewModel<LumaViewModel>()
    val tutorialViewModel = koinViewModel<TutorialViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lumaState by lumaViewModel.lumaState.collectAsStateWithLifecycle()
    val tutorialHint by tutorialViewModel.currentHint.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val narrator = remember(coroutineScope) { NarratorQueue(coroutineScope) }
    val narratorMessage by narrator.currentMessage.collectAsStateWithLifecycle()

    var hasShownFirstLink by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    // Delay win overlay so player can enjoy the confetti celebration first
    var showResultOverlay by remember { mutableStateOf(false) }
    LaunchedEffect(state.status) {
        if (state.status == GameStatus.WON) {
            showResultOverlay = false
            delay(1600L)
            showResultOverlay = true
        } else {
            showResultOverlay = false
        }
    }

    LaunchedEffect(stageId) {
        hasShownFirstLink = false
        narrator.clear()
        viewModel.loadStage(stageId)
        tutorialViewModel.onStageStarted(stageId)
        val level = viewModel.state.value.level
        level?.narratorIntro?.let { intro ->
            narrator.enqueue(NarratorMessages.stageIntro(intro))
        }
    }

    // Forward game events to LumaViewModel and Narrator
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            lumaViewModel.onGameEvent(event)
            when (event) {
                is GameEvent.LinkDrawn -> {
                    tutorialViewModel.onPlayerMadeFirstMove(stageId)
                    if (!hasShownFirstLink) {
                        hasShownFirstLink = true
                        narrator.enqueue(NarratorMessages.firstLink())
                    }
                }
                is GameEvent.LinkFailed -> narrator.enqueue(NarratorMessages.invalidLink())
                is GameEvent.EnergyLow -> narrator.enqueue(NarratorMessages.energyLow())
                is GameEvent.Crescendo -> narrator.enqueue(NarratorMessages.crescendo())
                is GameEvent.LevelWon -> {
                    val level = viewModel.state.value.level
                    level?.narratorOutro?.let { outro ->
                        narrator.enqueue(NarratorMessages.stageOutro(outro))
                    } ?: narrator.enqueue(NarratorMessages.levelWon())
                }
                is GameEvent.LevelFailed -> narrator.enqueue(NarratorMessages.levelFailed())
                is GameEvent.AchievementsUnlocked -> { /* achievements shown via LumaViewModel */ }
                is GameEvent.StreakUpdated -> { /* streak shown via narrator if milestone */ }
                is GameEvent.StreakMilestone -> narrator.enqueue(
                    NarratorMessages.streakMilestone(event.days)
                )
                is GameEvent.StreakReset -> { /* silent reset */ }
                is GameEvent.PressureExpired -> { /* energy penalty already applied in ViewModel */ }
            }
        }
    }

    var canvasWidthPx by remember { mutableStateOf(0f) }
    var canvasHeightPx by remember { mutableStateOf(0f) }

    // Compute stars earned (only meaningful when WON)
    val starsEarned = remember(state.energy, state.maxEnergy, state.level) {
        val fraction = if (state.maxEnergy > 0f) state.energy / state.maxEnergy else 0f
        val t = state.level?.starThresholds
        when {
            t == null -> 0
            fraction >= t.threeStar -> 3
            fraction >= t.twoStar -> 2
            fraction >= t.oneStar -> 1
            else -> 0
        }
    }

    // Clear-all confirmation dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all links?") },
            text = { Text("All connections will be removed.") },
            confirmButton = {
                TextButton(onClick = { viewModel.undoAll(); showClearConfirm = false }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeContent),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NightSurface.copy(alpha = 0.92f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = state.level?.title ?: "Stage $stageId",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { viewModel.undoLastLink() },
                enabled = state.links.isNotEmpty() && state.status == GameStatus.PLAYING,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo last link",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(
                onClick = { showClearConfirm = true },
                enabled = state.links.isNotEmpty() && state.status == GameStatus.PLAYING,
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Clear all links",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            // Luma in top bar
            LumaComposable(
                lumaState = lumaState,
                modifier = Modifier.padding(end = 4.dp),
            )
        }

        // Top bar / canvas divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TwilightRim),
        )

        // Energy bar
        EnergyBar(
            energy = state.energy,
            maxEnergy = state.maxEnergy,
            modifier = Modifier.fillMaxWidth(),
        )

        // Game canvas (takes remaining space)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF06061A))
                .onGloballyPositioned { coordinates ->
                    canvasWidthPx = coordinates.size.width.toFloat()
                    canvasHeightPx = coordinates.size.height.toFloat()
                },
        ) {
            GameCanvasWithAccessibility(
                state = state,
                canvasWidthPx = canvasWidthPx,
                canvasHeightPx = canvasHeightPx,
                onNodeTapped = { nodeId -> viewModel.onNodeTapped(nodeId) },
                modifier = Modifier.fillMaxSize(),
            )

            // Visual effects layer (bloom, particles, crescendo)
            EffectsLayer(
                nodes = state.nodes,
                gameStatus = state.status,
                canvasWidthPx = canvasWidthPx,
                canvasHeightPx = canvasHeightPx,
                crescendoTriggered = state.crescendoActive,
                fizzleAt = state.fizzleAt,
                modifier = Modifier.fillMaxSize(),
            )

            // Tutorial overlay — suppressed when narrator is active (no stacking)
            TutorialOverlay(
                hint = if (narratorMessage == null) tutorialHint else null,
                onDismiss = { tutorialViewModel.dismissCurrentHint() },
                modifier = Modifier.align(Alignment.Center),
            )

            // Narrator dialogue overlay
            DialogueOverlay(
                message = narratorMessage,
                onDismiss = { narrator.dismiss() },
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            // Status overlay
            when (state.status) {
                GameStatus.LOADING -> {
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                GameStatus.WON -> {
                    if (showResultOverlay) {
                        ResultOverlay(
                            title = "Level Complete!",
                            titleColor = MaterialTheme.colorScheme.primary,
                            stars = starsEarned,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .graphicsLayer { alpha = 1f },
                            primaryLabel = "Next Level",
                            onPrimary = { onNextLevel(stageId + 1) },
                            secondaryLabel = "Replay",
                            onSecondary = { viewModel.loadStage(stageId) },
                            onHome = onBack,
                        )
                    }
                }
                GameStatus.FAILED -> {
                    ResultOverlay(
                        title = "Out of Energy",
                        titleColor = MaterialTheme.colorScheme.error,
                        stars = 0,
                        modifier = Modifier.align(Alignment.Center),
                        primaryLabel = "Try Again",
                        onPrimary = { viewModel.loadStage(stageId) },
                        secondaryLabel = null,
                        onSecondary = null,
                        onHome = onBack,
                    )
                }
                GameStatus.PAUSED -> {
                    Text(
                        text = "Paused",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun ResultOverlay(
    title: String,
    titleColor: Color,
    stars: Int,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Sequential star reveal animation
    var revealedStars by remember(stars) { mutableIntStateOf(0) }
    var showPerfMessage by remember(stars) { mutableStateOf(false) }
    LaunchedEffect(stars) {
        revealedStars = 0
        showPerfMessage = false
        if (stars >= 1) { delay(450); revealedStars = 1 }
        if (stars >= 2) { delay(380); revealedStars = 2 }
        if (stars >= 3) { delay(380); revealedStars = 3 }
        delay(400)
        showPerfMessage = true
    }

    val perfMessageAlpha by animateFloatAsState(
        targetValue = if (showPerfMessage) 1f else 0f,
        animationSpec = tween(400),
        label = "perf_msg",
    )
    val perfMessage = when (stars) {
        3 -> "PERFECT EFFICIENCY!"
        2 -> "WELL DONE!"
        1 -> "KEEP PRACTICING!"
        else -> ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(ModalScrim)
            .border(1.5.dp, titleColor.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = titleColor,
            )

            // Animated star row
            if (stars > 0 || revealedStars > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (i in 1..3) {
                        val revealed = i <= revealedStars
                        val starScale by animateFloatAsState(
                            targetValue = if (revealed) 1f else 0f,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                            label = "star_$i",
                        )
                        Icon(
                            imageVector = if (revealed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (revealed) LumaGold else DuskGray,
                            modifier = Modifier
                                .size(52.dp)
                                .graphicsLayer { scaleX = starScale; scaleY = starScale },
                        )
                    }
                }
            }

            // Performance message — fades in after star reveal
            if (perfMessage.isNotEmpty()) {
                Text(
                    text = perfMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = when (stars) {
                        3 -> LumaGold
                        2 -> AuraTeal
                        else -> MoonGray
                    }.copy(alpha = perfMessageAlpha),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Gradient primary button (Next Level / Try Again)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CosmicViolet, AuraTeal)
                        )
                    )
                    .clickable { onPrimary() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = primaryLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }

            if (secondaryLabel != null && onSecondary != null) {
                OutlinedButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(secondaryLabel, color = MoonGray)
                }
            }

            OutlinedButton(
                onClick = onHome,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(22.dp),
                border = null,
            ) {
                Text("Home", color = DuskGray, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

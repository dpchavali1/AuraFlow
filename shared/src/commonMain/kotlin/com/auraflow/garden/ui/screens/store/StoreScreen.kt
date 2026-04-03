package com.auraflow.garden.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraflow.garden.platform.billing.ProductIds
import com.auraflow.garden.platform.billing.ProductInfo
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<StoreViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is StoreEvent.PurchaseSuccess -> snackbarHostState.showSnackbar("Purchase successful!")
                is StoreEvent.PurchasePending -> snackbarHostState.showSnackbar("Purchase pending approval.")
                is StoreEvent.PurchaseCancelled -> { /* no message needed */ }
                is StoreEvent.PurchaseError -> snackbarHostState.showSnackbar("Purchase failed: ${event.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Garden Store") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.restorePurchases() }) {
                        Text("Restore")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                WardenPassCard(
                    product = uiState.wardenPassProduct,
                    isOwned = uiState.hasWardenPass,
                    isPurchasing = uiState.purchasingProductId == ProductIds.WARDEN_PASS,
                    onPurchase = { viewModel.purchaseProduct(ProductIds.WARDEN_PASS) },
                )
            }

            if (uiState.auraSkins.isNotEmpty()) {
                item { StoreSectionHeader("Aura Skins") }
                items(uiState.auraSkins) { product ->
                    CosmeticProductCard(
                        product = product,
                        isPurchasing = uiState.purchasingProductId == product.productId,
                        onPurchase = { viewModel.purchaseProduct(product.productId) },
                    )
                }
            }

            if (uiState.lumaSkins.isNotEmpty()) {
                item { StoreSectionHeader("Luma Skins") }
                items(uiState.lumaSkins) { product ->
                    CosmeticProductCard(
                        product = product,
                        isPurchasing = uiState.purchasingProductId == product.productId,
                        onPurchase = { viewModel.purchaseProduct(product.productId) },
                    )
                }
            }

            if (uiState.seasonalPacks.isNotEmpty()) {
                item { StoreSectionHeader("Seasonal Packs") }
                items(uiState.seasonalPacks) { product ->
                    CosmeticProductCard(
                        product = product,
                        isPurchasing = uiState.purchasingProductId == product.productId,
                        onPurchase = { viewModel.purchaseProduct(product.productId) },
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WardenPassCard(
    product: ProductInfo?,
    isOwned: Boolean,
    isPurchasing: Boolean,
    onPurchase: () -> Unit,
) {
    val goldColor = Color(0xFFFFD700)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, goldColor, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Warden's Pass",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = goldColor,
                )
                Spacer(Modifier.width(8.dp))
                if (isOwned) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Owned",
                        tint = goldColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Unlock all 50 stages, exclusive aura effects, and the Zen Mode garden sandbox.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            if (!isOwned) {
                Button(
                    onClick = onPurchase,
                    enabled = !isPurchasing && product != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                    } else {
                        Text(
                            text = product?.price ?: "$3.99",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                Text(
                    text = "You own this pass",
                    style = MaterialTheme.typography.bodySmall,
                    color = goldColor,
                )
            }
        }
    }
}

@Composable
private fun StoreSectionHeader(title: String) {
    Column {
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun CosmeticProductCard(
    product: ProductInfo,
    isPurchasing: Boolean,
    onPurchase: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = product.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(12.dp))
        Button(
            onClick = onPurchase,
            enabled = !isPurchasing,
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Text(product.price)
            }
        }
    }
}

package com.auraflow.garden.ui.screens.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraflow.garden.platform.billing.BillingManager
import com.auraflow.garden.platform.billing.EntitlementManager
import com.auraflow.garden.platform.billing.ProductIds
import com.auraflow.garden.platform.billing.ProductInfo
import com.auraflow.garden.platform.billing.PurchaseResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreUiState(
    val isLoading: Boolean = true,
    val hasWardenPass: Boolean = false,
    val wardenPassProduct: ProductInfo? = null,
    val auraSkins: List<ProductInfo> = emptyList(),
    val lumaSkins: List<ProductInfo> = emptyList(),
    val seasonalPacks: List<ProductInfo> = emptyList(),
    val giftProduct: ProductInfo? = null,
    val purchasingProductId: String? = null,
)

sealed class StoreEvent {
    data class PurchaseSuccess(val productId: String) : StoreEvent()
    data class PurchasePending(val productId: String) : StoreEvent()
    data object PurchaseCancelled : StoreEvent()
    data class PurchaseError(val message: String) : StoreEvent()
}

class StoreViewModel(
    private val billingManager: BillingManager,
    private val entitlementManager: EntitlementManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StoreEvent>()
    val events: SharedFlow<StoreEvent> = _events.asSharedFlow()

    private val auraSkinIds = setOf(ProductIds.AURA_SKIN_AURORA, ProductIds.AURA_SKIN_NEBULA, ProductIds.AURA_SKIN_EMBER)
    private val lumaSkinIds = setOf(ProductIds.LUMA_SKIN_OWL, ProductIds.LUMA_SKIN_BUTTERFLY)
    private val seasonalIds = setOf(ProductIds.SEASONAL_PACK_WINTER)

    init {
        observePurchaseEvents()
        observeProducts()
        loadStore()
    }

    private fun loadStore() {
        viewModelScope.launch {
            billingManager.initialize()
            billingManager.queryProducts()

            val hasPass = entitlementManager.hasWardenPass()
            _uiState.value = _uiState.value.copy(
                hasWardenPass = hasPass,
                isLoading = false,
            )
        }
    }

    private fun observeProducts() {
        viewModelScope.launch {
            billingManager.availableProducts.collect { products ->
                val wardenPass = products.find { it.productId == ProductIds.WARDEN_PASS }
                val auraSkins = products.filter { it.productId in auraSkinIds }
                val lumaSkins = products.filter { it.productId in lumaSkinIds }
                val seasonalPacks = products.filter { it.productId in seasonalIds }
                val gift = products.find { it.productId == ProductIds.GIFT_GARDEN }

                _uiState.value = _uiState.value.copy(
                    wardenPassProduct = wardenPass,
                    auraSkins = auraSkins,
                    lumaSkins = lumaSkins,
                    seasonalPacks = seasonalPacks,
                    giftProduct = gift,
                )
            }
        }
    }

    private fun observePurchaseEvents() {
        viewModelScope.launch {
            billingManager.purchaseEvents.collect { result ->
                _uiState.value = _uiState.value.copy(purchasingProductId = null)
                when (result) {
                    is PurchaseResult.Success -> {
                        entitlementManager.syncEntitlements()
                        val hasPass = entitlementManager.hasWardenPass()
                        _uiState.value = _uiState.value.copy(hasWardenPass = hasPass)
                        _events.emit(StoreEvent.PurchaseSuccess(result.productId))
                    }
                    is PurchaseResult.Pending -> {
                        _events.emit(StoreEvent.PurchasePending(result.productId))
                    }
                    is PurchaseResult.Cancelled -> {
                        _events.emit(StoreEvent.PurchaseCancelled)
                    }
                    is PurchaseResult.Error -> {
                        _events.emit(StoreEvent.PurchaseError(result.message))
                    }
                }
            }
        }
    }

    fun purchaseProduct(productId: String) {
        if (_uiState.value.purchasingProductId != null) return  // already purchasing
        _uiState.value = _uiState.value.copy(purchasingProductId = productId)
        viewModelScope.launch {
            billingManager.purchase(productId)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            billingManager.restorePurchases()
            entitlementManager.syncEntitlements()
            val hasPass = entitlementManager.hasWardenPass()
            _uiState.value = _uiState.value.copy(hasWardenPass = hasPass)
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.release()
    }
}

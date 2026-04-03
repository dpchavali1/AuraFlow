package com.auraflow.garden.platform.billing

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-platform IAP wrapper.
 * Android actual: Google Play Billing v6
 * iOS actual: StoreKit 2
 *
 * IMPORTANT: purchaseEvents must be observed before calling purchase().
 * On iOS, Transaction.updates listener must be started at app launch.
 */
expect class BillingManager {
    val billingState: StateFlow<BillingState>
    val purchaseEvents: SharedFlow<PurchaseResult>
    val availableProducts: StateFlow<List<ProductInfo>>

    fun initialize()
    suspend fun queryProducts()
    suspend fun purchase(productId: String)
    suspend fun restorePurchases()
    fun hasPurchase(productId: String): Boolean
    fun release()
}

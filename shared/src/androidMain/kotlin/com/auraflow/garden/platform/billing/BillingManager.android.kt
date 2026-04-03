package com.auraflow.garden.platform.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.auraflow.garden.platform.PlatformContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class BillingManager(private val platformContext: PlatformContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _billingState = MutableStateFlow(BillingState.DISCONNECTED)
    actual val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 10)
    actual val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val _availableProducts = MutableStateFlow<List<ProductInfo>>(emptyList())
    actual val availableProducts: StateFlow<List<ProductInfo>> = _availableProducts.asStateFlow()

    private val confirmedPurchases = mutableSetOf<String>()
    private val productDetailsCache = mutableMapOf<String, ProductDetails>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            scope.launch { _purchaseEvents.emit(PurchaseResult.Cancelled) }
        } else {
            scope.launch {
                _purchaseEvents.emit(PurchaseResult.Error(billingResult.responseCode, billingResult.debugMessage))
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(platformContext.context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    actual fun initialize() {
        scope.launch { connectWithRetry() }
    }

    private suspend fun connectWithRetry() {
        var delayMs = 1_000L
        val maxRetries = 5
        var attempt = 0
        _billingState.value = BillingState.CONNECTING

        while (attempt < maxRetries) {
            try {
                connectBillingClient()
                _billingState.value = BillingState.CONNECTED
                return
            } catch (e: Exception) {
                attempt++
                if (attempt >= maxRetries) {
                    _billingState.value = BillingState.ERROR
                    return
                }
                delay(delayMs)
                delayMs = minOf(delayMs * 2, 32_000L)
            }
        }
    }

    private suspend fun connectBillingClient() = suspendCancellableCoroutine { continuation ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (continuation.isActive) continuation.resume(Unit)
                } else {
                    if (continuation.isActive) {
                        continuation.resumeWithException(Exception("Billing setup failed: ${billingResult.debugMessage}"))
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.DISCONNECTED
                scope.launch { connectWithRetry() }
            }
        })
    }

    actual suspend fun queryProducts() {
        if (!billingClient.isReady) return

        val productList = ProductIds.ALL.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList?.let { details ->
                details.forEach { productDetailsCache[it.productId] = it }
                _availableProducts.value = details.map { it.toProductInfo() }
            }
        }
    }

    actual suspend fun purchase(productId: String) {
        if (!billingClient.isReady) {
            _purchaseEvents.emit(PurchaseResult.Error(-1, "Billing not ready"))
            return
        }

        val productDetails = productDetailsCache[productId]
        if (productDetails == null) {
            _purchaseEvents.emit(PurchaseResult.Error(-1, "Product not found: $productId"))
            return
        }

        val activity = platformContext.context as? Activity
        if (activity == null) {
            _purchaseEvents.emit(PurchaseResult.Error(-1, "Activity context required for billing"))
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
        // Result delivered via purchasesUpdatedListener
    }

    actual suspend fun restorePurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            confirmedPurchases.clear()
            for (purchase in result.purchasesList) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    for (productId in purchase.products) {
                        confirmedPurchases.add(productId)
                    }
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
            }
        }
    }

    actual fun hasPurchase(productId: String): Boolean = confirmedPurchases.contains(productId)

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            for (productId in purchase.products) {
                confirmedPurchases.add(productId)
                _purchaseEvents.emit(PurchaseResult.Success(productId))
            }
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            for (productId in purchase.products) {
                _purchaseEvents.emit(PurchaseResult.Pending(productId))
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { _ -> /* ignore result */ }
    }

    actual fun release() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    private fun ProductDetails.toProductInfo(): ProductInfo {
        val oneTimePriceInfo = oneTimePurchaseOfferDetails
        return ProductInfo(
            productId = productId,
            title = title,
            description = description,
            price = oneTimePriceInfo?.formattedPrice ?: "",
            priceAmountMicros = oneTimePriceInfo?.priceAmountMicros ?: 0L,
            currencyCode = oneTimePriceInfo?.priceCurrencyCode ?: "",
            type = ProductType.ONE_TIME,
        )
    }
}

package com.auraflow.garden.platform.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.StoreKit.SKMutablePayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject

/**
 * iOS actual BillingManager using StoreKit 1.
 * StoreKit 2 requires Swift async/await which is difficult to bridge from Kotlin/Native.
 * Using SK1 with a Kotlin/Native observer pattern instead.
 *
 * NOTE: The Transaction.updates listener start is handled in iOSAppDelegate.swift
 * via the startTransactionObserver() method called at app launch.
 */
actual class BillingManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _billingState = MutableStateFlow(BillingState.DISCONNECTED)
    actual val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 10)
    actual val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val _availableProducts = MutableStateFlow<List<ProductInfo>>(emptyList())
    actual val availableProducts: StateFlow<List<ProductInfo>> = _availableProducts.asStateFlow()

    private val confirmedPurchases = mutableSetOf<String>()
    private val productCache = mutableMapOf<String, SKProduct>()

    private inner class TransactionObserver : NSObject(), SKPaymentTransactionObserverProtocol {
        override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
            scope.launch {
                for (transaction in updatedTransactions) {
                    val tx = transaction as? SKPaymentTransaction ?: continue
                    handleTransaction(tx, queue)
                }
            }
        }
    }

    private val observer = TransactionObserver()

    actual fun initialize() {
        SKPaymentQueue.defaultQueue().addTransactionObserver(observer)
        _billingState.value = BillingState.CONNECTED
    }

    actual suspend fun queryProducts() {
        // Kotlin Set<String> bridges to NSSet automatically in Kotlin/Native
        val productIdSet: Set<Any?> = ProductIds.ALL.toSet()
        val request = SKProductsRequest(productIdentifiers = productIdSet)

        val delegate = object : NSObject(), SKProductsRequestDelegateProtocol {
            override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
                val products = didReceiveResponse.products.filterIsInstance<SKProduct>()
                products.forEach { product ->
                    // Cache both with and without bundle ID prefix
                    val strippedId = product.productIdentifier.removePrefix("com.auraflow.garden.")
                    productCache[strippedId] = product
                    productCache[product.productIdentifier] = product
                }
                scope.launch {
                    _availableProducts.value = products.map { it.toProductInfo() }
                }
            }

            override fun request(request: SKRequest, didFailWithError: NSError) {
                scope.launch {
                    _billingState.value = BillingState.ERROR
                }
            }
        }

        request.delegate = delegate
        request.start()
    }

    actual suspend fun purchase(productId: String) {
        val product = productCache[productId] ?: productCache["com.auraflow.garden.$productId"]
        if (product == null) {
            _purchaseEvents.emit(PurchaseResult.Error(-1, "Product not found: $productId"))
            return
        }
        val payment = SKMutablePayment.paymentWithProduct(product)
        SKPaymentQueue.defaultQueue().addPayment(payment)
    }

    actual suspend fun restorePurchases() {
        SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
    }

    actual fun hasPurchase(productId: String): Boolean = confirmedPurchases.contains(productId)

    private suspend fun handleTransaction(tx: SKPaymentTransaction, queue: SKPaymentQueue) {
        val rawId = tx.payment.productIdentifier
        val productId = rawId.removePrefix("com.auraflow.garden.")
        when (tx.transactionState) {
            SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
            SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                confirmedPurchases.add(productId)
                confirmedPurchases.add(rawId)
                _purchaseEvents.emit(PurchaseResult.Success(productId))
                queue.finishTransaction(tx)
            }
            SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                val error = tx.error
                if (error != null && error.code == 2L) { // SKErrorPaymentCancelled
                    _purchaseEvents.emit(PurchaseResult.Cancelled)
                } else {
                    _purchaseEvents.emit(PurchaseResult.Error(
                        error?.code?.toInt() ?: -1,
                        error?.localizedDescription ?: "Unknown error",
                    ))
                }
                queue.finishTransaction(tx)
            }
            SKPaymentTransactionState.SKPaymentTransactionStatePurchasing -> {
                _purchaseEvents.emit(PurchaseResult.Pending(productId))
            }
            else -> {}
        }
    }

    actual fun release() {
        SKPaymentQueue.defaultQueue().removeTransactionObserver(observer)
    }

    private fun SKProduct.toProductInfo(): ProductInfo {
        val strippedId = productIdentifier.removePrefix("com.auraflow.garden.")
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.locale = priceLocale
        val formattedPrice = formatter.stringFromNumber(price) ?: "$0.00"
        val currency = priceLocale.objectForKey("currency") as? String ?: "USD"
        return ProductInfo(
            productId = strippedId,
            title = localizedTitle,
            description = localizedDescription,
            price = formattedPrice,
            priceAmountMicros = (price.doubleValue * 1_000_000).toLong(),
            currencyCode = currency,
            type = ProductType.ONE_TIME,
        )
    }
}

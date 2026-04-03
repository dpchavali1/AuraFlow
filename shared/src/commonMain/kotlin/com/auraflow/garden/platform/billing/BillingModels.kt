package com.auraflow.garden.platform.billing

enum class ProductType {
    ONE_TIME,
    SUBSCRIPTION,
}

data class ProductInfo(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long,
    val currencyCode: String,
    val type: ProductType,
)

sealed class PurchaseResult {
    data class Success(val productId: String) : PurchaseResult()
    data class Pending(val productId: String) : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val code: Int, val message: String) : PurchaseResult()
}

enum class BillingState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
}

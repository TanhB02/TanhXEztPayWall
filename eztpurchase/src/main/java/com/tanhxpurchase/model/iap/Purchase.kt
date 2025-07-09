package com.tanhxpurchase.model.iap


data class Purchase(
    val productId: String,
    val type: String,
    val quantity: Int,
    val orderId: String? = null,
    val packageName: String? = null,
    val purchaseToken: String? = null,
    val purchaseTime: Long = 0L,
    val purchaseState: Int = 1,
    val autoRenewing: Boolean = false,
    val acknowledged: Boolean = false
)
package com.tanhxpurchase.listeners

import com.tanhxpurchase.model.iap.Purchase

interface PurchaseUpdateListener {
    fun onPurchaseSuccess(purchases: Purchase) { }
    fun onPurchaseFailure(code: Int, errorMsg: String?) { }
    fun onUserCancelBilling() { }
    fun onOwnedProduct(productId: String) { }
}
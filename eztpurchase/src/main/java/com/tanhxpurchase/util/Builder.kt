package com.tanhxpurchase.util

import com.tanhxpurchase.PurchaseUtils.addSubscriptionAndProduct
import com.tanhxpurchase.PurchaseUtils.setListRemoveAdsId

class Builder() {
    private val mSubscriptions: MutableList<String> = mutableListOf()
    private val mOneTimeProducts: MutableList<String> = mutableListOf()
    private val mConsumableProducts: MutableList<String> = mutableListOf()
    private val mRemoveAds: MutableList<String> = mutableListOf()

    fun subscriptions(subscriptions: List<String>) = apply {
        mSubscriptions.clear()
        mSubscriptions.addAll(subscriptions)
    }

    fun subscriptions(vararg subscriptions: String) = apply {
        mSubscriptions.clear()
        mSubscriptions.addAll(subscriptions)
    }

    fun oneTimeProducts(oneTimeProducts: List<String>) = apply {
        mOneTimeProducts.clear()
        mOneTimeProducts.addAll(oneTimeProducts)
    }

    fun oneTimeProducts(vararg oneTimeProducts: String) = apply {
        mOneTimeProducts.clear()
        mOneTimeProducts.addAll(oneTimeProducts)
    }

    fun consumableProducts(consumableProducts: List<String>) = apply {
        mConsumableProducts.clear()
        mConsumableProducts.addAll(consumableProducts)
    }

    fun consumableProducts(vararg consumableProducts: String) = apply {
        mConsumableProducts.clear()
        mConsumableProducts.addAll(consumableProducts)
    }


    fun removeAds(removeAds: List<String>) = apply {
        mRemoveAds.clear()
        mRemoveAds.addAll(removeAds)
    }

    fun removeAds(vararg removeAds: String) = apply {
        mRemoveAds.clear()
        mRemoveAds.addAll(removeAds)
    }


    fun build() {
        addSubscriptionAndProduct(
            mSubscriptions,
            mOneTimeProducts,
            mConsumableProducts
        )
        setListRemoveAdsId(mRemoveAds)
    }
}

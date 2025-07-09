package com.tanhxpurchase.util

import android.os.Bundle
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.tanhxpurchase.model.iap.BasePlanSubscription
import com.tanhxpurchase.model.iap.OfferSubscription
import com.tanhxpurchase.model.iap.OnetimeProduct
import com.tanhxpurchase.model.iap.Phase

/**
 * Find all offer in a product detail
 */
private fun ProductDetails.findOffers(basePlan: BasePlanSubscription): List<OfferSubscription> {
    val result = mutableListOf<OfferSubscription>()
    subscriptionOfferDetails?.forEach {
        if ((it.offerId != null) && (it.basePlanId == basePlan.basePlanId)) {
            result.add(
                OfferSubscription(
                    it.offerId!!,
                    basePlan,
                    it.pricingPhases.pricingPhaseList.map { Phase.fromPricingPhase(it) },
                    productId,
                    it.offerToken
                )
            )
        }
    }
    return result
}


fun ProductDetails.findAllBasePlan(): List<BasePlanSubscription> {
    val result = mutableListOf<BasePlanSubscription>()
    subscriptionOfferDetails?.forEach {
        if (it.offerId == null) {
            result.add(it.toBasePlan(productId))
        }
    }
    result.forEach {
        it.offers = findOffers(it)
    }
    return result
}

fun SubscriptionOfferDetails.toBasePlan(productId: String) = BasePlanSubscription(
    basePlanId,
    offerTags,
    Phase.fromPricingPhase(pricingPhases.pricingPhaseList[0]),
    productId,
    offerToken
)

fun OneTimePurchaseOfferDetails.toOneTimeProduct(productId: String) =
    OnetimeProduct(productId, formattedPrice, priceAmountMicros / 1000000f, priceCurrencyCode)

@JvmOverloads
fun logFirebaseEvent(eventName: String, bundle: Bundle? = null) {
    Firebase.analytics.logEvent(eventName, bundle)
}


package com.tanhxpurchase.model

/**
 * Subscription is class that represent a subscription in Play Billing System.
 * @see [OfferSubscription]
 * @see BasePlanSubscription
 */
abstract class Subscription(
    internal val productId: String,
    internal val token: String,
)
package com.tanhxpurchase.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tanhxpurchase.EZT_Purchase
import com.tanhxpurchase.listeners.PurchaseUpdateListener
import com.tanhxpurchase.model.BasePlanSubscription
import com.tanhxpurchase.model.OfferSubscription
import com.tanhxpurchase.model.OnetimeProduct
import com.tanhxpurchase.model.Subscription
import com.tanhxpurchase.sharepreference.EzTechPreferences
import com.tanhxpurchase.util.createStandardJsonPayload
import com.tanhxpurchase.util.findAllBasePlan
import com.tanhxpurchase.util.logd
import com.tanhxpurchase.util.logeSelf
import com.tanhxpurchase.util.toOneTimeProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Collections
import kotlin.coroutines.CoroutineContext

internal class BillingService private constructor() : PurchasesUpdatedListener,
    BillingClientStateListener, CoroutineScope {
    companion object {
        private val instant by lazy { BillingService() }

        @JvmStatic
        internal fun getInstance() = instant
    }

    private fun logDebug(message: String? = null) {
        logd(message, EZT_Purchase)
    }

    private lateinit var billingClient: BillingClient

    private val setSubscriptionId = mutableSetOf<String>()
    private val setOneTimeProductId = mutableSetOf<String>()
    private val setConsumableId = mutableSetOf<String>()

    private val setRemoveAdsId = mutableSetOf<String>()

    private val productDetailMap = hashMapOf<String, ProductDetails>()
    private val subscriptionMap = hashMapOf<String, Subscription>()
    private val onetimeProductMap = hashMapOf<String, OnetimeProduct>()

    private val _ownedProducts = MutableStateFlow(mutableSetOf<String>())
    val ownedProducts: StateFlow<Set<String>> = _ownedProducts.asStateFlow()

    private var listPurchaseUpdateListener = arrayListOf<PurchaseUpdateListener>()
    internal var onInitBillingFinish: (() -> Unit)? = null

    private val cachedOwnedProducts = mutableSetOf<String>()
    private var syncPurchased = false

    private var isConnecting = false


    init {
        val ownedProduct = Gson().fromJson<List<String>>(
            EzTechPreferences.valueOf("ownedProduct", ""),
            object : TypeToken<List<String>>() {}.type
        )
        if (ownedProduct != null) {
            logDebug("Found ownedProduct in Eztreferences: $ownedProduct")
            cachedOwnedProducts.addAll(ownedProduct)
        }
    }

    private fun onOwned(productId: String) {
        val addSuccess = _ownedProducts.value.add(productId)
        if (addSuccess) {
            _ownedProducts.value = _ownedProducts.value.toMutableSet()
            logDebug("productId was add $productId")
            onOwnedProduct(productId)
            logDebug("ownProducts size: ${_ownedProducts.value.size}, details: = $_ownedProducts")
        }
    }

    fun addPurchaseUpdateListener(listener: PurchaseUpdateListener) {
        listPurchaseUpdateListener.add(listener)
    }

    /**
     * Khởi tạo billing client
     */
    fun initBillingClient(context: Context) {
        if (!this::billingClient.isInitialized) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        }
        if (!billingClient.isReady) {
            logDebug("billing client initializing...")
            startConnection()
        }
    }

    /**
     * Add one time product. After add success, all product and purchase will be query again
     */
    fun addOneTimeProductId(listId: List<String>) {
        if (listId.isNotEmpty()) {
            setOneTimeProductId.addAll(listId)
            if (!billingClient.isReady) {
                startConnection()
            } else launch {
                queryProductDetails()
                runBlocking {
                    queryPurchases()
                }
            }
        }
    }

    /**
     * Add subscription. After add success, all product and purchase will be query again
     */
    fun addSubscriptionId(listId: List<String>) {
        if (listId.isNotEmpty()) {
            setSubscriptionId.addAll(listId)
            if (!billingClient.isReady) {
                startConnection()
            } else launch {
                queryProductDetails()
                runBlocking {
                    queryPurchases()
                }
            }
        }
    }

    fun setListRemoveAdsId(list: List<String>) {
        setRemoveAdsId.addAll(list)
    }

    fun addAllSubsAndProduct(
        listSubscriptionId: List<String>,
        listOnetimeProductId: List<String>,
        listConsumableProductId: List<String>
    ) {
        setSubscriptionId.addAll(listSubscriptionId)
        setOneTimeProductId.addAll(listOnetimeProductId)
        setConsumableId.addAll(listConsumableProductId)
        if (!billingClient.isReady) {
            startConnection()
        } else launch {
            queryProductDetails()
            runBlocking {
                queryPurchases()
            }
        }
    }

    /**
     * Connect Google Play Billing
     */
    fun startConnection() {
        isConnecting = true
        billingClient.startConnection(this)
    }

    override fun onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
        retryConnection()
    }

    private val maxRetryConnection = 5
    var retryCount = 0
    /**
     * Reconnect max 5 lần
     */
    private fun retryConnection() {
        if (!isConnecting) {
            if (retryCount < maxRetryConnection) {
                launch {
                    delay(5000)
                    retryCount++
                    startConnection()
                }
            }
        }
    }

    /**
     * In order to make purchases, you need the [ProductDetails] for the item or subscription.
     * This is an asynchronous call that will receive a result in [onProductDetailsResponse]
     */
    private fun queryProductDetails() {

        val subDetailParams = QueryProductDetailsParams.newBuilder()

        val subscriptionList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()
        for (id in setSubscriptionId) {
            subscriptionList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        val productDetailParams = QueryProductDetailsParams.newBuilder()
        val productList = arrayListOf<QueryProductDetailsParams.Product>()
        setOneTimeProductId.forEach { id ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }
        setConsumableId.forEach { id ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(id)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        runBlocking {
            logDebug("Querying async ProductDetail ...")
            if (subscriptionList.isNotEmpty()) {
                subDetailParams.setProductList(subscriptionList).let { params ->
                    val result = billingClient.queryProductDetails(params.build())
                    logDebug("Query subscription detail response ${result.billingResult.responseCode} ${result.billingResult.debugMessage}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        logDebug("Found ${result.productDetailsList?.size} subscription")
//                        val standardJsonPayload = createStandardJsonPayload(result.productDetailsList)
                        logDebug("📦 STANDARD JSON PAYLOAD:")
//                        logDebug(standardJsonPayload)
                        result.productDetailsList?.forEach { productDetail ->
                            //store detail
                            productDetailMap[productDetail.productId] = productDetail
                            //get subscriptions and put all it into map
                            productDetail.findAllBasePlan().forEach { basePlan ->
                                subscriptionMap[basePlan.basePlanId] = basePlan
                                basePlan.offers.forEach { offer ->
                                    subscriptionMap[offer.offerId] = offer
                                }
                            }
                        }
                    }
                }
            }

            //Query ProductDetail for products
            if (productList.isNotEmpty()) {
                productDetailParams.setProductList(productList).let { params ->
                    billingClient.queryProductDetails(params.build())
                    val result = billingClient.queryProductDetails(params.build())
                    logDebug("Query product detail response ${result.billingResult.responseCode}")
                    val response = BillingResponse(result.billingResult.responseCode)
                    if (response.isOk) {
                        logDebug("Found ${result.productDetailsList?.size} one time product")
                        result.productDetailsList?.forEach { productDetail ->
                            logDebug(productDetail.toString())
                            // store detail
                            productDetailMap[productDetail.productId] = productDetail
                            onetimeProductMap[productDetail.productId] =
                                productDetail.oneTimePurchaseOfferDetails!!.toOneTimeProduct(
                                    productDetail.productId
                                )
                        }
                    }
                }
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     */
    fun queryPurchases() {
        logDebug("Querying Purchases....")
        runBlocking {
            if (!billingClient.isReady) {
                logDebug("queryPurchases: BillingClient is not ready")
                billingClient.startConnection(this@BillingService)
                return@runBlocking
            }
            val subPurchaseResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            if (BillingResponse(subPurchaseResult.billingResult.responseCode).isOk) {
                logDebug("Query subscription purchases....")
                processPurchase(subPurchaseResult.purchasesList)
            }
            val productPurchaseResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            if (BillingResponse(productPurchaseResult.billingResult.responseCode).isOk) {
                logDebug("Query product purchases....")
                processPurchase(productPurchaseResult.purchasesList)
            }
            logDebug("Finish query purchase.... Found ${_ownedProducts.value.size} purchases")
            EzTechPreferences.setValue(
                "ownedProduct",
                Gson().toJson(_ownedProducts.value.toMutableList())
            )
            syncPurchased = true
        }
    }

    private suspend fun processPurchase(purchases: List<Purchase>?) {
        if (!purchases.isNullOrEmpty()) {
            for (purchase in purchases) {
                logDebug("Purchase state: ${purchase.purchaseState}")
                if (purchase.purchaseState == PurchaseState.PURCHASED || purchase.purchaseState == PurchaseState.PENDING) {
                    //Grant entitlement to the user.
                    purchase.products.forEach { productId ->
                        val detail = productDetailMap[productId]
                        logDebug("Product id: $productId, type: ${detail?.productType}")
                        when (detail?.productType) {
                            BillingClient.ProductType.INAPP -> {
                                //consume purchase
                                if (setConsumableId.contains(productId)) consumePurchase(
                                    productId,
                                    purchase.purchaseToken
                                )
                                else onOwned(productId)
                            }

                            BillingClient.ProductType.SUBS -> {
                                onOwned(productId)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun consumePurchase(
        productId: String,
        purchaseToken: String
    ) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
        val result = billingClient.consumePurchase(consumeParams)
        val response = BillingResponse(result.billingResult.responseCode)
        if (response.isOk) {
            onOwned(productId)
        } else {
            logDebug("Consume purchase failure with code: ${result.billingResult.responseCode}")
        }

    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        isConnecting = false
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        if (responseCode == BillingResponseCode.OK) {
            // The BillingClient is ready. You can query purchases here.
            logDebug("onBillingSetupFinished: $responseCode $debugMessage")
            onInitBillingFinish?.invoke()
            launch {
                queryProductDetails()
                queryPurchases()
            }

        } else {
            logDebug("Error when billing setup: error code = $responseCode message = $debugMessage")
            if (responseCode != BillingResponseCode.DEVELOPER_ERROR) {
                onInitBillingFinish?.invoke()
            }
        }
    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val responseCode = billingResult.responseCode
        launch {
            when (responseCode) {
                BillingResponseCode.OK -> {
                    if (purchases == null) {
                        processPurchase(null)
                    } else {
                        processPurchase(purchases)
                        purchases.forEach { purchase ->
                            purchase.products.forEach {
                                onProductPurchased(it, purchase)
                            }
                        }
                    }
                }

                BillingResponseCode.USER_CANCELED -> {
                    onUserCancelBilling()
                }

                BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    onPurchaseFailure(responseCode, "The user already owns this item")
                    queryPurchases()
                }

                BillingResponseCode.DEVELOPER_ERROR -> {
                    logDebug("developer error")
                    onPurchaseFailure(
                        responseCode, "Developer error means that Google Play " +
                                "does not recognize the configuration. If you are just getting started, " +
                                "make sure you have configured the application correctly in the " +
                                "Google Play Console. The product ID must match and the APK you " +
                                "are using must be signed with release keys."
                    )
                }

                BillingResponseCode.SERVICE_DISCONNECTED -> {
                    retryConnection()
                    onPurchaseFailure(
                        responseCode, "service disconnected, please wait a minute for re-connect"
                    )
                }

                else -> {
                    onPurchaseFailure(responseCode, "An error occur")
                }
            }
        }
    }

    fun buy(activity: Activity, id: String) {
        productDetailMap[id]?.let { launchBillingFlow(activity, it) }
            ?: subscriptionMap[id]?.let {
                launchBillingFlow(activity, productDetailMap[it.productId]!!, it.token)
            }
            ?: "Can not find any basePlan or offer or oneTimeProduct that has id = $id. Please check your id again".logeSelf()
    }


    private fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails,
        token: String? = null
    ) {
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
        token?.let { params.setOfferToken(it) }
        val billingParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(params.build())
        ).build()

        logDebug("IAP_ShowPopupSystem")
        billingClient.launchBillingFlow(activity, billingParams)
    }

    fun getPrice(id: String): String {
        return onetimeProductMap[id]?.price ?: subscriptionMap[id]?.let {
            when (it) {
                is BasePlanSubscription -> it.price
                is OfferSubscription -> it.discountPrice
                else -> ""
            }
        } ?: ""
    }

    fun getPriceWithoutCurrency(id: String): Float {
        return onetimeProductMap[id]?.priceWithoutCurrency ?: subscriptionMap[id]?.let {
            when (it) {
                is BasePlanSubscription -> it.priceWithoutCurrency
                is OfferSubscription -> it.discountPriceWithoutCurrency
                else -> 0f
            }
        } ?: 0f
    }

    fun getCurrency(id: String): String {
        return onetimeProductMap[id]?.priceCurrencyCode ?: subscriptionMap[id]?.let {
            when (it) {
                is BasePlanSubscription -> it.priceCurrencyCode
                is OfferSubscription -> it.basePlanSubscription.priceCurrencyCode
                else -> ""
            }
        } ?: ""
    }

    fun getDiscountPrice(id: String): String {
        return subscriptionMap[id]?.let {
            when (it) {
                is OfferSubscription -> it.discountPrice
                else -> ""
            }
        } ?: ""
    }

    fun getStandardJsonPayload(): String {
        val allProductDetails = productDetailMap.values.toList()
        Log.d(EZT_Purchase, "getStandardJsonPayload: allProductDetails:${allProductDetails}")
        return createStandardJsonPayload(allProductDetails)
    }

    /**
     * Return true if user is owning at least one product or available subscription
     */
    fun checkPurchased(): Boolean {
        if (!syncPurchased) {
            queryPurchases()
        }
        return if (!syncPurchased) cachedOwnedProducts.size > 0 else _ownedProducts.value.size > 0
    }


    fun isRemoveAds(): Boolean {
        if (!syncPurchased) {
            queryPurchases()
        }
        return if (!syncPurchased) !Collections.disjoint(
            cachedOwnedProducts,
            setRemoveAdsId
        ) else !Collections.disjoint(_ownedProducts.value, setRemoveAdsId)
    }

    private fun onOwnedProduct(productId: String) {
        logDebug("onOwnedProduct: onOwned: $productId")
        listPurchaseUpdateListener.forEach {
            try {
                it.onOwnedProduct(productId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onProductPurchased(productId: String, purchase: Purchase) {
        listPurchaseUpdateListener.forEach {
            try {
                val productDetails = productDetailMap[productId]
                it.onPurchaseSuccess(
                    com.tanhxpurchase.model.Purchase(
                        productId, productDetails!!.productType, purchase.quantity
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private fun onUserCancelBilling() {
        logDebug("User canceled billing")
        listPurchaseUpdateListener.forEach {
            try {
                it.onUserCancelBilling()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onPurchaseFailure(code: Int, errorMsg: String?) {
        logDebug("Purchase failure: $errorMsg")
        listPurchaseUpdateListener.forEach {
            try {
                it.onPurchaseFailure(code, errorMsg)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}

@JvmInline
private value class BillingResponse(val code: Int) {
    val isOk: Boolean
        get() = code == BillingResponseCode.OK
}
package com.tanhxpurchase

object ConstantsPurchase {
    const val BASE_URL = "https://paywall.eztechglobal.com"

    //REMOTE CONFIG
    const val CONFIG_IAP_KEY = "config_iap"

    //TAG LOG
    const val EZT_Purchase = "EZT_Purchase"
    const val TokenPayWall = "TokenPayWall"
    const val DataTemplate = "DataTemplate"
    const val API = "API"

    //KEY JS - Native
    const val Android = "Android"

    // Callback IAP
    const val CLOSE = "close"
    const val POLICY = "policy"
    const val TERMS = "terms"
    const val RESTORE = "restore"
    const val PAYLOAD_RECEIVED = "payload_received"

    //Callback bts,dialog
    const val UPGRADE = "upgrade"
    const val WATCH_ADS = "watch_ads"

    //Type Tracking
    const val BUY_PAYWALL = 1
    const val CLOSE_PAYWALL = 2
    const val SHOW_PAYWALL = 3
    const val UPGRADE_PAYWALL = 4
    const val WATCHADS_PAYWALL = 5

    //Base Plan riêng cho VPN vì vpn có UI IAP backup trong lib
    const val Base_Plan_Id_1Monthly = "sub-1monthly"
    const val Base_Plan_Id_6Monthly = "sub-6monthly"
    const val Base_Plan_Id_Yearly = "sub-yearly"
    const val Base_Plan_Id_Yearly_Trial = "sub-yearly-free-trial"

    const val Restore = "https://play.google.com/store/account/subscriptions"
}

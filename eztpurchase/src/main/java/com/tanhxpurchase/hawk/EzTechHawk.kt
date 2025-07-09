package com.tanhxpurchase.hawk

import com.orhanobut.hawk.Hawk
import com.tanhxpurchase.ConstantsPurchase.CONFIG_IAP_KEY
import com.tanhxpurchase.model.iap.RemoteProductConfig

object EzTechHawk {

    private const val PRODUCT_FREE_TRIAL = "product_free_trial"
    private const val COUNTRY_CODE = "country_code"
    private const val DARK_MODE = "dark_mode"
    private const val IS_FREE_TRIAL = "is_free_trial"

    private const val PRIVACY_POLICY = "privacy_policy"
    private const val TERMS_OF_SERVICE = "terms_of_service"

    private const val AUTHEN_PAYWALL = "authen_paywall"
    private const val AUTHEN_TRACKING = "authen_tracking"
    private const val ACCESS_TOKEN = "access_token"

    private const val TIMEOUT_PAYWALL = "timeout_paywall"

    fun <T> valueOf(key: String, defaultValue: T): T {
        return Hawk.get(key, defaultValue)
    }

    fun <T> setValue(key: String, value: T) {
        Hawk.put(key, value)
    }

    var privacyPolicy: String
        get() = valueOf(PRIVACY_POLICY, "https://docs.google.com/document/d/1UpbdwirSeXskAE0-gKjHRDTfNaCRpSX_AxYSaFADXi4/edit?tab=t.0#heading=h.31g5u5fze7eh")
        set(value) = setValue(PRIVACY_POLICY, value)

    var termsOfService: String
        get() = valueOf(TERMS_OF_SERVICE, "https://docs.google.com/document/d/1UpbdwirSeXskAE0-gKjHRDTfNaCRpSX_AxYSaFADXi4/edit?tab=t.0#heading=h.31g5u5fze7eh")
        set(value) = setValue(TERMS_OF_SERVICE, value)

    var timeOutPayWall : Long
        get() = valueOf(TIMEOUT_PAYWALL, 5678)
        set(value) = setValue(TIMEOUT_PAYWALL, value)

    var producFreetrial: String
        get() = valueOf(PRODUCT_FREE_TRIAL, "")
        set(value) = setValue(PRODUCT_FREE_TRIAL, value)

    var countryCode: String
        get() = valueOf(COUNTRY_CODE, "en")
        set(value) = setValue(COUNTRY_CODE, value)

    var isDarkMode: Boolean
        get() = valueOf(DARK_MODE, false)
        set(value) = setValue(DARK_MODE, value)

    var isFreeTrial: Boolean
        get() = valueOf(IS_FREE_TRIAL, false)
        set(value) = setValue(IS_FREE_TRIAL, value)

    var authenPayWall : String
        get() = valueOf(AUTHEN_PAYWALL, "")
        set(value) = setValue(AUTHEN_PAYWALL, value)

    var authenTracking : String
        get() = valueOf(AUTHEN_TRACKING, "")
        set(value) = setValue(AUTHEN_TRACKING, value)

    var accessToken : String
        get() = valueOf(ACCESS_TOKEN, "")
        set(value) = setValue(ACCESS_TOKEN, value)

    var configIAP : RemoteProductConfig?
        get() = valueOf(CONFIG_IAP_KEY, null)
        set(value) = setValue(CONFIG_IAP_KEY, value)


}
package com.tanhxpurchase.sharepreference

import android.content.Context
import android.content.SharedPreferences

object EzTechPreferences {

    private const val NAME = "publisher_share_preference"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    private const val PRODUCT_FREE_TRIAL = "product_free_trial"
    private const val COUNTRY_CODE = "country_code"
    private const val DARK_MODE = "dark_mode"
    private const val IS_FREE_TRIAL = "is_free_trial"

    @JvmStatic
    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }


    fun <T> valueOf(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is Boolean -> {
                preferences.getBoolean(key, defaultValue)
            }

            is Int -> {
                preferences.getInt(key, defaultValue)
            }

            is Float -> {
                preferences.getFloat(key, defaultValue)
            }

            is Long -> {
                preferences.getLong(key, defaultValue)
            }

            is String -> {
                preferences.getString(key, defaultValue)
            }

            else -> {
                throw IllegalArgumentException("Generic type is not supported")
                1
            }
        } as T
    }


    fun <T> setValue(key: String, value: T) {
        when (value) {
            is Boolean -> {
                preferences.edit {
                    it.putBoolean(key, value)
                }
            }

            is Int -> {
                preferences.edit {
                    it.putInt(key, value)
                }
            }

            is Float -> {
                preferences.edit {
                    it.putFloat(key, value)
                }
            }

            is Long -> {
                preferences.edit {
                    it.putLong(key, value)
                }
            }

            is String -> {
                preferences.edit {
                    it.putString(key, value)
                }
            }

            else -> {
                throw IllegalArgumentException("$value type is not supported")
            }
        }
    }

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



}
package com.tanhxpurchase.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.tanhxpurchase.hawk.EzTechHawk
import com.tanhxpurchase.worker.WokerMananer.enqueueDeviceRegistration
import java.util.Locale

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    lateinit var binding: T
    abstract fun getDataBinding(): T
    protected open fun isActivityFullscreen() = true
    abstract fun observeViewModel()
    abstract fun initView()
    abstract fun addEvent()

    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemUI()
        updateLanguage()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = getDataBinding()
        setContentView(binding.root)
        initView()
        observeViewModel()
        addEvent()
        enqueueDeviceRegistration(applicationContext)

    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun updateLanguage() {
        val locale = Locale(EzTechHawk.countryCode, "")
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(Locale("en"))
        resources.updateConfiguration(config, resources.displayMetrics)
    }


}
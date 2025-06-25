package com.example.eztpaywall_tanhx

import android.R
import android.content.BroadcastReceiver
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding


abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    lateinit var binding: T


    abstract fun getDataBinding(): T

    abstract fun observeViewModel()
    abstract fun initView()
    abstract fun addEvent()
    protected open fun isActivityFullscreen() = false


    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemUI()
        super.onCreate(savedInstanceState)

        binding = getDataBinding()
        setContentView(binding.root)
        observeViewModel()
        initView()
        addEvent()
        if (isActivityFullscreen()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

    }

    fun checkFullScreen(): Boolean {
        return false
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private var receiver: BroadcastReceiver? = null


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("homeTab", true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun hideStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val decorView: View = window.decorView
        val uiOptions: Int = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.hide(
                WindowInsets.Type.statusBars()
            )
        }
    }


    fun setWhiteStatusBar(isWhite: Boolean = true, hideSystemIcons: Boolean = true) {
        if (isWhite) {
            window.statusBarColor = Color.WHITE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        if (hideSystemIcons) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let { controller ->
                    // Ẩn cả status bar và navigation bar
                    controller.hide(
                        WindowInsets.Type.statusBars() or
                                WindowInsets.Type.navigationBars()
                    )
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                // Thêm các flag để ẩn navigation bar
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        )

                // Đặt màu cho navigation bar thành trong suốt để tránh đè lên view
                window.navigationBarColor = Color.TRANSPARENT
            }

            // Đảm bảo layout có thể vẽ ra phía dưới navigation bar để tránh bị đè
            window.attributes.flags = window.attributes.flags or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
    }


}

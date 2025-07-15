package com.example.eztpaywall_tanhx

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import com.example.eztpaywall_tanhx.PremiumHelper.showBottomSheetPayWall
import com.example.eztpaywall_tanhx.PremiumHelper.showDialogPayWall
import com.example.eztpaywall_tanhx.PremiumHelper.startIAP
import com.example.eztpaywall_tanhx.databinding.ActivityMainBinding
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.model.iap.InfoScreen
import com.tanhxpurchase.util.TemplateDataManager.createTrackingEventFromValue
import com.tanhxpurchase.util.logD
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var infoScreen : InfoScreen = InfoScreen()
    override fun getDataBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun observeViewModel() {

    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        createTrackingEventFromValue("PayWallIAPDefault", "MainActivity", 0)?.let {
            infoScreen.templateId = 10
            infoScreen.paywallConfigId = 10
            infoScreen.storeId = 10
        }
        PurchaseUtils.addInitBillingFinishListener {
            lifecycleScope.launch {
                delay(2000)
                binding.test.text = PurchaseUtils.getPrice("weekly")
            }
        }
    }

    override fun addEvent() {
        binding.test.setOnClickListener {
            logD("TANHXXXX =>>>>> infoScreen:${infoScreen}")
            PurchaseUtils.buy(
                this,
                "weekly",
                infoScreen,
                onPurchaseSuccess = { purchase ->

                },
                onPurchaseFailure = { code, errorMsg ->

                }
            )
        }

        binding.tvIAP.setOnClickListener {
            startIAP(
                this@MainActivity,
                "PayWallIAPDefault",
                this::class.java.simpleName,
                onReceivedError = {})
        }

        binding.tvBTS.setOnClickListener {
            showBottomSheetPayWall(
                this@MainActivity, "DialogHighSpeed1",
                this::class.java.simpleName,
                onFailure = {},
                watchAdsCallBack = {})
        }

        binding.tvDialog.setOnClickListener {
            showDialogPayWall(
                this@MainActivity,
                lifecycleScope,
                "DialogRemove1",
                this::class.java.simpleName
            ) {

            }
        }
    }

}
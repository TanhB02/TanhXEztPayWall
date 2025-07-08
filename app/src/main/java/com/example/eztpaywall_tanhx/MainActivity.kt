package com.example.eztpaywall_tanhx

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.eztpaywall_tanhx.PremiumHelper.showBottomSheetPayWall
import com.example.eztpaywall_tanhx.PremiumHelper.showDialogPayWall
import com.example.eztpaywall_tanhx.PremiumHelper.startIAP
import com.example.eztpaywall_tanhx.databinding.ActivityMainBinding
import com.tanhxpurchase.DialogRemoveDefault
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.util.logD
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>() {


    override fun getDataBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun observeViewModel() {

    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
      PurchaseUtils.addInitBillingFinishListener {
          lifecycleScope.launch {
              delay(2000)
              binding.test.text = PurchaseUtils.getPrice("ardraw_weekly")
          }
      }
    }

    override fun addEvent() {
        binding.test.setOnClickListener {
            PurchaseUtils.buy(
                this,
                "ardraw_weekly",
                onPurchaseSuccess = { purchase ->

                },
                onPurchaseFailure = { code, errorMsg ->

                }
            )
        }

        binding.tvIAP.setOnClickListener {
            startIAP(this@MainActivity, lifecycleScope, onReceivedError = {})
        }

        binding.tvBTS.setOnClickListener {
            showBottomSheetPayWall(
                this@MainActivity, lifecycleScope, "DialogHighSpeedDefault",
                onFailure = {},
                watchAdsCallBack = {})
        }

        binding.tvDialog.setOnClickListener {
            showDialogPayWall(this@MainActivity, lifecycleScope, "DialogRemoveDefault") {

            }
        }
    }

}
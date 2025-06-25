package com.tanhxpurchase.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tanhxpurchase.PurchaseUtils


class PremiumConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        if (isInEditMode) {
            visibility = View.VISIBLE
        } else {
            checkPurchaseStatus()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            checkPurchaseStatus()
        }
    }

    private fun checkPurchaseStatus() {
        PurchaseUtils.setActionPurchase(
            actionSuccess = {
                visibility = View.GONE
            },
            actionFailed = {
                visibility = View.VISIBLE
            }
        )
    }

    fun refreshPurchaseStatus() {
        checkPurchaseStatus()
    }
}
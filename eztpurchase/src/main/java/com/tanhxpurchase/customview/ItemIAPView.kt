package com.tanhxpurchase.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.lib.tanhx_purchase.R
import com.lib.tanhx_purchase.databinding.ItemIapBinding
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_1Monthly
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_6Monthly
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_Yearly
import com.tanhxpurchase.ConstantsPurchase.Base_Plan_Id_Yearly_Trial
import com.tanhxpurchase.PurchaseUtils
import com.tanhxpurchase.hawk.EzTechHawk.isFreeTrial
import com.tanhxpurchase.util.dpToPx
import com.tanhxpurchase.util.toGone
import com.tanhxpurchase.util.toVisible

class ItemIAPView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding: ItemIapBinding = ItemIapBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.ItemIAP, 0, 0
        ).apply {
            try {
                binding.tvFreeTrial.isSelected = true
                setPrice(getString(R.styleable.ItemIAP_price))
                if (getString(R.styleable.ItemIAP_price).toString() == Base_Plan_Id_Yearly) {
                    binding.ivTag.toVisible()
                }
                selectView(getBoolean(R.styleable.ItemIAP_isSelect, false))
                binding.apply {
                    tvJust.text =
                        context.getString(
                            R.string.just_month,
                            (PurchaseUtils.getPriceWithoutCurrency(Base_Plan_Id_Yearly) / 12).toString() + " " + PurchaseUtils.getCurrency(
                                Base_Plan_Id_Yearly
                            )
                        )
                }
            } finally {
                recycle()
            }
        }
    }

    fun selectView(isSelect: Boolean) {
        binding.apply {
            if (isSelect) {
                ivStatus.setImageResource(R.drawable.ic_select_iap)
                tvTitle.setTextColor(context.getColor(R.color.color_20222C))
                tvFreeTrial.setTextColor(context.getColor(R.color.color_20222C))
                tvJust.setTextColor(context.getColor(R.color.color_20222C))
                binding.boxView.toVisible()
                ctRoot.setBackgroundResource(R.drawable.bg_item_iap_select_new)
            } else {
                tvFreeTrial.setTextColor(context.getColor(R.color.color_7B809E))
                tvJust.setTextColor(context.getColor(R.color.color_7B809E))
                ivStatus.setImageResource(R.drawable.ic_no_select_iap)
                ctRoot.setBackgroundResource(R.drawable.bg_item_iap_no_select)
                tvTitle.setTextColor(context.getColor(R.color.color_7B809E))
                binding.boxView.toGone()
            }
        }
    }


    fun setPrice(baseplant: String? = "") {
        binding.apply {
            tvPrice.text = baseplant?.let { PurchaseUtils.getPrice(it) }
            when (baseplant) {
                Base_Plan_Id_1Monthly -> {
                    tvPrice.text = PurchaseUtils.getPrice(Base_Plan_Id_1Monthly)
                    tvTitle.text = context.getString(R.string.one_month)
                }

                Base_Plan_Id_6Monthly -> {
                    tvPrice.text = PurchaseUtils.getPrice(Base_Plan_Id_6Monthly)
                    tvTitle.text = context.getString(R.string.six_months)
                }

                Base_Plan_Id_Yearly -> {
                    tvTitle.text = context.getString(R.string.one_year)
                    if (isFreeTrial) {
                        tvPrice.text = PurchaseUtils.getPrice(Base_Plan_Id_Yearly_Trial)
                        tvJust.toVisible()
                        tvFreeTrial.toVisible()
                        binding.ctRoot.setPadding(
                            binding.ctRoot.paddingLeft,
                            context.dpToPx(18),
                            binding.ctRoot.paddingRight,
                            context.dpToPx(18)
                        )
                    } else {
                        tvPrice.text = PurchaseUtils.getPrice(Base_Plan_Id_Yearly)
                    }
                }
            }
        }
    }

}
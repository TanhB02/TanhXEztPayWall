package com.tanhxpurchase.util.clickeffect

import android.os.SystemClock
import android.view.View

private const val DURATION_CLICK = 500L

class OnSingleClickListener(private val block: (View) -> Unit) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < DURATION_CLICK) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()

        block(view)
    }
}

fun View.setOnSingleClickListener(block: (View) -> Unit) {
    val onClick = OnSingleClickListener {
        block(it)
    }
    setOnClickListener(onClick)
}
package com.tanhxpurchase.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<B: ViewBinding>(
    context: Context,
    dlStyle: Int,
    val bindingFactory: (LayoutInflater) -> B
) : Dialog(context, dlStyle) {
    val binding by lazy {
        bindingFactory(layoutInflater)
    }
    protected abstract fun initViews(binding: B)
    protected abstract fun initActions(binding: B)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        initViews(binding)
        initActions(binding)
    }

    protected fun hideKeyboard(view: View) = runCatching {
        val inputManager: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
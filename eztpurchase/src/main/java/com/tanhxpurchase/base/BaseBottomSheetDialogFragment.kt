package com.tanhxpurchase.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tanhxpurchase.worker.WokerMananer.enqueueDeviceRegistration

open class BaseBottomSheetDialogFragment<T : ViewDataBinding>(@LayoutRes contentLayout: Int) :
    BottomSheetDialogFragment(contentLayout) {
    private var _binding: T? = null
    protected val binding: T
        get() = _binding as T

    protected lateinit var mDialog: Dialog
    lateinit var behavior: BottomSheetBehavior<*>
    lateinit var bottomSheet: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val superView = super.onCreateView(inflater, container, savedInstanceState)!!
        _binding = DataBindingUtil.bind(superView)
        mDialog = requireDialog()
        mDialog.setOnShowListener {
            bottomSheet =
                mDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            behavior = BottomSheetBehavior.from(bottomSheet)
            setupBehavior(behavior)
            onShowed()
        }
        return binding.root
    }

    protected open fun onShowed() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.root.parent.parent as ViewGroup).clipChildren = false
        initViewModel()
        initView()
        addEvent()
        addObservers()
        initData()
        enqueueDeviceRegistration(requireActivity().applicationContext)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            isEnabled = false
            dismiss()
        }
    }

    open fun initViewModel() {}

    open fun initView() {}

    open fun addEvent() {}

    open fun addObservers() {}

    open fun initData() {}


    protected open fun setupBehavior(behavior: BottomSheetBehavior<*>) {

    }

    protected fun post(task: Runnable) {
        binding.root.post(task)
    }

    protected fun postDelay(task: Runnable, delay: Long) {
        binding.root.postDelayed(task, delay)
    }


}

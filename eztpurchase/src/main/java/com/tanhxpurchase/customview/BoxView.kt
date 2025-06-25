package com.tanhxpurchase.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.graphics.toColorInt
import com.lib.tanhx_purchase.R
import com.tanhxpurchase.util.dpToPx

class BoxView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var angle = 0f

    private val cornerRadius = context.dpToPx(24).toFloat()
    private val strokeWidth = context.dpToPx(2).toFloat()

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        paint.style = Paint.Style.FILL

        var animator = ValueAnimator.ofFloat(0f, 360f)
        animator.addUpdateListener {
            angle = it.animatedValue as Float
            invalidate()
        }
        animator.interpolator = LinearInterpolator()
        animator.duration = 4000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.start()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val sideLengthWidth = width.toFloat()
        val sideLengthHeight = height.toFloat()

        val colors = intArrayOf(
            Color.parseColor("#96AAFF"),
            Color.parseColor("#96AAFF"),
            Color.parseColor("#1F49DE"),
            Color.parseColor("#1F49DE"),
            Color.parseColor("#96AAFF"),
            Color.parseColor("#96AAFF"),
            Color.parseColor("#1F49DE"),
            Color.parseColor("#1F49DE"),
            Color.parseColor("#96AAFF"),
            Color.parseColor("#96AAFF"),
            Color.parseColor("#1F49DE"),
            Color.parseColor("#1F49DE")
        )
        val positions = floatArrayOf(
            0f,
            0.09f,
            0.10f,
            0.28f,
            0.36f,
            0.55f,
            0.63f,
            0.72f,
            0.81f,
            0.90f,
            0.91f,
            1f
        )

        val shader = SweepGradient(centerX, centerY, colors, positions)
        val maxtrix = Matrix()
        maxtrix.preRotate(angle, centerX, centerY)
        shader.setLocalMatrix(maxtrix)

        paint.shader = shader

        val rect = RectF(
            centerX - sideLengthWidth / 2,
            centerY - sideLengthHeight / 2,
            centerX + sideLengthWidth / 2,
            centerY + sideLengthHeight / 2
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        val backgroundPaint = Paint()
        backgroundPaint.color = context.getColor(R.color.box_inner_background)

        val innerRect = RectF(
            centerX - sideLengthWidth / 2 + strokeWidth,
            centerY - sideLengthHeight / 2 + strokeWidth,
            centerX + sideLengthWidth / 2 - strokeWidth,
            centerY + sideLengthHeight / 2 - strokeWidth
        )
        canvas.drawRoundRect(innerRect, cornerRadius, cornerRadius, backgroundPaint)
    }
}
package com.management.capstone.textView

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.management.capstone.R

@SuppressLint("AppCompatCustomView")
class CustomTextOutLineView : AppCompatTextView {
    private var stroke = false
    private var strokeWidth = 0.0f
    private var strokeColor = 0

    constructor(context:Context, attrs:AttributeSet, defStyle:Int) : super(context, attrs, defStyle) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs:AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context) : super(context)

    private fun initView(context:Context, attrs: AttributeSet) {
        val type = context.obtainStyledAttributes(attrs, R.styleable.CustomTextOutLineView)
        stroke = type.getBoolean(R.styleable.CustomTextOutLineView_textStroke, false)
        strokeWidth = type.getFloat(R.styleable.CustomTextOutLineView_textStrokeWidth, 0.0f)
        strokeColor = type.getColor(R.styleable.CustomTextOutLineView_textStrokeColor, -0x1)
    }

    override fun onDraw(canvas: Canvas) {
        if(stroke) {
            val states = textColors
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            setTextColor(strokeColor)
            super.onDraw(canvas)

            paint.style = Paint.Style.FILL
            setTextColor(states)
        }
        super.onDraw(canvas)
    }
}
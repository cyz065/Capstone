package com.management.capstone.drawing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.ceil

class DrawView (c : Context, attributeSet: AttributeSet) : View(c, attributeSet){
    private val TOUCH_TOLERANCE = 4f
    //private val BRUSH_SIZE = 20
    private var mX = 0f
    private var mY = 0f
    private lateinit var mPath: Path

    private var mPaint: Paint = Paint()
    //ArrayList to store all the strokes drawn by the user on the Canvas
    private val paths = mutableListOf<Stroke>()
    private var currentColor = 0
    private var strokeWidth = 0
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private lateinit var maxBound:Bound

    init {
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.BLACK
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND

        mPaint.alpha = 0xff
    }

    fun init(height:Int, width:Int) {
        maxBound = Bound()
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)

        currentColor = Color.BLACK
        strokeWidth = 15
    }

    // erase one line
    fun undo() {
        if(paths.size != 0) {
            paths.removeAt(paths.size - 1)
            invalidate()
        }
    }

    // erase all lines
    fun clear() {
        if(paths.size != 0) {
            paths.clear()
            invalidate()
        }
    }

    // return bitmap to store in device
    fun save() : Bitmap? {
        val bound = maxBound.getBound()
        val x = bound.left.toInt()
        val y = bound.top.toInt()
        val width = ceil(bound.width()).toInt()
        val height = ceil(bound.height()).toInt()
        Log.d("저장", "bound: $bound" + "x : $x" + "y : $y" + "width : $width" + "height : $height")
        if(width <= 0)
            return null
        if(y + height > mBitmap.height)
            return null
        val bmp = Bitmap.createBitmap(mBitmap, x, y, width, height)
        val bmp128 = Bitmap.createScaledBitmap(bmp, 128, 128, true)
        return bmp128
    }

    // actual drawing will happen
    override fun onDraw(canvas: Canvas) {
        //save the current state of the canvas before,
        //to draw the background of the canvas
        canvas.save()

        val backgroundColor = Color.WHITE
        mCanvas!!.drawColor(backgroundColor)

        //now, iterate over the list of paths and draw each path on the canvas
        for(fp in paths) {
            mPaint.color = fp.color
            mPaint.strokeWidth = fp.strokeWidth.toFloat()
            mCanvas.drawPath(fp.path, mPaint)
        }
        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitmapPaint)
        canvas.restore()
    }


    //the below methods manages the touch response of the user on the screen

    //the below methods manages the touch response of the user on the screen
    //firstly, we create a new Stroke and add it to the paths list
    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val fp = Stroke(currentColor, strokeWidth, mPath)
        paths.add(fp)

        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    //in this method we check if the move of finger on the
    // screen is greater than the Tolerance we have previously defined,
    //then we call the quadTo() method which actually smooths the turns we create,
    //by calculating the mean position between the previous position and current position
    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    //at the end, we call the lineTo method which simply draws the line until
    //the end position
    private fun touchUp() {
        mPath.lineTo(mX, mY)
        val newPath = mPath
        maxBound.add(Path(newPath))
    }

    //the onTouchEvent() method provides us with the information about the type of motion
    //which has been taken place, and according to that we call our desired methods
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    /*
    private fun runInference():Bitmap? {
        val bound = maxBound.getBound()
        val x = bound.left.toInt()
        val y = bound.top.toInt()
        val width = ceil(bound.width()).toInt()
        val height = ceil(bound.height()).toInt()

        if(width <= 0)
            return null
        if(y + height > mBitmap.height)
            return null
        val bmp = Bitmap.createBitmap(mBitmap, x, y, width, height)
        val bmp64 = Bitmap.createScaledBitmap(bmp, 64, 64, true)

        return bmp64
    }*/
}
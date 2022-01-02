package com.fztkm.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var mDrawPath: CustomPath? = null // CustomPath(インナークラス)を利用するための変数。
    private var mCanvasBitmap: Bitmap? = null //Bitmapインスタンス

    // Paint クラスは、ジオメトリ、テキスト、ビットマップの描画方法に関するスタイルと色の情報を保持する。
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null// Instance of canvas paint view.

    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK

    private var mPaths = ArrayList<CustomPath>()
    /**
     * A variable for canvas which will be initialized later and used.
     *
     *The Canvas class holds the "draw" calls. To draw something, you need 4 basic components:
     * A Bitmap to hold the pixels, a Canvas to host　the draw calls (writing into the bitmap),
     * a drawing primitive (e.g. Rect,
     * Path, text, Bitmap), and a paint (to describe the colors and styles for the
     * drawing)
     *  Canvasクラスは、「draw」の呼び出しを保持しています。
     *  何かを描くには、4つの基本的な構成要素が必要です。
     *  ピクセルを保持するビットマップ、draw呼び出し（ビットマップへの書き込み）を
     *  ホストするCanvas、描画プリミティブ（Rect、Path、text、Bitmapなど）、
     *  ペイント（描画の色やスタイルを記述する）です。
     */
    private var canvas: Canvas? = null

    init {
        setDrawing()
    }

    /**
     * This method initializes the attributes of the
     * ViewForDrawing class.
     */
    private fun setDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND

        mCanvasPaint = Paint(Paint.DITHER_FLAG)// Paint flag that enables dithering when blitting.

        mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    /**
     * This method is called when a stroke is drawn on the canvas
     * as a part of the painting.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        /**
         * Draw the specified bitmap, with its top/left corner at (x,y), using the specified paint,
         * transformed by the current matrix.
         *
         *If the bitmap and canvas have different densities, this function will take care of
         * automatically scaling the bitmap to draw at the same density as the canvas.
         *
         * 指定されたビットマップの左上/右下隅を(x,y)とし、指定されたペイントで、現在の行列で変換して描画します。
         * ビットマップとキャンバスの密度が異なる場合、この関数は、キャンバスと同じ密度で描画するためにビットマップ
         * を自動的に拡大縮小します。
         *
         * @param bitmap The bitmap to be drawn
         * @param left The position of the left side of the bitmap being drawn
         * @param top The position of the top side of the bitmap being drawn
         * @param paint The paint used to draw the bitmap (may be null)
         */
        canvas.drawBitmap(mCanvasBitmap!!,  0f, 0f, mCanvasPaint!!)

        //過去に書いた線（Path）を描画
        for(p in mPaths){
            mDrawPaint!!.strokeWidth = p.brushThickness
            mDrawPaint!!.color = p.color
            canvas.drawPath(p, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!) //Pathを画面(canvas)に描画
        }
    }

    /**
     * This method acts as an event listener when a touch
     * event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            //触った時
            MotionEvent.ACTION_DOWN -> {
                //CustomPathの設定
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.color = color

                mDrawPath!!.reset()// Clear any lines and curves from the path, making it empty.
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)// Set the beginning of the next contour to the point (x,y).
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)// Add a line from the last point to the specified point (x,y).
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!) //Add when to stroke is drawn to canvas and added in the path arraylist

                mDrawPath = CustomPath(color, mBrushSize)
            }
            else ->return false
        }

        /**
         * invalidate()
         * ビュー全体を無効化する。ビューが表示されている場合、将来のある時点で
         * "onDraw(android.graphics.Canvas)" が呼び出されます。
         * これは、UIスレッドから呼ばれなければならない。
         * UI以外のスレッドから呼び出すには、"postInvalidate()" を呼び出します。
         */
        invalidate()
        return true
    }


    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path(){

    }
}
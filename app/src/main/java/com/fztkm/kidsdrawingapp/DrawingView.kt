package com.fztkm.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

    private var mDrawPath: CustomPath? = null // CustomPath(インナークラス)を利用するための変数。
    private var mCanvasBitmap: Bitmap? = null //Bitmapインスタンス

    // Paint クラスは、ジオメトリ、テキスト、ビットマップの描画方法に関するスタイルと色の情報を保持する。
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null// Instance of canvas paint view.

    private var mBrushSize: Float = 0.toFloat()
    var color = Color.BLACK
        private set

    //Pathを保存するリスト。Path：図形のデータ。指でなぞった形と座標をデータとしてもつ。
    //mPaths（今まで描いた図形データ）と、現在描いてるPath（mDrawPath）をonDraw()にてcanvasに描画する
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

    /**
     * On size changed
     * スクリーンが立ち上がった時（起動時や、画面の向き変えたとき）に実行される
     *
     * @param w 新しい画面幅
     * @param h　新しい画面高さ
     * @param oldw
     * @param oldh
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        println("ON SIZE CHANGED")
        //Bitmapの高さと幅、各ビットは色の情報をARGB_8888でもつ;透明度、RGBをそれぞれ8ビットのデータで保持
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
            //触った瞬間　ACTION_DOWN
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
            //なぞっている時 ACTION_MOVE
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX, touchY)// Add a line from the last point to the specified point (x,y).
                    }
                }
            }
            //指を離した時　ACTION_UP
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

    fun setSizeForBrush(newSize: Float){
        //画面サイズ(screen dimension)に合わせたBrushSizeにしたい
        /**返り値　Float
         * @param unit : Int 単位
         * @param value : Float 値　unit何個分
         * @param metrics : DisplayMetrics //Current display metrics to use in the conversion --
         * supplies display density and scaling information.
         */
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    /**
     * Set brush color
     *
     * @param newColor: String e.g."#aaff02" カラーコードの文字列
     * をInt変換してcolorにセット
     */
    fun setBrushColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    fun setBrushColor(newColor: Int){
        color = newColor
        mDrawPaint!!.color = color
    }

    /**
     * Clear paths
     * mPathsをクリア　画面に書かれた絵を全消去する
     * invalidate()でonDraw()を呼び出す
     */
    fun clearPaths(){
        mPaths.clear()
        invalidate()
    }

    fun popPathsList(){
        if(mPaths.isNotEmpty()){
            mPaths.apply { removeAt(mPaths.size-1) }
        }
        invalidate()
    }


    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path(){

    }
}
package com.fztkm.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeForBrush(15.toFloat())

        val linearLayoutColors: LinearLayout = findViewById(R.id.ll_colors)
        mImageButtonCurrentPaint = linearLayoutColors[1] as ImageButton
        //ImageButtonのsrcにdrawableをセットする
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )

        val ibBrushSize: ImageButton = findViewById(R.id.ib_brush_size)
        ibBrushSize.setOnClickListener {
            showChoseBrushSizeDialog()
        }

        val ibClearButton: ImageButton = findViewById(R.id.ib_clear)
        ibClearButton.setOnClickListener {
            drawingView!!.clearPaths()
        }
    }

    /**
     * Show chose brush size dialog
     * ブラシサイズ選択のダイアログを表示
     * 大 中 小　30dp 20dp 10dp
     */
    private fun showChoseBrushSizeDialog(){
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")

        //小さいサイズ
        val ibSmallBrush: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        ibSmallBrush.setOnClickListener{
            drawingView!!.setSizeForBrush(10f)
            brushDialog.dismiss()
        }
        //中サイズ
        val ibMediumBrush: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        ibMediumBrush.setOnClickListener {
            drawingView!!.setSizeForBrush(20f)
            brushDialog.dismiss()
        }
        //大きいサイズ
        val ibLargeBrush: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        ibLargeBrush.setOnClickListener {
            drawingView!!.setSizeForBrush(30f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun onPalletClick(view: View){
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView!!.setBrushColor(colorTag)

            //選択状態
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_selected)
            )
            //非選択状態
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            mImageButtonCurrentPaint = view
        }
    }
}
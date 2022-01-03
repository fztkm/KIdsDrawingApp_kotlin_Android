package com.fztkm.kidsdrawingapp

import android.app.Dialog
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeForBrush(15.toFloat())
        val ibBrushSize: ImageButton = findViewById(R.id.ib_brush_size)
        ibBrushSize.setOnClickListener {
            showChoseBrushSizeDialog()
        }
    }

    /**
     * Show chose brush size dialog
     * ブラシサイズ選択のダイアログを表示
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
}
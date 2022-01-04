package com.fztkm.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeForBrush(10.toFloat())

        //ブラシ色＿黒＿ImageButtonを選択状態にする
        val linearLayoutColors: LinearLayout = findViewById(R.id.ll_colors)
        mImageButtonCurrentPaint = linearLayoutColors[1] as ImageButton
        //ImageButtonのsrcに選択状態のdrawableをセットする
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        //ブラシサイズ変更のクリックリスナー
        val ibBrushSize: ImageButton = findViewById(R.id.ib_brush_size)
        ibBrushSize.setOnClickListener {
            showChoseBrushSizeDialog()
        }

        //描画消去のクリックリスナー
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

    /**
     * On palette click
     * 色ボタンを押すと、そのボタンを選択状態にして、ブラシの色をその色にする
     * @param view
     * xmlファイルでid/ll_colors内のImageButtonのonClickに渡した
     */
    fun onPaletteClick(view: View){
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView!!.setBrushColor(colorTag)

            //選択状態
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_selected)
            )
            //非選択状態
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )

            mImageButtonCurrentPaint = view
        }
    }

    /**
     * On random color click
     * 画面下部の真ん中のパレットImageButton(id/ib_color_picker)を押した時に実行
     * カラーピッカーダイアログを表示（AmbilWarnaDialogライブラリ）
     * デフォルトカラーは現在のブラシの色
     * @param view
     */
    fun onRandomColorClick(view: View){
        when(mImageButtonCurrentPaint!!.id){
            R.id.ib_color_picker -> Unit
            else -> {
                mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.palette_normal)
                )
            }
        }

        val colorPickerDialog = AmbilWarnaDialog(
            this, drawingView!!.color, object: AmbilWarnaDialog.OnAmbilWarnaListener{
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    Unit
                }
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawingView!!.setBrushColor(color)
                }
            }
        )
        colorPickerDialog.show()
    }

    fun onBackImageButtonClick(view: View){
        drawingView!!.popPathsList()
    }
}
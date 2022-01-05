package com.fztkm.kidsdrawingapp

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value

                if(isGranted){
                    Toast.makeText(this@MainActivity,
                        "Permission granted for external storage",
                        Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@MainActivity,
                        "Permission denied for external storage",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
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

    /**
     * 操作を一つ戻す
     * ImageButton(id/ib_back)のonClickに渡した
     * popPathsList() -> mPaths:ArrayList<CustomPath> の最後の要素を消して再描画する
     * @param view
     */
    fun onBackImageButtonClick(view: View){
        drawingView!!.popPathsList()
    }

    private fun showRationaleDialog(
        title: String,
        message: String
    ){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){
                dialog,_ -> dialog.dismiss()
            }
        builder.create().show()
    }
}
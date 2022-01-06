package com.fztkm.kidsdrawingapp

import android.Manifest
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
import com.fztkm.kidsdrawingapp.databinding.ActivityMainBinding
import com.fztkm.kidsdrawingapp.databinding.DialogBrushSizeBinding
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    //Manifestの<uses-permission/>に欲しい権限を設定することが必要
    //権限要求のための変数、権限を要求するタイミングで、requestPermission.launch()する
    //launchにArrayで要求する権限を渡す。forEachでひとつづつ処理を行う。
    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                //要求通った
                if(isGranted){
                    //認められたのは外部ストレージ読み取り権限か
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this@MainActivity,
                            "Permission granted for external storage",
                            Toast.LENGTH_LONG).show()
                    }
                //拒否
                }else{
                    //拒否されたのは外部ストレージ読み取り権限か
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this@MainActivity,
                            "Permission denied for external storage",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drawingView = findViewById(R.id.drawingView)
        drawingView?.setSizeForBrush(10.toFloat())

        //ブラシ色＿黒＿ImageButtonを選択状態にする
        val linearLayoutColors: LinearLayout = binding.llColors
        mImageButtonCurrentPaint = linearLayoutColors[1] as ImageButton
        //ImageButtonのsrcに選択状態のdrawableをセットする
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        //ブラシサイズ変更のクリックリスナー
        val ibBrushSize: ImageButton = binding.ibBrushSize
        ibBrushSize.setOnClickListener {
            showChoseBrushSizeDialog()
        }

        //描画消去のクリックリスナー
        val ibClearButton: ImageButton = binding.ibClear
        ibClearButton.setOnClickListener {
            drawingView!!.clearPaths()
        }

        val ibGallery: ImageButton = binding.ibPhoto
        ibGallery.setOnClickListener {
           requestStoragePermission()
        }
    }

    /**
     * ストレージへのアクセス権限を要求する
     *　既に拒否されている場合には、権限が必要だが拒否されていることをダイアログで表示
     */
    private fun requestStoragePermission(){
        //すでに権限要求が拒否されている場合 shouldShowRequestPermissionRationaleはtrueを返す
        // その場合, showRationaleDialogを表示
        if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRationaleDialog("KidsDrawingApp requires external storage access",
                "External storage cannot be used because storage access is denied")
        }else{
            //権限要求をする
            requestPermission.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }
    }

    /**
     *ダイアログを表示する
     * 用途：権限が必要であることを表示する
     * @param title
     * @param message
     */
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

    /**
     * Show chose brush size dialog
     * ブラシサイズ選択のダイアログを表示
     * 大 中 小　30dp 20dp 10dp
     */
    private fun showChoseBrushSizeDialog(){
        val brushDialog = Dialog(this)
        val brushSizeDgBinding: DialogBrushSizeBinding =
            DialogBrushSizeBinding.inflate(layoutInflater)
        val dgView = brushSizeDgBinding.root
        brushDialog.setContentView(dgView)
        brushDialog.setTitle("Brush size: ")

        //小さいサイズ
        val ibSmallBrush: ImageButton = brushSizeDgBinding.ibSmallBrush
        ibSmallBrush.setOnClickListener{
            drawingView!!.setSizeForBrush(10f)
            brushDialog.dismiss()
        }
        //中サイズ
        val ibMediumBrush: ImageButton = brushSizeDgBinding.ibMediumBrush
        ibMediumBrush.setOnClickListener {
            drawingView!!.setSizeForBrush(20f)
            brushDialog.dismiss()
        }
        //大きいサイズ
        val ibLargeBrush: ImageButton = brushSizeDgBinding.ibLargeBrush
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
}

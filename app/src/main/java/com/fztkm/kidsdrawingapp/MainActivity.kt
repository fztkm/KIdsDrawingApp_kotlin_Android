package com.fztkm.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.fztkm.kidsdrawingapp.databinding.ActivityMainBinding
import com.fztkm.kidsdrawingapp.databinding.DialogBrushSizeBinding
import com.fztkm.kidsdrawingapp.databinding.DialogShowProgressBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    //画像を保存している間に表示するダイアログ
    private var dialogShowProgress: Dialog? = null

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

                        //外部ストレージを開き写真を選択、背景にセット
                        val pickIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
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

    //フォトギャラリーを開いて写真を選択し、背景にセットするランチャー
    //openGalleryLauncher.launch(pickIntent: Intent)で実行される
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                val ivBackground = binding.ivBackground
                ivBackground.setImageURI(result.data?.data)
            }
        }

    //ViewBinding
    private lateinit var binding: ActivityMainBinding

    /**
     * On create
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drawingView = binding.drawingView
        drawingView?.setSizeForBrush(10.toFloat())

        //ブラシ色＿黒＿ImageButtonを選択状態にする
        val linearLayoutColors: LinearLayout = binding.llColors
        mImageButtonCurrentPaint = linearLayoutColors[1] as ImageButton
        //ImageButtonのsrcに選択状態のdrawableをセットする
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        //ブラシサイズ変更
        val ibBrushSize: ImageButton = binding.ibBrushSize
        ibBrushSize.setOnClickListener {
            showChoseBrushSizeDialog()
        }

        //描画消去
        val ibClearButton: ImageButton = binding.ibClear
        ibClearButton.setOnClickListener {
            drawingView!!.clearPaths()
        }

        //背景画像を選択
        val ibGallery: ImageButton = binding.ibPhoto
        ibGallery.setOnClickListener {
           requestStoragePermission()
        }

        //イラストを保存
        val ibSave: ImageButton = binding.ibSava
        ibSave.setOnClickListener {
            if(isReadStorageAllowed()){
                lifecycleScope.launch {
                    showProgressDialog()
                    val flDrawingView: FrameLayout = binding.flDrawingViewContainer
                    val myBitmap: Bitmap = getBitmapFromView(flDrawingView)
                    saveBitmapFile(myBitmap)
                }
            }
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
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
     * Is read storage allowed
     *
     * @return
     */
    private fun isReadStorageAllowed(): Boolean{
        val result = ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
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

    /**
     * Get bitmap from view
     *
     * @param view
     * @return viewをBitmapに写したもの
     */
    private fun getBitmapFromView(view: View): Bitmap{
        //viewと同じサイズのBitmapを定義する
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //canvasをreturnedBitmapに紐づける
        val canvas = Canvas(returnedBitmap)
        val gbDrawable = view.background
        if(gbDrawable != null){
            //viewがbackgroundを持つならbackgroundをcanvasに写す
            gbDrawable.draw(canvas)
        }else{
            //backgroundない場合、canvasを白く塗る
            canvas.drawColor(Color.WHITE)
        }
        //viewをcanvasに写す
        view.draw(canvas)

        return returnedBitmap
    }

    /**
     * Save bitmap file
     * Android/data/com.fztkm.kidsdrawingapp/cache/に保存
     * キャッシュデータにする
     * @param bitmap
     * @return
     */
    private suspend fun saveBitmapFile(bitmap: Bitmap?): String{
        var result = ""
        withContext(Dispatchers.IO){
            if(bitmap != null){
                try {
                    val bytes = ByteArrayOutputStream()
                    //bitmapをcompress(圧縮)して、bytesに出力
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    //externalCacheDirはここでは、Android/data/com.fztkm.kidsdrawingapp/cache
                    val f = File(externalCacheDir?.absoluteFile.toString()
                    + File.separator + "KidsDrawingApp_" + System.currentTimeMillis()/1000 + ".png")

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath

                    runOnUiThread {
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity,
                                "File saved successfully :$result",
                            Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@MainActivity,
                            "Something went wrong while saving the file",
                            Toast.LENGTH_LONG).show()
                        }
                        dismissProgressDialog()
                    }
                }catch (e: Exception){
                    result = ""
                    e.stackTrace
                }
            }
        }
        return result
    }

    /**
     * Show progress dialog
     *  Progressダイアログを表示する
     *  (画像を保存し始めたときに呼び出す)
     */
    private fun showProgressDialog(){
        val progressBinding: DialogShowProgressBinding =
            DialogShowProgressBinding.inflate(layoutInflater)
        val view = progressBinding.root

        dialogShowProgress = Dialog(this)
        dialogShowProgress?.setContentView(view)
        dialogShowProgress?.show()
    }

    /**
     * Dismiss progress dialog
     * Progressダイアログの表示を消す
     * （画像の保存が終了したとき呼び出す）
     */
    private fun dismissProgressDialog(){
        //dialogShowProgressがnullでないならdismissして、null代入
        //表示されている時以外はnullにしておく
        dialogShowProgress?.let {
            it.dismiss()
            dialogShowProgress = null
        }
    }
}

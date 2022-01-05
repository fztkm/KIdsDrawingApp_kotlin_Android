package com.fztkm.kidsdrawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import yuku.ambilwarna.AmbilWarnaDialog

class MainActivity : AppCompatActivity() {

    //カメラと位置情報の権限リクエスト、lounch()で呼び出す
    private val cameraAndLocationResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted){
                    //詳細な位置情報の権限が許可
                    if(permissionName == Manifest.permission.ACCESS_FINE_LOCATION){
                        Toast.makeText(this,
                            "Permission granted for location",
                            Toast.LENGTH_LONG).show()
                    }else{
                        //カメラ権限許可
                        Toast.makeText(this,
                            "Permission granted for Camera",
                            Toast.LENGTH_LONG).show()
                    }
                }else{
                    //詳細な位置情報の権限が拒否された
                    if (permissionName == Manifest.permission.ACCESS_FINE_LOCATION){
                        Toast.makeText(this,
                        "Permission denied for location",
                        Toast.LENGTH_LONG).show()
                    }else{
                        //カメラの権限が拒否された
                        Toast.makeText(this,
                        "Permission denied for Camera",
                        Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btnCameraPermission: Button = findViewById(R.id.btn_camera_permission)
        btnCameraPermission.setOnClickListener{
            //shouldShowRequestPermissionRationale()は
            // 以前ユーザーがリクエストを許可しなかった場合trueを返す、
            // 「今後表示しない」を選択していた場合はfalseを返します。
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                showRationaleDialog("Permision Demo requires camera access",
                "Camera cannot be used because Camera access is denied")
            }else{
                //カメラと位置情報の権限の要求をする
                cameraAndLocationResultLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                       )
                )
            }
        }
    }

    /**
     * Show rationale dialog for display why the app needs permission
     * Only shown if the user has denied the permission request previously
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
            .setPositiveButton("CANCEL"){
                dialog,_ -> dialog.dismiss()
            }
        builder.create().show()
    }

}
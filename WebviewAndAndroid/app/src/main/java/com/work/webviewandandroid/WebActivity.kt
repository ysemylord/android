package com.work.webviewandandroid

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.intellij.lang.annotations.JdkConstants
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File


class WebActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST: Int = 1
    }

    private var pictureFile: File? = null
    private val PICTURE_REQUEST_CODE: Int = 0
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null

    val permissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //去除状态栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_web)
        requestPermission()

        web_view.settings.run {
            //允许javascript
            javaScriptEnabled = true

            // 设置允许JS弹窗
            javaScriptCanOpenWindowsAutomatically = true

            //是网页中的viewport标签效
            useWideViewPort = true

            //超屏自动缩放
            loadWithOverviewMode = true

        }

        web_view.run {
            webChromeClient = MyChromeClient()
            webViewClient = MyWWeViewClient()
            //loadUrl("file:///android_asset/web.html")
            //loadUrl("http://39.98.225.131/808gps/login.html")
            loadUrl("http://39.98.225.131")
        }

        invokeByJs()

    }

    private fun invokeByJs() {
        //将InvokeByJS类对象映射到js的invokeByJS对象
        web_view.addJavascriptInterface(InvokeByJS(), "invokeByJS")
    }

    private fun requestPermission() {
        EasyPermissions.requestPermissions(
            PermissionRequest.Builder(
                this,
                PERMISSION_REQUEST,
                *permissions
            )
                .setRationale("请求权限")
                .setPositiveButtonText("确定")
                .setNegativeButtonText("取消")
                .build()
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    @AfterPermissionGranted(PERMISSION_REQUEST)
    private fun getPermission() {
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            //Toast.makeText(this,"获取到权限",Toast.LENGTH_LONG).show()
        } else {
            requestPermission()
        }
    }


    inner class InvokeByJS {
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        fun showMsg(msg: String) {
            Toast.makeText(this@WebActivity, msg, Toast.LENGTH_LONG).show()
        }

        @JavascriptInterface
        fun choosePhoto() {
            toPhoto()
        }
    }

    /**
     * 获取相册
     */
    private fun toPhoto() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICTURE_REQUEST_CODE)
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        when (requestCode) {
            PICTURE_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                if (intent == null || intent.data == null) {
                    Toast.makeText( this, "为获取到数据", Toast.LENGTH_SHORT).show()
                    return
                }

                var uri = intent.data
                if (uri == null) {
                    Toast.makeText(this, "为获取到数据", Toast.LENGTH_SHORT).show()
                    return
                }


                // val images =
                //   arrayOf(MediaStore.Images.Media.DATA) //将获取到的

                //val cursor: Cursor = managedQuery(uri, images, null, null, null)
                //val index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                //cursor.moveToFirst()
                //val imgUri = cursor.getString(index)
                val result = Array(1) {
                    uri!!
                }

                mUploadMessage?.onReceiveValue(result)
            }
        }
    }

    inner class MyChromeClient : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            //通过ValueCallback将文件信息传递给WevView
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (mUploadMessage != null) {
                mUploadMessage?.onReceiveValue(null);
            }
            mUploadMessage = filePathCallback
            toPhoto()
            return true
        }
    }

    inner class MyWWeViewClient:WebViewClient(){
        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url)
            return true
        }
    }

}
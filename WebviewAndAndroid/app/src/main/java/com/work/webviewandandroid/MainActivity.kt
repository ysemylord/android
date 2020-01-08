package com.work.webviewandandroid

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLDecoder


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val settings = web_view.settings

        settings.javaScriptEnabled = true

        // 设置允许JS弹窗
        settings.javaScriptCanOpenWindowsAutomatically = true

        web_view.loadUrl("file:///android_asset/my.html")

        invokeByJs()

    }



    /**
     * 使用WebView.loadUrl调用js
     */
    fun androidInvokeJsByLoadUrl(view: View) {
        web_view.loadUrl("javascript:callByAndroidLoadUrl()")
    }

    /**
     * 使用WebView.evaluateJavascript调用js
     */
    fun androidInvokeJsByEvaluateJavascript(view: View) {
        web_view.evaluateJavascript("javascript:callByAndroidEvaluateJavascript()") {
            Log.i("MainActivity", it)
        }
    }


    private fun invokeByJs() {
        //将InvokeByJS类对象映射到js的invokeByJS对象
        web_view.addJavascriptInterface(InvokeByJS(), "invokeByJS")


        //拦截url
        web_view.webViewClient = object : WebViewClient() {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val url = request?.url.toString()
                if (url.startsWith("js://tosat")) {
                    //得到的url是通过url编码的，所以这里需要url解码
                    val msg =URLDecoder.decode(url).split("msg=")[1]
                    Toast.makeText(this@MainActivity, (msg), Toast.LENGTH_LONG).show()
                    return true
                }

                return false
            }
        }

        web_view.webChromeClient = object :WebChromeClient(){
            override fun onJsPrompt(
                view: WebView?,
                url: String,
                message: String,
                defaultValue: String?,
                result: JsPromptResult
            ): Boolean {
                if(message.startsWith("js://tosat")){
                    val msg =message.split("msg=")[1]
                    Toast.makeText(this@MainActivity, (msg), Toast.LENGTH_LONG).show()
                    //result.confirm必须要赋值，表示prompt的返回值
                    result.confirm("js调用了Android的方法成功啦")
                    return true
                }
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }
        }

    }



    inner class InvokeByJS {
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        fun showMsg(msg: String) {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
        }
    }

}

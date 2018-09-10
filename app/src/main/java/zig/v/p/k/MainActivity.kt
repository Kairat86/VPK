package zig.v.p.k

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }

    private var ad: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        if (!isNetworkConnected()) {
            setContentView(R.layout.no_internet)
            return
        }
        setContentView(R.layout.activity_main)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(JS(), "Android")
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.webViewClient = TextPertClient()
        webView.loadUrl("https://m.vk.com/audio")
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = VISIBLE
            }
        }
        adView.loadAd(AdRequest.Builder().build())
        ad = InterstitialAd(this)
        ad?.adUnitId = getString(R.string.inter_id)
        ad?.loadAd(AdRequest.Builder().build())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ad?.show()
    }

    fun refresh(v: View) {
        init()
    }

    private fun isNetworkConnected() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo != null

    inner class TextPertClient : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String?) {
            injectScriptFile(view, "script.js")
            Log.i(TAG, "page finished loading")
        }

        private fun injectScriptFile(view: WebView, scriptFile: String) {
            val input: InputStream
            try {
                input = assets.open(scriptFile)
                val buffer = ByteArray(input.available())
                input.read(buffer)
                input.close()

                // String-ify the script byte-array using BASE64 encoding !!!
                val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
                view.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type = 'text/javascript';" +
                        // Tell the browser to BASE64-decode the string into your script !!!
                        "script.innerHTML = window.atob('" + encoded + "');" +
                        "parent.appendChild(script)" +
                        "})()")
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    class JS {

        @JavascriptInterface
        fun log(msg: String) {
            Log.i(this::class.java.simpleName, msg)
        }
    }
}

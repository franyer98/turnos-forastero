package com.franyer.turnosforastero

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

/** La pantalla es una página (assets/index.html) que arma los turnos por habitación. */
class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Puente(), "Android")
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) { revisarNovedades() }
        }
        web.loadUrl("file:///android_asset/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                web.evaluateJavascript("window.atras ? window.atras() : false") { r ->
                    if (r != "true") finish()
                }
            }
        })

        ActualizacionWorker.programar(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun js(codigo: String) = runOnUiThread { web.evaluateJavascript(codigo, null) }

    /** Al abrir: baja turnos nuevos y busca versión nueva de la app. */
    private fun revisarNovedades() {
        lifecycleScope.launch {
            val nuevos = Datos.bajar(this@MainActivity)
            js("window.alActualizar && window.alActualizar(${if (nuevos) "true" else "false"})")
            val nueva = Actualizador.buscar(this@MainActivity) ?: Actualizador.descargada(this@MainActivity)
            if (nueva != null) js("window.hayVersion && window.hayVersion(${JSONObject.quote(nueva.versionName)})")
        }
    }

    inner class Puente {
        @JavascriptInterface fun datos(): String = Datos.leer(this@MainActivity)
        @JavascriptInterface fun version(): String = Actualizador.nombreVersion(this@MainActivity)
        @JavascriptInterface fun refrescar() = runOnUiThread { revisarNovedades() }
        @JavascriptInterface fun instalar() = runOnUiThread {
            Actualizador.descargada(this@MainActivity)?.let { Actualizador.instalar(this@MainActivity, it) }
        }
    }
}

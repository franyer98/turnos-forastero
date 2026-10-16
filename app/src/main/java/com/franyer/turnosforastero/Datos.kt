package com.franyer.turnosforastero

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Los turnos viven en el repositorio (datos/meses.json). Cada mes Claude cruza
 * las fotos y actualiza ese archivo; la app lo baja sola al abrir y cada pocas horas.
 * Sin internet usa la última copia guardada (o la que trae el APK).
 */
object Datos {
    private const val URL_DATOS =
        "https://raw.githubusercontent.com/franyer98/turnos-forastero/main/datos/meses.json"

    private fun archivo(ctx: Context) = File(ctx.filesDir, "meses.json")

    fun leer(ctx: Context): String {
        val f = archivo(ctx)
        val guardado = if (f.exists()) runCatching { f.readText() }.getOrNull() else null
        val incluido = runCatching { ctx.assets.open("meses.json").bufferedReader().use { it.readText() } }.getOrNull()
        // Usa el más reciente entre lo guardado y lo que trae el APK
        return listOfNotNull(guardado, incluido)
            .maxByOrNull { runCatching { JSONObject(it).optString("actualizado") }.getOrDefault("") }
            ?: """{"meses":{}}"""
    }

    /** Baja los datos. Devuelve true si llegaron datos nuevos. */
    suspend fun bajar(ctx: Context): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val con = (URL("$URL_DATOS?t=${System.currentTimeMillis()}").openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                setRequestProperty("Cache-Control", "no-cache")
            }
            if (con.responseCode !in 200..299) return@runCatching false
            val texto = con.inputStream.bufferedReader().use { it.readText() }
            JSONObject(texto) // valida que sea JSON
            val anterior = leer(ctx)
            if (texto == anterior) return@runCatching false
            archivo(ctx).writeText(texto)
            true
        }.getOrDefault(false)
    }
}

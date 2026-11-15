package com.apleq.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        com.apleq.app.data.util.LanguagePreferences.apply(this)
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val initialized = try { FirebaseApp.initializeApp(this) } catch (_: Throwable) { null }
                if (initialized == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:498651324948:android:1cacae6d0919a7a78dbd01")
                        .setApiKey("AIzaSyAfdb5NIJtcratWCAebPD41hvWGTe5FSNA")
                        .setProjectId("apleq-76e0a")
                        .setStorageBucket("apleq-76e0a.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
        } catch (t: Throwable) {
            Log.w("MyApplication", "FirebaseApp init handled gracefully: ${t.message}")
        }
    }

    /** Cargador de imágenes compartido: más caché y sin revalidar con el servidor. */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            // Las URLs de Firebase Storage no traen cabeceras de caché útiles: usar siempre la copia guardada.
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(false)
            .build()
    }
}

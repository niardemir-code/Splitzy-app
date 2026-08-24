package com.apleq.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
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
}

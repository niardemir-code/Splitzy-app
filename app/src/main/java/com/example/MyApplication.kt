package com.example

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
                        .setApplicationId("1:345750792662:android:7123300d38bec1521f1c77")
                        .setApiKey("AIzaSyD5S5DI2FSp-LHWmdEhId-5zGETcrqsm78")
                        .setProjectId("splitzy-8ceb1")
                        .setStorageBucket("splitzy-8ceb1.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
        } catch (t: Throwable) {
            Log.w("MyApplication", "FirebaseApp init handled gracefully: ${t.message}")
        }
    }
}

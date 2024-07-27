package com.example.myapplication.ui.theme.view

import android.app.Application
import com.google.firebase.FirebaseApp
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class MyApplication : Application() {
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Create custom cache directory
        val cacheDir = File(cacheDir, "image_cache").apply { mkdirs() }

        imageLoader = ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizePercent(0.10) // Use 2% of the app's storage space for disk caching
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(Cache(cacheDir, 10L * 1024 * 1024)) // 10MB cache size
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}

package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.remote.CoinGeckoService
import com.example.data.remote.CryptoCompareService
import com.example.data.repository.CryptoRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            
            // Build temporary retrofit services for syncing
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = OkHttpClient.Builder().build()

            val cryptoCompareService = Retrofit.Builder()
                .baseUrl("https://min-api.cryptocompare.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CryptoCompareService::class.java)

            val coinGeckoService = Retrofit.Builder()
                .baseUrl("https://api.coingecko.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CoinGeckoService::class.java)

            val repository = CryptoRepositoryImpl(
                context = applicationContext,
                newsDao = db.newsDao(),
                eventDao = db.eventDao(),
                watchlistDao = db.watchlistDao(),
                cryptoCompareService = cryptoCompareService,
                coinGeckoService = coinGeckoService
            )

            // Trigger news refresh
            repository.refreshNews()
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

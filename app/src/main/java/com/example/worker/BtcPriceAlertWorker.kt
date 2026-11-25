package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.remote.CoinGeckoService
import com.example.notification.NotificationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class BtcPriceAlertWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("crypto_prefs", Context.MODE_PRIVATE)
        val alertsEnabled = prefs.getBoolean("btc_alerts_enabled", true)
        if (!alertsEnabled) {
            return Result.success()
        }

        val thresholdPercent = prefs.getFloat("btc_alert_threshold", 2.0f).toDouble()
        val lastPrice = prefs.getFloat("last_btc_price", 0.0f).toDouble()

        return try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val coinGeckoService = Retrofit.Builder()
                .baseUrl("https://api.coingecko.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CoinGeckoService::class.java)

            val prices = coinGeckoService.getSimplePrices()
            val currentPrice = prices["bitcoin"]?.get("usd") ?: 0.0

            if (currentPrice > 0.0) {
                if (lastPrice > 0.0) {
                    val changePct = ((currentPrice - lastPrice) / lastPrice) * 100.0
                    if (abs(changePct) >= thresholdPercent) {
                        NotificationHelper.showBtcPriceAlert(
                            context = applicationContext,
                            currentPrice = currentPrice,
                            previousPrice = lastPrice,
                            changePct = changePct
                        )
                        // Update baseline price after triggering alert
                        prefs.edit().putFloat("last_btc_price", currentPrice.toFloat()).apply()
                    }
                } else {
                    // Seed initial baseline price
                    prefs.edit().putFloat("last_btc_price", currentPrice.toFloat()).apply()
                }
            }

            Result.success()
        } catch (t: Throwable) {
            if (t is kotlinx.coroutines.CancellationException) throw t
            t.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "BtcPriceAlertWork"

        fun schedule(context: Context) {
            try {
                val workRequest = PeriodicWorkRequestBuilder<BtcPriceAlertWorker>(15, TimeUnit.MINUTES)
                    .build()
                WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        fun cancel(context: Context) {
            try {
                WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}

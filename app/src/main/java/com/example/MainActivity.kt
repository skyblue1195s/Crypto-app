package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.remote.CoinGeckoService
import com.example.data.remote.CryptoCompareService
import com.example.data.repository.CryptoRepositoryImpl
import com.example.presentation.CryptoViewModel
import com.example.presentation.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Build DB DAOs
    val db = AppDatabase.getDatabase(applicationContext)

    // 2. Build Moshi and Retrofit Services
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

    // 3. Build Repository
    val repository = CryptoRepositoryImpl(
        context = applicationContext,
        newsDao = db.newsDao(),
        eventDao = db.eventDao(),
        watchlistDao = db.watchlistDao(),
        cryptoCompareService = cryptoCompareService,
        coinGeckoService = coinGeckoService
    )

    // 4. Build ViewModel using custom factory
    val viewModel: CryptoViewModel by viewModels {
        CryptoViewModel.provideFactory(application, repository)
    }

    setContent {
      MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}

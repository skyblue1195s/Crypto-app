package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CryptoGold,
    secondary = CryptoSurfaceVariant,
    background = CryptoDarkBg,
    surface = CryptoSurface,
    onPrimary = CryptoDarkBg,
    onBackground = CryptoTextPrimary,
    onSurface = CryptoTextPrimary,
    onSecondary = CryptoTextPrimary
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark Theme as default for trading app style
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our tailored branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

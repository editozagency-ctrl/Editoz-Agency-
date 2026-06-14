package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OrangePrimary,
    onPrimary = TextWhite,
    secondary = OrangeSecondary,
    onSecondary = TextWhite,
    tertiary = OrangeAccent,
    background = RichBlack,
    surface = PremiumCharcoal,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = PremiumGray,
    onSurfaceVariant = TextLightGray,
    outline = BorderGray
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OrangePrimary,
    onPrimary = TextWhite,
    secondary = OrangeSecondary,
    onSecondary = TextWhite,
    tertiary = OrangeAccent,
    background = Color(0xFFFAFAFC),
    surface = Color(0xFFFFFFFF),
    onBackground = RichBlack,
    onSurface = RichBlack,
    surfaceVariant = Color(0xFFF3F3F5),
    onSurfaceVariant = Color(0xFF555566),
    outline = Color(0xFFE2E2E8)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disable to force our premium brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

package com.expenss.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    background = BgDark,
    surface = Bg2Dark,
    onBackground = TextDark,
    onSurface = TextDark,
    secondary = Green,
)

private val LightColors = lightColorScheme(
    primary = Accent,
    background = BgLight,
    surface = Bg2Light,
    onBackground = TextLight,
    onSurface = TextLight,
    secondary = Green,
)

@Composable
fun ExpenssTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
){
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
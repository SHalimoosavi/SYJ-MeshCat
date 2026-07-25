package com.sayanjalinexus.meshchat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MeshAmberDark,
    secondary = MeshVioletDark,
    tertiary = MeshVioletDark,
    background = MeshSurfaceDark,
    surface = MeshSurfaceDark,
    surfaceVariant = MeshSurfaceVariantDark,
    onPrimary = MeshNearBlack,
    onSecondary = MeshNearBlack,
    onBackground = MeshOnSurfaceDark,
    onSurface = MeshOnSurfaceDark,
    onSurfaceVariant = MeshOnSurfaceVariantDark,
    error = MeshErrorDark,
)

private val LightColorScheme = lightColorScheme(
    primary = MeshAmberLight,
    secondary = MeshVioletLight,
    tertiary = MeshVioletLight,
    background = MeshSurfaceLight,
    surface = MeshSurfaceLight,
    surfaceVariant = MeshSurfaceVariantLight,
    onPrimary = MeshSurfaceLight,
    onSecondary = MeshSurfaceLight,
    onBackground = MeshOnSurfaceLight,
    onSurface = MeshOnSurfaceLight,
    onSurfaceVariant = MeshOnSurfaceVariantLight,
    error = MeshErrorLight,
)

/**
 * App-wide Compose theme. Defaults to the system dark/light setting;
 * respects Material You dynamic color on Android 12+ when available, but
 * always falls back to the fixed SYJ brand palette (amber/violet/near-black)
 * on older devices or when dynamic color is disabled.
 */
@Composable
fun MeshChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MeshTypography,
        content = content,
    )
}

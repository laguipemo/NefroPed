package com.laguipemo.nefroped.designsystem.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Modern SystemBarsController using WindowInsetsControllerCompat.
 * Replaces the need for Accompanist SystemUIController.
 */
@Composable
fun SystemBarsController(
    useStatusDarkIcons: Boolean,
    useNavigationDarkIcons: Boolean
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(useStatusDarkIcons, useNavigationDarkIcons) {
            val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            insetsController.isAppearanceLightStatusBars = useStatusDarkIcons
            insetsController.isAppearanceLightNavigationBars = useNavigationDarkIcons
            
            onDispose {}
        }
    }
}

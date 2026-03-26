package com.laguipemo.nefroped.designsystem.components

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

/**
 * Modern SystemBarsController using WindowInsetsControllerCompat.
 * Handles transparency and icon appearance for both Activity and Dialog/BottomSheet windows.
 */
@Composable
fun SystemBarsController(
    useStatusDarkIcons: Boolean,
    useNavigationDarkIcons: Boolean,
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(useStatusDarkIcons, useNavigationDarkIcons, statusBarColor, navigationBarColor) {
            val window = findWindow(view) ?: return@DisposableEffect onDispose {}
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // 1. Permitir que el contenido fluya bajo las barras
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // 2. Configurar apariencia de iconos
            insetsController.isAppearanceLightStatusBars = useStatusDarkIcons
            insetsController.isAppearanceLightNavigationBars = useNavigationDarkIcons
            
            // 3. Forzar fondos transparentes y habilitar dibujo de barras
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
            
            // 4. CRÍTICO: Desactivar el contraste forzado del sistema (el "scrim" u opacidad)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
                window.isStatusBarContrastEnforced = false
            }
            
            onDispose {}
        }
    }
}

private fun findWindow(view: View): Window? {
    var parent = view.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) {
            return parent.window
        }
        parent = parent.parent
    }
    return (view.context as? Activity)?.window
}

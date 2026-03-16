package com.laguipemo.nefroped.designsystem.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SystemBarsController(
    useStatusDarkIcons: Boolean,
    useNavigationDarkIcons: Boolean
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = useStatusDarkIcons
            insetsController.isAppearanceLightNavigationBars = useNavigationDarkIcons
        }
    }
}

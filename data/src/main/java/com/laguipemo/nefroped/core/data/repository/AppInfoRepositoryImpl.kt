package com.laguipemo.nefroped.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.laguipemo.nefroped.core.domain.repository.app.AppInfoRepository

class AppInfoRepositoryImpl(
    private val context: Context
) : AppInfoRepository {
    override fun getAppVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

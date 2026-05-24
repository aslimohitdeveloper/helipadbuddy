package com.mskdevelopers.helipadbuddy.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * Location permission checks and request handling.
 * Plan Phase 6.1: Request location permissions, handle results, show rationale dialogs.
 */
object PermissionsManager {

    private const val PERMISSION_PREFS = "helipad_permission_prefs"
    private const val KEY_FINE_LOCATION_REQUESTED = "fine_location_requested"

    fun hasFineLocation(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasCoarseLocation(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasBackgroundLocation(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true

    fun hasAllLocationPermissions(context: Context): Boolean =
        hasFineLocation(context) && hasCoarseLocation(context) && hasBackgroundLocation(context)

    /**
     * Returns true if we should show a rationale before requesting permission
     * (user denied before or should see explanation).
     */
    fun shouldShowLocationRationale(activity: Activity): Boolean {
        val needed = locationPermissionsNeeded(activity)
        if (needed.isEmpty()) return false
        return needed.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }

    /** Foreground location only — used for in-app Allow prompts. */
    fun foregroundLocationPermissionsNeeded(context: Context): Array<String> {
        val list = mutableListOf<String>()
        if (!hasFineLocation(context)) list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!hasCoarseLocation(context)) list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        return list.toTypedArray()
    }

    fun locationPermissionsNeeded(context: Context): Array<String> {
        val list = foregroundLocationPermissionsNeeded(context).toMutableList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocation(context)) {
            list.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return list.toTypedArray()
    }

    fun appSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
        }

    fun locationSourceSettingsIntent(): Intent =
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    fun markLocationPermissionRequested(context: Context) {
        context.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FINE_LOCATION_REQUESTED, true)
            .apply()
    }

    private fun hasRequestedLocationPermission(context: Context): Boolean =
        context.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_FINE_LOCATION_REQUESTED, false)

    /**
     * True when the user previously denied and the system will not show the request dialog again.
     * Avoids treating the first launch (never asked) as permanently denied.
     */
    fun isPermissionPermanentlyDenied(activity: Activity, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (activity.shouldShowRequestPermissionRationale(permission)) {
            return false
        }
        return hasRequestedLocationPermission(activity)
    }
}

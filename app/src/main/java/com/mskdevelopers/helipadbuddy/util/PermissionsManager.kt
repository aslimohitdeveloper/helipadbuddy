package com.mskdevelopers.helipadbuddy.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Location permission checks and request handling.
 * Plan Phase 6.1: Request location permissions, handle results, show rationale dialogs.
 */
object PermissionsManager {

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

    fun locationPermissionsNeeded(context: Context): Array<String> {
        val list = mutableListOf<String>()
        if (!hasFineLocation(context)) list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!hasCoarseLocation(context)) list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundLocation(context)) {
            list.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return list.toTypedArray()
    }

    /**
     * Returns true if permission is denied and permanently denied
     * (user checked "Don't ask again" or system won't show dialog again).
     */
    fun isPermissionPermanentlyDenied(activity: Activity, permission: String): Boolean {
        val isDenied = ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        val shouldShowRationale = activity.shouldShowRequestPermissionRationale(permission)
        return isDenied && !shouldShowRationale
    }
}

package de.uni_hannover.errorcorrection

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class Permissions {

    /**
     * This function checks the needed permissions for this application
     * @param mainActivity context for permission checking
     */
    internal fun checkPermissions(mainActivity: MainActivity) {
        val permissions = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.RECORD_AUDIO
        )

        if (!permissions.all {
                ActivityCompat.checkSelfPermission(
                    mainActivity,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(mainActivity, permissions.toTypedArray(), 1)
        }
    }
}
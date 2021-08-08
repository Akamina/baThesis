package de.test.errorcorrection

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permissions {

    /**
     * This function checks the needed permissions for this application
     * @param mainActivity context for permission checking
     */
    internal fun checkPermissions(mainActivity: MainActivity) {
        //TODO change external to internal storage
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(Manifest.permission.CAMERA), 1)
        }

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(Manifest.permission.READ_CALENDAR), 1)
        }

        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, arrayOf(Manifest.permission.WRITE_CALENDAR), 1)
        }
    }
}
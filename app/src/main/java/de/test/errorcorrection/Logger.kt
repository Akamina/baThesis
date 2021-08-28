package de.test.errorcorrection

import android.os.Environment
import android.util.Log
import java.io.File

class Logger {
    /**
     * This function writes a String into a log file.
     * Format: YYYY-MM-DDTH:M:S:MS-sender-Content
     * YYYY: Year
     * MM: Month
     * DD: Day
     * H: Hour
     * M: Minute
     * S: Second
     * MS: Millisecond
     * Sender: User/System
     * Content: User input / System actions/output
     * @param text user or system input to write into the log file
     * @param sender sender of text parameter (User: 1, System: 0)
     */
    internal fun writeLog(text: String, sender: Int, mainActivity: MainActivity) {
        //TODO use another path
        //val sd_main = File(mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "test")
        val sdMain =
            File(mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "test")
        var success = true
        if (!sdMain.exists())
            success = sdMain.mkdir()
        if (success) {
            //directory exists or already created
            val dest = File(sdMain, "voice_log.txt")
            println(dest.absolutePath)
            //Get current time and date for the log
            val currentTimeAndDate = org.threeten.bp.LocalDateTime.now()
            var sndr = "USR"
            if (sender == 0) {
                sndr = "SYS"
            }
            try {
                dest.appendText("$currentTimeAndDate-$sndr:$text\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            //directory creation is not successful
            Log.e("DEBUG", "Erstellen vom Ordner fehlgeschlagen")
        }
        //readLog(mainActivity)
    }

    /**
     * This function reads the log file and prints it on the console for debugging
     */
    internal fun readLog(mainActivity: MainActivity) {
        val sdMain = File(mainActivity.getExternalFilesDir(Environment.DIRECTORY_DCIM), "test")
        var success = true
        if (!sdMain.exists())
            success = sdMain.mkdir()

        if (success) {
            val sd = File(sdMain, "voice_log.txt")
            //println("Path in read: " + sd.path)
            val lineList = mutableListOf<String>()
            sd.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach { println("DEBUG: $it") }

        }
    }
}
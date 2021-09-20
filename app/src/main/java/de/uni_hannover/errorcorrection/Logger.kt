package de.uni_hannover.errorcorrection

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
        val sdMain =
            File(mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "log")
        var success = true
        //create directory if it does not exist
        if (!sdMain.exists()) success = sdMain.mkdir()
        //check for valid directory
        if (success) {
            //directory exists or already created
            val dest = File(sdMain, "voice_log.txt")
            //Get current time and date for the log
            val currentTimeAndDate = org.threeten.bp.LocalDateTime.now()
            //Set correct sender of message
            var sndr = "USR"
            if (sender == 0) {
                sndr = "SYS"
            }
            //write into log file
            try {
                dest.appendText("$currentTimeAndDate-$sndr:$text\n")
            } catch (e: Exception) {
                e.printStackTrace()
                dest.appendText("Error during write")
            }
        } else {
            //directory creation is not successful
            Log.e("DEBUG", "Erstellen vom Ordner fehlgeschlagen")
        }
    }

    /**
     * This function reads the log file and prints it on the console for debugging
     */
    internal fun readLog(mainActivity: MainActivity) {
        val sdMain = File(mainActivity.getExternalFilesDir(Environment.DIRECTORY_DCIM), "log")
        var success = true
        //create directory if it does not exist
        if (!sdMain.exists()) success = sdMain.mkdir()
        //check for valid directory
        if (success) {
            val sd = File(sdMain, "voice_log.txt")
            val lineList = mutableListOf<String>()
            sd.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach { println("DEBUG: $it") }

        }
    }
}
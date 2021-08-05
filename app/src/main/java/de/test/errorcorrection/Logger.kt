package de.test.errorcorrection

import android.os.Environment
import android.util.Log
import java.io.File

object Logger {
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
    internal fun writeLog(text: String, sender: Int) {
        //TODO change to internal storage
        val sd_main = File(Environment.getExternalStorageDirectory(), "test")
        var success = true
        if (!sd_main.exists())
            success = sd_main.mkdir()

        if (success) {
            //directory exists or already created
            //val sd = File("voice_log.txt")
            //Log.e("DEBUG","Ordner erstellt/existiert")
            val dest = File(sd_main, "voice_log.txt")
            //println("Pfad: " + dest.path)
            //Get current time and date for the log
            val currentTimeAndDate = org.threeten.bp.LocalDateTime.now()
            var sndr = "USR"
            if (sender == 0) {
                sndr = "SYS"
            }

            try {
                dest.appendText("$currentTimeAndDate-$sndr:$text\n")
            } catch (e: Exception) {
                //TODO handle exception
            }
        } else {
            //directory creation is not successful
            Log.e("DEBUG", "Erstellen vom Odner fehlgeschlagen")
        }
    }

    /**
     * This function reads the log file and prints it on the console for debugging
     */
    internal fun readLog() {
        //TODO change to internal storage
        val sd_main = File(Environment.getExternalStorageDirectory(), "test")
        var success = true
        if (!sd_main.exists())
            success = sd_main.mkdir()

        if (success) {
            val sd = File(sd_main, "voice_log.txt")
            //println("Path in read: " + sd.path)
            val lineList = mutableListOf<String>()
            sd.useLines { lines -> lines.forEach { lineList.add(it) } }
            lineList.forEach {println("DEBUG: " + it)}

        }
    }
}
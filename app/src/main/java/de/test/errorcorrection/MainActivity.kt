package de.test.errorcorrection

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalTime
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_STT = 1
    }

    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.GERMANY
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        // Checking permissions
        //TODO change external to internal storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1);
        }
        setContentView(R.layout.activity_main);

        //get reference for tts button
        val btn_tts = findViewById<Button>(R.id.playButton)

        val textbox = findViewById<EditText>(R.id.et_text_input)

        btn_tts.setOnClickListener{
            val text = textbox.text.toString().trim()
            if (text.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                    readLog()
                } else {
                    textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                    readLog()

                }
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
            }
        }

        val btn_stt = findViewById<Button>(R.id.recordButton)
        /**
         * Getting user input and starting decision finding process
         * */

        btn_stt.setOnClickListener {
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            sttIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

            try {
                startActivityForResult(sttIntent, REQUEST_CODE_STT)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val textbox = findViewById<EditText>(R.id.et_text_input)
        when (requestCode) {
            REQUEST_CODE_STT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        writeLog(recognizedText, 1)
                        handleInput(recognizedText)
                    }
                }
            }
        }
    }

    override fun onPause() {
        textToSpeechEngine.stop()
        super.onPause()
    }

    override fun onDestroy() {
        textToSpeechEngine.shutdown()
        super.onDestroy()
    }

    /**
     * This function writes data into a log file
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
     * Input values:
     * text: Message to add to the log
     * sender: Sender of this message (User: 1, System: 0)
     */
    private fun writeLog(text: String, sender: Int) {
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

    private fun readLog() {
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

    /**
     *This function determines the command that will be performed e.g. create or edit etc.
     **/
    private fun getCommand(text: String): String {
        //Create appointment
        //Create reminder
        //Create list
        //Edit appointment
        //Edit reminder
        //Edit list
        //Delete appointment
        //Delete reminder
        //Delete list
        //Read appointment
        //Read reminder
        //Read list
        //TODO: add lists of words and synonyms to compare here
        if (text.contains("erstelle")) {
            //erinnere mich soll auch diesen Fall auslösen
            return "create"
        }
        if (text.contains("lösche")) {
            return "delete"
        }
        if (text.contains("bearbeite")) {
            return "edit"
        }
        if (text.contains("lies")) {
            return "read"
        }
        return "error"
    }

    /**
     * This function determines the target of a command. Appointment or reminder or list
     **/
    //TODO: compare index of reminder and appointment so create an appointment with the name reminder is still an appointment and not a reminder
    private fun getTarget(text: String): String {
        if (text.contains("Liste")) {
            return "list"
        }
        if (text.contains("Erinnerung")) {
            return "reminder"
        }
        if (text.contains("Termin")) {
            return "appointment"
        }
        return "error"
    }

    /**
     * This function handles user input and calls functions to perform the users intend
     **/

    private fun handleInput(text: String) {
        val command = getCommand(text)
        val target = getTarget(text)

        when (command) {
            "create" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            "edit" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            "delete" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            "read" -> when (target) {
                "appointment" -> println("$command $target")
                "reminder" -> println("$command $target")
                "list" -> println("$command $target")
                else -> { println("Ziel nicht verstanden")}
            }
            else -> { println("Kommando nicht verstanden")}
        }
    }
}
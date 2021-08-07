package de.test.errorcorrection

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Semaphore

open class MainActivity : AppCompatActivity() {
    companion object {
        internal const val REQUEST_CODE_STT = 1
        internal const val REQUEST_CODE_STT_ANSWER = 2
        internal const val REQUEST_CODE_STT_NAME = 3
        internal const val REQUEST_CODE_STT_DATE = 4
        internal const val REQUEST_CODE_STT_TIME = 5
        internal const val REQUEST_CODE_STT_LOCATION = 6
    }

    internal val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this@MainActivity
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechEngine.language = Locale.GERMANY
            }
        }
    }

    lateinit internal var job: Job

    internal lateinit var logger: Logger
    internal lateinit var permissions: Permissions
    internal lateinit var handler: IntendHandler
    internal lateinit var stt: STT
    internal lateinit var tts : TTS
    internal lateinit var appntmnt: Appointment
    internal val sem = Semaphore(1)

    /**
     * This functions initializes the application
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)
        logger = Logger()
        permissions = Permissions()
        handler = IntendHandler()
        stt = STT()
        tts = TTS()
        appntmnt = Appointment()

        // Checking permissions
        permissions.checkPermissions(this)
        setContentView(R.layout.activity_main);

        //get reference for tts button
        val btn_tts = findViewById<Button>(R.id.playButton)

        val textbox = findViewById<EditText>(R.id.et_text_input)

        //askUser("", this, REQUEST_CODE_STT_NAME)

        btn_tts.setOnClickListener{

            val text = textbox.text.toString().trim()
            if (text.isNotEmpty()) {
                askUser(text, this, REQUEST_CODE_STT_NAME)
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
            }
        }

        val btn_stt = findViewById<Button>(R.id.recordButton)

        /**
         * Getting user input and starting decision finding process
         * */

        btn_stt.setOnClickListener {
            var text = stt.getUserInputMain(this)
            //IntendHandler.handleInput(text, this)
        }

    }

    /**
     * This function displays the users input from STT in a text box, calls writeLog for it
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val textbox = findViewById<EditText>(R.id.et_text_input)
        //TODO handle system user dialog in here
        when (requestCode) {
            REQUEST_CODE_STT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        //IntendHandler.handleInput(recognizedText, this)
                        handler.handleInput(recognizedText, this)
                    }
                }
            }
            REQUEST_CODE_STT_ANSWER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)

                    }
                }

            }
            REQUEST_CODE_STT_NAME -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText)
                        appntmnt.setName(recognizedText)
                        askUser("An welchem Datum ist der Termin?", this, REQUEST_CODE_STT_DATE)

                    }
                }
            }
            REQUEST_CODE_STT_DATE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText)
                        appntmnt.setDate(recognizedText)
                        askUser("Um welche Uhrzeit ist der Termin?", this, REQUEST_CODE_STT_TIME)

                    }
                }
            }
            REQUEST_CODE_STT_TIME -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText)
                        appntmnt.setTime(recognizedText)
                        askUser("Wo findet der Termin statt?", this, REQUEST_CODE_STT_LOCATION)

                    }
                }
            }
            REQUEST_CODE_STT_LOCATION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText)
                        appntmnt.setLocation(recognizedText)
                        //askUser("An welchem Datum ist der Termin?", this, REQUEST_CODE_STT_DATE)

                        //TODO call function to create event in calendar here
                    }
                }
            }

        }
    }

    /**
     * This function stops the TTS-Engine
     */
    override fun onPause() {
        textToSpeechEngine.stop()
        super.onPause()
    }

    /**
     * This function shuts the TTS-Engine down
     */
    override fun onDestroy() {
        textToSpeechEngine.shutdown()

        super.onDestroy()
    }


    /**
     * TODO
     *
     * @param text
     */
    internal fun askUser(text: String, mainActivity: MainActivity, requestCode: Int) {
        val textbox = findViewById<EditText>(R.id.et_text_input)
        textbox.setText(text)
        val logger = Logger()


        val job = GlobalScope.launch {
            println("waiting in thread: ${Thread.currentThread().name}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainActivity.textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                logger.writeLog(text, 0)
            } else {
                mainActivity.textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                logger.writeLog(text, 0)

            }
            delay((text.length * 80).toLong())
        }
        runBlocking {
            job.join()
        }
        mainActivity.stt.getUserInput(mainActivity, requestCode)

    }




    /**
     * This function starts the dialogue to create a list
     **/
    private fun createList() {

    }

    /**
     * This function starts the dialogue to create a reminder
     **/
    private fun createReminder() {

    }


    /*
    /**
     * This function determines the command that will be performed e.g. create or edit etc.
     * @param text user input to get command from
     * @return type of command (create, edit, delete, read)
     */
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
        if (text.contains("erstell")) {
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
     * @param text user input to get target from
     * @return target type (appointment, reminder, list)
     */
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
     * @param text user input to handle
     * @param mainActivity activity for context
     */

    internal fun handleInput(text: String, mainActivity: MainActivity) {
        val command = getCommand(text)
        val target = getTarget(text)
        val tmp = Appointment()

        when (command) {
            "create" -> when (target) {
                "appointment" -> createAppointment(mainActivity)//println("$command $target")
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

    /**
     * This function starts the dialogue to create an appointment
     * @param mainActivity context for functions and SST and TTS
     */
    internal fun createAppointment(mainActivity: MainActivity) {
        println("Erstelle Termin")
        var name = askName(mainActivity)
        var date = askDate()
        var time = askTime()
        var location = askLocation()
        /*
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

        try {
            mainActivity.startActivityForResult(sttIntent, REQUEST_CODE_STT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
        }
        */

    }

    /**
     * TODO
     * This function asks the user for the name of this appointment
     * @param mainActivity
     * @return name of the appointment
     */
    private fun askName(mainActivity: MainActivity): String {
        val s = "Wie lautet der Name des Termins?"
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeechEngine.speak(s, TextToSpeech.QUEUE_FLUSH, null, "tts1")

            Logger.writeLog(s, 0)
        } else {
            textToSpeechEngine.speak(s, TextToSpeech.QUEUE_FLUSH, null)

            Logger.writeLog(s, 0)

        }*/
        askUser(s, mainActivity)
        Thread.sleep(3000)
        getUserInput()


        return "foo"
    }

    private fun askDate(): LocalDate? {
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return LocalDate.parse("21.02.2021", formatter)//Date.parse("21.02.2021")
    }

    private fun askTime(): LocalTime? {
        return LocalTime.parse("12:00", DateTimeFormatter.ISO_TIME)
    }

    private fun askLocation(): String {
        return "foo"
    }

    */


}
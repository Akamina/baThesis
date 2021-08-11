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
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.*
import java.util.*

open class MainActivity : AppCompatActivity() {
    companion object {
        internal const val REQUEST_CODE_STT = 1
        //internal const val REQUEST_CODE_STT_ANSWER = 2
        internal const val REQUEST_CODE_STT_NAME = 3
        internal const val REQUEST_CODE_STT_DATE = 4
        internal const val REQUEST_CODE_STT_TIME = 5
        internal const val REQUEST_CODE_STT_LOCATION = 6
        internal const val REQUEST_CODE_STT_DELETE_APPOINTMENT = 7
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT = 8
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD = 9
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW = 10
        internal const val REQUEST_CODE_STT_NOTIFY = 2
    }

    //Initialize TTS-Engine
    internal val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this@MainActivity
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechEngine.language = Locale.GERMANY
            }
        }
    }

    internal lateinit var logger: Logger
    internal lateinit var permissions: Permissions
    internal lateinit var handler: IntendHandler
    internal lateinit var stt: STT
    internal lateinit var tts : TTS
    internal lateinit var appntmnt: Appointment

    /**
     * This functions initializes the application
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        setContentView(R.layout.activity_main)

        //Initialize other classes
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

        //testing purposes:
        appntmnt.setName("test")
        appntmnt.setDate("am 9.08 2021")
        appntmnt.setLocation("zuhause")
        appntmnt.setTime("19:34 Uhr")

        appntmnt.createAppointment(this)

        btn_tts.setOnClickListener{

            val text = textbox.text.toString().trim()
            /*
            if (text.isNotEmpty()) {
                askUser(text, this, REQUEST_CODE_STT_NAME)
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
            }
            */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                logger.writeLog(text, 0)
            } else {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                logger.writeLog(text, 0)

            }
        }

        val btn_stt = findViewById<Button>(R.id.recordButton)

        /**
         * Getting user input and starting decision finding process
         * */

        btn_stt.setOnClickListener {
            stt.getUserInputMain(this)
            //IntendHandler.handleInput(text, this)
        }

    }

    /**
     * This function displays the users input from STT in a text box, calls writeLog for it
     * Also handles the dialogue to create appointments, reminders and lists
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val textbox = findViewById<EditText>(R.id.et_text_input)
        //TODO handle system user dialog in here
        when (requestCode) {
            //Start of dialogue
            REQUEST_CODE_STT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        handler.handleInput(recognizedText, this)
                    }
                }
            }

            /*
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

             */
            //Get name of appointment and ask for date
            REQUEST_CODE_STT_NAME -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //Debug
                        appntmnt.setName(recognizedText)
                        askUser("An welchem Datum ist der Termin?", this, REQUEST_CODE_STT_DATE)

                    }
                }
            }
            //Get the date of the appointment and ask for time
            REQUEST_CODE_STT_DATE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //debug
                        appntmnt.setDate(recognizedText)
                        askUser("Um welche Uhrzeit ist der Termin?", this, REQUEST_CODE_STT_TIME)

                    }
                }
            }
            //Get the time for the appointment and ask for location
            //TODO add check for valid date, if invalid ask again
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
            //TODO add check for valid time, if invalid ask again
            //Get the location for the appointment and start creating it
            REQUEST_CODE_STT_LOCATION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //debug
                        appntmnt.setLocation(recognizedText)
                        appntmnt.createAppointment(this)
                    }
                }
            }
            //Get the name of the appointment and call delete function
            REQUEST_CODE_STT_DELETE_APPOINTMENT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //debug
                        appntmnt.deleteAppointment(this, recognizedText)
                    }
                }
            }
            //Get the name of the appointment and call edit function
            REQUEST_CODE_STT_EDIT_APPOINTMENT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //debug
                        appntmnt.startEdit(recognizedText, this)
                    }
                }
            }
            //Get the field to edit and continue edit
            REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //debug
                        appntmnt.continueEdit(recognizedText, this)
                    }
                }
            }
            //Get changes and perform these on the event
            REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        logger.writeLog(recognizedText, 1)
                        println(recognizedText) //debug
                        //TODO add fucntion to edit event
                        appntmnt.editAppointment(recognizedText, this)
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
        //val textbox = findViewById<EditText>(R.id.et_text_input)
        //textbox.setText(text)
        //val logger = Logger()

        val job = GlobalScope.launch {
            println("waiting in thread: ${Thread.currentThread().name}") //Debug
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainActivity.textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                logger.writeLog(text, 0)
            } else {
                mainActivity.textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                logger.writeLog(text, 0)

            }
            delay((text.length * 80).toLong())
        }
        //Wait until job is done, speaking in this case
        runBlocking {
            job.join()
        }

        if (requestCode != REQUEST_CODE_STT_NOTIFY)mainActivity.stt.getUserInput(mainActivity, requestCode)

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


}
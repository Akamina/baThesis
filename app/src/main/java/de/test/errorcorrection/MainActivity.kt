package de.test.errorcorrection

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*


open class MainActivity : AppCompatActivity() {
    companion object {
        internal const val REQUEST_CODE_STT = 1
        internal const val REQUEST_CODE_STT_NOTIFY = 2
        internal const val REQUEST_CODE_STT_NAME = 3
        internal const val REQUEST_CODE_STT_DATE = 4
        internal const val REQUEST_CODE_STT_TIME = 5
        internal const val REQUEST_CODE_STT_LOCATION = 6
        internal const val REQUEST_CODE_STT_DELETE_APPOINTMENT = 7
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT = 8
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD = 9
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW = 10
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK = 11
        internal const val REQUEST_CODE_STT_EDIT_APPOINTMENT_READ = 12
        internal const val REQUEST_CODE_STT_READ_APPOINTMENT_NO_NAME = 13
        internal const val REQUEST_CODE_STT_REMINDER_NAME = 14
        internal const val REQUEST_CODE_STT_REMINDER_DATE = 15
        internal const val REQUEST_CODE_STT_REMINDER_TIME = 16
        internal const val REQUEST_CODE_STT_REMINDER_DELETE = 17
        internal const val REQUEST_CODE_STT_REMINDER_READ = 18
        internal const val REQUEST_CODE_STT_REMINDER_EDIT = 19
        internal const val REQUEST_CODE_STT_REMINDER_EDIT_FIELD = 20
        internal const val REQUEST_CODE_STT_REMINDER_EDIT_READ = 21
        internal const val REQUEST_CODE_STT_REMINDER_EDIT_NEW = 22
        internal const val REQUEST_CODE_STT_REMINDER_EDIT_ASK = 23

    }

    //Initialize TTS-Engine
    internal val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(
            this@MainActivity
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
    internal lateinit var tts: TTS
    internal lateinit var appntmnt: Appointment
    internal lateinit var rmdr: Reminder

    internal lateinit var dialogueStart: ActivityResultLauncher<Intent>
    internal lateinit var dialogueNotify: ActivityResultLauncher<Intent>
    internal lateinit var appointmentName: ActivityResultLauncher<Intent>
    internal lateinit var appointmentDate: ActivityResultLauncher<Intent>
    internal lateinit var appointmentTime: ActivityResultLauncher<Intent>
    internal lateinit var appointmentLocation: ActivityResultLauncher<Intent>
    internal lateinit var appointmentDelete: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEdit: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditField: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditNew: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditAsk: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditRead: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditNoName: ActivityResultLauncher<Intent>
    internal lateinit var reminderName: ActivityResultLauncher<Intent>
    internal lateinit var reminderDate: ActivityResultLauncher<Intent>
    internal lateinit var reminderTime: ActivityResultLauncher<Intent>
    internal lateinit var reminderDelete: ActivityResultLauncher<Intent>
    internal lateinit var reminderRead: ActivityResultLauncher<Intent>
    internal lateinit var reminderEdit: ActivityResultLauncher<Intent>
    internal lateinit var reminderEditField: ActivityResultLauncher<Intent>
    internal lateinit var reminderEditRead: ActivityResultLauncher<Intent>
    internal lateinit var reminderEditNew: ActivityResultLauncher<Intent>
    internal lateinit var reminderEditAsk: ActivityResultLauncher<Intent>

    private var countTime: Int = 0
    private var countDate: Int = 0
    private var countEdit: Int = 0


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
        rmdr = Reminder()

        // Checking permissions
        permissions.checkPermissions(this)
        setContentView(R.layout.activity_main);

        //get reference for tts button
        val btn_tts = findViewById<Button>(R.id.playButton)
        val textbox = findViewById<EditText>(R.id.et_text_input)

        /*
        //testing purposes:
        appntmnt.setName("test")
        appntmnt.setDate("am 9.08 2021")
        appntmnt.setLocation("zuhause")
        appntmnt.setTime("19:34 Uhr")
        println(appntmnt.parseLocalDate("am 9.7 2021"))
        println(appntmnt.parseLocalTime("19:34 Uhr"))

        //appntmnt.createAppointment(this)

         */

        btn_tts.setOnClickListener {

            val text = textbox.text.toString().trim()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                logger.writeLog(text, 0)
            } else {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                logger.writeLog(text, 0)

            }
        }

        //Init ActivityResultLauncher
        dialogueStart =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { rslt: ActivityResult ->
                val uri = rslt.data
                //Sanity check
                println(uri)
                if (uri == null || rslt.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }

                val result =
                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                result?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    handler.handleInput(recognizedText, this)

                }
            }

        /*
        dialogueNotify =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Sanity check
            val uri = result.data
            if (uri == null || result.resultCode != RESULT_OK) {
                Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                return@registerForActivityResult
            }
            val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            rslt?.let {
                val recognizedText = it[0]
                textbox.setText(recognizedText)
                logger.writeLog(recognizedText, 1)
                println(recognizedText) //debug

            }
        } */

        appointmentName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    appntmnt.setName(recognizedText)
                    askUser("An welchem Datum ist der Termin?", this, REQUEST_CODE_STT_DATE)
                }
            }

        appointmentDate =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    try {
                        appntmnt.parseLocalDate(recognizedText)
                        appntmnt.setDate(recognizedText)
                        countDate = 0
                        askUser(
                            "Um welche Uhrzeit ist der Termin?",
                            this,
                            REQUEST_CODE_STT_TIME
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        countDate++
                        if (countDate < 3) {
                            askUser(
                                "Das habe ich nicht richtig verstanden. An welchem Datum ist der Termin?",
                                this,
                                REQUEST_CODE_STT_DATE
                            )
                        }
                    }
                }
            }

        appointmentTime =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    try {
                        appntmnt.parseLocalTime(recognizedText)
                        appntmnt.setTime(recognizedText)
                        countTime = 0
                        askUser(
                            "Wo findet der Termin statt?", this,
                            REQUEST_CODE_STT_LOCATION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        countTime++
                        if (countTime < 3) askUser(
                            "Das habe ich nicht verstanden. Um welche Uhrzeit ist der Termin?",
                            this,
                            REQUEST_CODE_STT_TIME
                        )
                    }
                }
            }

        appointmentLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    appntmnt.setLocation(recognizedText)
                    appntmnt.createAppointment(this)
                }
            }

        appointmentDelete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    appntmnt.deleteAppointment(
                        this,
                        recognizedText
                    )
                }
            }

        appointmentEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    appntmnt.startEdit(recognizedText, this)
                }
            }

        appointmentEditField =             registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Sanity check
            val uri = result.data
            if (uri == null || result.resultCode != RESULT_OK) {
                Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                return@registerForActivityResult
            }
            val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            rslt?.let {
                val recognizedText = it[0]
                textbox.setText(recognizedText)
                logger.writeLog(recognizedText, 1)
                println(recognizedText) //debug
                appntmnt.continueEdit(recognizedText, this)
            }
        }

        appointmentEditNew =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Sanity check
            val uri = result.data
            if (uri == null || result.resultCode != RESULT_OK) {
                Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                return@registerForActivityResult
            }
            val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            rslt?.let {
                val recognizedText = it[0]
                textbox.setText(recognizedText)
                logger.writeLog(recognizedText, 1)
                println(recognizedText) //debug
                try {
                    if (appntmnt.field == "date") {
                        appntmnt.parseLocalDate(recognizedText)
                    }
                    if (appntmnt.field == "time") {
                        appntmnt.parseLocalTime(recognizedText)
                    }
                    countEdit = 0
                    println(recognizedText) //debug
                    appntmnt.editAppointment(recognizedText, this)
                } catch (e: Exception) {
                    e.printStackTrace()
                    countEdit++
                    if (countEdit < 3) {
                        askUser(
                            "Das habe ich nicht richtig verstanden. Wie lautet die Änderung?",
                            this,
                            REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW
                        )
                    }
                }
            }
        }

        appointmentEditAsk =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    if (recognizedText.contains("nein") || recognizedText.contains("Nein") || recognizedText.contains(
                            "Stop"
                        ) || recognizedText.contains("stop") || recognizedText.contains("nichts") || recognizedText.contains(
                            "Nichts"
                        )
                    ) {
                        return@registerForActivityResult
                    } else {
                        println(recognizedText) //debug
                        appntmnt.continueEdit(recognizedText, this)
                    }
                }
            }

        appointmentEditRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    appntmnt.readAppointmentEdit(this)
                }
            }

        appointmentEditNoName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Sanity check
            val uri = result.data
            if (uri == null || result.resultCode != RESULT_OK) {
                Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                return@registerForActivityResult
            }
            val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            rslt?.let {
                val recognizedText = it[0]
                textbox.setText(recognizedText)
                logger.writeLog(recognizedText, 1)
                println(recognizedText) //debug
                appntmnt.setEvent(appntmnt.listSelectedCalendars(recognizedText, this))
                appntmnt.readAppointment(this, recognizedText)
            }
        }

        reminderName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    rmdr.setName(recognizedText)
                    askUser(
                        "An welchem Datum soll ich dich erinnern?",
                        this,
                        REQUEST_CODE_STT_REMINDER_DATE
                    )
                }
            }

        reminderDate =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    try {
                        appntmnt.parseLocalDate(recognizedText)
                        countDate = 0
                        rmdr.setDate(recognizedText)
                        askUser(
                            "Um wieviel Uhr soll ich dich erinnern?",
                            this,
                            REQUEST_CODE_STT_REMINDER_TIME
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                        countDate++
                        if (countDate < 3) {
                            askUser(
                                "Das habe ich nicht richtig verstanden. An welchem Datum soll ich dich erinnern?",
                                this,
                                REQUEST_CODE_STT_REMINDER_DATE
                            )
                        }
                    }
                }
            }
        reminderTime =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    try {
                        appntmnt.parseLocalTime(recognizedText)
                        countTime = 0
                        rmdr.setTime(recognizedText)
                        //askUser("Um wieviel Uhr soll ich dich erinnern?", this, REQUEST_CODE_STT_REMINDER_TIME)
                        rmdr.createReminder(this)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        countTime++
                        if (countTime < 3) {
                            askUser(
                                "Das habe ich nicht richtig verstanden. Um wieviel Uhr soll ich dich erinnern?",
                                this,
                                REQUEST_CODE_STT_REMINDER_TIME
                            )
                        }
                    }
                }
            }

        reminderDelete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Sanity check
            val uri = result.data
            if (uri == null || result.resultCode != RESULT_OK) {
                Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                return@registerForActivityResult
            }
            val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            rslt?.let {
                val recognizedText = it[0]
                textbox.setText(recognizedText)
                logger.writeLog(recognizedText, 1)
                println(recognizedText) //debug
                rmdr.deleteReminder(this, recognizedText)
            }
        }

        reminderRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    rmdr.setEvent(appntmnt.listSelectedCalendars(recognizedText, this))
                    rmdr.readReminder(this, recognizedText)
                }
            }

        reminderEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    rmdr.startEdit(recognizedText, this)
                }
            }
        reminderEditField =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    rmdr.continueEdit(recognizedText, this)
                }
            }
        reminderEditRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    rmdr.readReminderEdit(this)
                }
            }

        reminderEditNew =

            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    try {
                        if (rmdr.field == "date") {
                            appntmnt.parseLocalDate(recognizedText)
                        }
                        if (rmdr.field == "time") {
                            appntmnt.parseLocalTime(recognizedText)
                        }
                        countEdit = 0
                        println(recognizedText) //debug
                        //handler.getField(recognizedText,this)
                        rmdr.editReminder(recognizedText, this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        countEdit++
                        if (countEdit < 3) {
                            askUser(
                                "Das habe ich nicht richtig verstanden. Wie lautet die Änderung?",
                                this,
                                REQUEST_CODE_STT_REMINDER_EDIT_NEW
                            )
                        }
                    }
                }
            }

        reminderEditAsk =

            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                val uri = result.data
                if (uri == null || result.resultCode != RESULT_OK) {
                    Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1)
                    println(recognizedText) //debug
                    if (recognizedText.contains("nein") || recognizedText.contains("Nein") || recognizedText.contains(
                            "Stop"
                        ) || recognizedText.contains("stop") || recognizedText.contains("nichts") || recognizedText.contains(
                            "Nichts"
                        )
                    ) {
                        return@registerForActivityResult
                    } else {
                        println(recognizedText) //debug
                        rmdr.continueEdit(recognizedText, this)
                    }
                }
            }


                val btn_stt = findViewById<Button>(R.id.recordButton)

                /**
                 * Getting user input and starting decision finding process
                 * */

                btn_stt.setOnClickListener {
                    //stt.getUserInputMain(this)
                    stt.getUserInput(this, REQUEST_CODE_STT)
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
                            try {
                                appntmnt.parseLocalDate(recognizedText)
                                appntmnt.setDate(recognizedText)
                                countDate = 0
                                askUser(
                                    "Um welche Uhrzeit ist der Termin?",
                                    this,
                                    REQUEST_CODE_STT_TIME
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                countDate++
                                if (countDate < 3) {
                                    askUser(
                                        "Das habe ich nicht richtig verstanden. An welchem Datum ist der Termin?",
                                        this,
                                        REQUEST_CODE_STT_DATE
                                    )
                                }
                            }
                        }
                    }
                }
                //Get the time for the appointment and ask for location
                REQUEST_CODE_STT_TIME -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            println(recognizedText)
                            try {
                                appntmnt.parseLocalTime(recognizedText)
                                appntmnt.setTime(recognizedText)
                                countTime = 0
                                askUser(
                                    "Wo findet der Termin statt?", this,
                                    REQUEST_CODE_STT_LOCATION
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                countTime++
                                if (countTime < 3) askUser(
                                    "Das habe ich nicht verstanden. Um welche Uhrzeit ist der Termin?",
                                    this,
                                    REQUEST_CODE_STT_TIME
                                )
                            }
                        }
                    }
                }
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
                            try {
                                if (appntmnt.field == "date") {
                                    appntmnt.parseLocalDate(recognizedText)
                                }
                                if (appntmnt.field == "time") {
                                    appntmnt.parseLocalTime(recognizedText)
                                }
                                countEdit = 0
                                println(recognizedText) //debug
                                //handler.getField(recognizedText,this)
                                appntmnt.editAppointment(recognizedText, this)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                countEdit++
                                if (countEdit < 3) {
                                    askUser(
                                        "Das habe ich nicht richtig verstanden. Wie lautet die Änderung?",
                                        this,
                                        REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW
                                    )
                                }
                            }
                        }
                    }
                }
                //Ask if the user wants to do some more changes
                REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            if (recognizedText.contains("nein") || recognizedText.contains("Nein") || recognizedText.contains(
                                    "Stop"
                                ) || recognizedText.contains("stop") || recognizedText.contains("nichts") || recognizedText.contains(
                                    "Nichts"
                                )
                            ) {
                                return
                            } else {
                                println(recognizedText) //debug
                                appntmnt.continueEdit(recognizedText, this)
                            }
                        }
                    }
                }
                //Read out appointment during edit
                REQUEST_CODE_STT_EDIT_APPOINTMENT_READ -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            appntmnt.readAppointmentEdit(this)
                        }
                    }
                }
                //Read out appointment without context set so ask for context
                REQUEST_CODE_STT_READ_APPOINTMENT_NO_NAME -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            appntmnt.setEvent(appntmnt.listSelectedCalendars(recognizedText, this))
                            appntmnt.readAppointment(this, recognizedText)
                        }
                    }
                }
                //Get name for reminder here
                REQUEST_CODE_STT_REMINDER_NAME -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            rmdr.setName(recognizedText)
                            askUser(
                                "An welchem Datum soll ich dich erinnern?",
                                this,
                                REQUEST_CODE_STT_REMINDER_DATE
                            )
                        }
                    }
                }
                //Get date for reminder here
                REQUEST_CODE_STT_REMINDER_DATE -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            try {
                                appntmnt.parseLocalDate(recognizedText)
                                countDate = 0
                                rmdr.setDate(recognizedText)
                                askUser(
                                    "Um wieviel Uhr soll ich dich erinnern?",
                                    this,
                                    REQUEST_CODE_STT_REMINDER_TIME
                                )

                            } catch (e: Exception) {
                                e.printStackTrace()
                                countDate++
                                if (countDate < 3) {
                                    askUser(
                                        "Das habe ich nicht richtig verstanden. An welchem Datum soll ich dich erinnern?",
                                        this,
                                        REQUEST_CODE_STT_REMINDER_DATE
                                    )
                                }
                            }
                        }
                    }

                }
                //Get time for reminder here
                REQUEST_CODE_STT_REMINDER_TIME -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            try {
                                appntmnt.parseLocalTime(recognizedText)
                                countTime = 0
                                rmdr.setTime(recognizedText)
                                //askUser("Um wieviel Uhr soll ich dich erinnern?", this, REQUEST_CODE_STT_REMINDER_TIME)
                                rmdr.createReminder(this)

                            } catch (e: Exception) {
                                e.printStackTrace()
                                countTime++
                                if (countTime < 3) {
                                    askUser(
                                        "Das habe ich nicht richtig verstanden. Um wieviel Uhr soll ich dich erinnern?",
                                        this,
                                        REQUEST_CODE_STT_REMINDER_TIME
                                    )
                                }
                            }
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_DELETE -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            println(recognizedText) //debug
                            rmdr.deleteReminder(this, recognizedText)
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_READ -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            rmdr.setEvent(appntmnt.listSelectedCalendars(recognizedText, this))
                            rmdr.readReminder(this, recognizedText)
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_EDIT -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            println(recognizedText) //debug
                            rmdr.startEdit(recognizedText, this)
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_EDIT_FIELD -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            println(recognizedText) //debug
                            rmdr.continueEdit(recognizedText, this)
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_EDIT_NEW -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            try {
                                if (rmdr.field == "date") {
                                    appntmnt.parseLocalDate(recognizedText)
                                }
                                if (rmdr.field == "time") {
                                    appntmnt.parseLocalTime(recognizedText)
                                }
                                countEdit = 0
                                println(recognizedText) //debug
                                //handler.getField(recognizedText,this)
                                rmdr.editReminder(recognizedText, this)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                countEdit++
                                if (countEdit < 3) {
                                    askUser(
                                        "Das habe ich nicht richtig verstanden. Wie lautet die Änderung?",
                                        this,
                                        REQUEST_CODE_STT_REMINDER_EDIT_NEW
                                    )
                                }
                            }
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_EDIT_ASK -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            if (recognizedText.contains("nein") || recognizedText.contains("Nein") || recognizedText.contains(
                                    "Stop"
                                ) || recognizedText.contains("stop") || recognizedText.contains("nichts") || recognizedText.contains(
                                    "Nichts"
                                )
                            ) {
                                return
                            } else {
                                println(recognizedText) //debug
                                rmdr.continueEdit(recognizedText, this)
                            }
                        }
                    }
                }
                REQUEST_CODE_STT_REMINDER_EDIT_READ -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        result?.let {
                            val recognizedText = it[0]
                            textbox.setText(recognizedText)
                            logger.writeLog(recognizedText, 1)
                            rmdr.readReminderEdit(this)
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
         * This function reads out a given text and continues an appropriate dialogue.
         * @param text text that will be read out
         * @param mainActivity context
         * @param requestCode code that is used to determine what to do next
         */
        internal fun askUser(text: String, mainActivity: MainActivity, requestCode: Int) {
            //val textbox = findViewById<EditText>(R.id.et_text_input)
            //textbox.setText(text)
            //val logger = Logger()

            val job = GlobalScope.launch {
                println("waiting in thread: ${Thread.currentThread().name}") //Debug
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mainActivity.textToSpeechEngine.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "tts1"
                    )

                    logger.writeLog(text, 0)
                } else {
                    mainActivity.textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                    logger.writeLog(text, 0)

                }
                var speakingEnd: Boolean = textToSpeechEngine.isSpeaking
                do {
                    delay(500)
                    speakingEnd = textToSpeechEngine.isSpeaking
                } while (speakingEnd)
                //delay((text.length * 80).toLong())
            }
            //Wait until job is done, speaking in this case
            runBlocking {
                job.join()
            }

            if (requestCode != REQUEST_CODE_STT_NOTIFY) mainActivity.stt.getUserInput(
                mainActivity,
                requestCode
            )

        }

        internal fun waitForTTS(mainActivity: MainActivity) {
            var speakingEnd: Boolean = textToSpeechEngine.isSpeaking
            do {
                Thread.sleep(500)
                speakingEnd = textToSpeechEngine.isSpeaking
            } while (speakingEnd)
        }


        /**
         * This function starts the dialogue to create a list
         **/
        private fun createList() {

        }


    }
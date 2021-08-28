package de.test.errorcorrection

import android.content.Intent
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
import java.util.*
import kotlin.Exception


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
        internal const val REQUEST_CODE_STT_LIST_NAME = 24
        internal const val REQUEST_CODE_STT_LIST_CREATE_ITEM = 25
        internal const val REQUEST_CODE_STT_LIST_READ = 26
        internal const val REQUEST_CODE_STT_LIST_DELETE = 27
        internal const val REQUEST_CODE_STT_LIST_EDIT = 28
        internal const val REQUEST_CODE_STT_LIST_EDIT_FIELD = 29
        internal const val REQUEST_CODE_STT_LIST_EDIT_ITEM_ADD = 30
        internal const val REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE = 31
        internal const val REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE_NEXT = 32
        internal const val REQUEST_CODE_STT_LIST_EDIT_ITEM_REMOVE = 33
        internal const val REQUEST_CODE_STT_LIST_EDIT_ITEM_READ = 34
        internal const val REQUEST_CODE_STT_LIST_EDIT_NAME = 35
        internal const val REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION = 36
        internal const val REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END = 37
        internal const val REQUEST_CODE_STT_REMINDER_EDIT_CREATION = 38
        internal const val REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END = 39
    }

    //Initialize TTS-Engine
    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(
            this@MainActivity
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechEngine.language = Locale.US
            }
        }
    }

    private lateinit var logger: Logger
    private lateinit var permissions: Permissions
    internal lateinit var handler: IntendHandler
    private lateinit var stt: STT
    internal lateinit var appntmnt: Appointment
    private lateinit var rmdr: Reminder
    private lateinit var lst: List

    internal var currentList = -1

    internal lateinit var dialogueStart: ActivityResultLauncher<Intent>
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
    internal lateinit var listName: ActivityResultLauncher<Intent>
    internal lateinit var listItem: ActivityResultLauncher<Intent>
    internal lateinit var listRead: ActivityResultLauncher<Intent>
    internal lateinit var listDelete: ActivityResultLauncher<Intent>
    internal lateinit var listEdit: ActivityResultLauncher<Intent>
    internal lateinit var listEditItemAdd: ActivityResultLauncher<Intent>
    internal lateinit var listEditItemRemove: ActivityResultLauncher<Intent>
    internal lateinit var listEditItemReplace: ActivityResultLauncher<Intent>
    internal lateinit var listEditItemReplaceNext: ActivityResultLauncher<Intent>
    internal lateinit var listEditField: ActivityResultLauncher<Intent>
    internal lateinit var listEditName: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditCreation: ActivityResultLauncher<Intent>
    internal lateinit var appointmentEditCreationSecond: ActivityResultLauncher<Intent>
    internal lateinit var reminderEditCreation: ActivityResultLauncher<Intent>
    internal lateinit var reminderEditCreationSecond: ActivityResultLauncher<Intent>

    private var countTime: Int = 0
    private var countDate: Int = 0
    private var countEdit: Int = 0
    internal var countName: Int = 0


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
        appntmnt = Appointment()
        rmdr = Reminder()
        lst = List()

        // Checking permissions
        permissions.checkPermissions(this)
        setContentView(R.layout.activity_main)

        val textbox = findViewById<EditText>(R.id.et_text_input)


        var loadedLst = lst.loadLists<List>(this)
        if (loadedLst != null) {
            lst = loadedLst
        }

        //Init ActivityResultLauncher
        //Initial dialogue
        dialogueStart =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }

                val rslt =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    handler.handleInput(recognizedText, this)

                }
            }
        //Get appointments name during creation
        appointmentName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    appntmnt.setName(recognizedText)
                    //askUser("An welchem Datum ist der Termin?", this, REQUEST_CODE_STT_DATE)
                    askUser("What date is the appointment?", this, REQUEST_CODE_STT_DATE)
                }
            }
        //Get appointmnets date during creation
        appointmentDate =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    //TESTING
                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditAppointment(
                            editField,
                            "I did not really get that. What date is the appointment?",
                            this,
                            REQUEST_CODE_STT_DATE,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }

                    try {
                        appntmnt.parseLocalDate(recognizedText)
                        appntmnt.setDate(recognizedText)
                        countDate = 0
                        //askUser("Um welche Uhrzeit ist der Termin?",
                        askUser(
                            "What time is the appointment?",
                            this,
                            REQUEST_CODE_STT_TIME
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        countDate++
                        if (countDate < 3) {
                            askUser(
                                //"Das habe ich nicht richtig verstanden. An welchem Datum ist der Termin?",
                                "I did not really get that. What date is the appointment?",
                                this,
                                REQUEST_CODE_STT_DATE
                            )
                        }
                    }
                }
            }
        //Get appointments time during creation
        appointmentTime =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditAppointment(
                            editField,
                            "I did not understand. What time is the appointment?",
                            this,
                            REQUEST_CODE_STT_TIME,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }

                    try {
                        appntmnt.parseLocalTime(recognizedText)
                        appntmnt.setTime(recognizedText)
                        countTime = 0
                        askUser(
                            //"Wo findet der Termin statt?", this,
                            "Where does the appointment take place?", this,
                            REQUEST_CODE_STT_LOCATION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        countTime++
                        if (countTime < 3) askUser(
                            //"Das habe ich nicht verstanden. Um welche Uhrzeit ist der Termin?",
                            "I did not understand. What time is the appointment?",
                            this,
                            REQUEST_CODE_STT_TIME
                        )
                    }
                }
            }
        //Get appointmnets location during creation
        appointmentLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditAppointment(
                            editField,
                            "I did not understand. Where does the appointment take place?",
                            this,
                            REQUEST_CODE_STT_LOCATION,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }

                    appntmnt.setLocation(recognizedText)
                    appntmnt.addError()
                    appntmnt.readData(this)
                    //appntmnt.createAppointment(this)
                }
            }
        appointmentEditCreationSecond =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    if (recognizedText.contains("read") || recognizedText.contains("Read")) {
                        appntmnt.readData(this)
                        return@registerForActivityResult
                    }
                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditAppointment(
                            editField,
                            "I did not understand that. What field do you want to edit?",
                            this,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }
                    if (stopDialogue(recognizedText)) {
                        appntmnt.createAppointment(this)
                    } else {
                        askUser(
                            "I did not understand that. What do you want to do?",
                            this,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END
                        )
                    }
                }
            }
        //Delete appointment dialogue, calls delete function
        appointmentDelete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    appntmnt.deleteAppointment(
                        this,
                        recognizedText
                    )
                }
            }
        //Start of edit appointment dialogue
        appointmentEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    appntmnt.startEdit(recognizedText, this)
                }
            }
        //Get field to edit for appointments
        appointmentEditField =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    if (stopDialogue(recognizedText)) return@registerForActivityResult
                    println(recognizedText) //debug
                    appntmnt.continueEdit(recognizedText, this)
                }
            }
        //Last step to edit appointment, also sanity checking for dates and time
        appointmentEditNew =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    try {
                        if (appntmnt.getField() == "date") {
                            appntmnt.parseLocalDate(recognizedText)
                        }
                        if (appntmnt.getField() == "time") {
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
                                //"Das habe ich nicht richtig verstanden. Wie lautet die Änderung?",
                                "I did not really get that. What's the change?",
                                this,
                                REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW
                            )
                        }
                    }
                }
            }
        //Appointment get field during edit, maybe delete this later
        appointmentEditAsk =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (stopDialogue(recognizedText)) {
                        return@registerForActivityResult
                    } else {
                        println(recognizedText) //debug
                        appntmnt.continueEdit(recognizedText, this)
                    }
                }
            }
        //Read out an appointment during edit
        appointmentEditRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    appntmnt.readAppointmentEdit(this)
                }
            }
        //Read out an appointment
        appointmentEditNoName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    appntmnt.setEvent(appntmnt.listSelectedCalendars(recognizedText, this))
                    appntmnt.readAppointment(this, recognizedText)
                }
            }
        //Get reminders name during creation
        reminderName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    rmdr.setName(recognizedText)
                    askUser(
                        //"An welchem Datum soll ich dich erinnern?",
                        "What date should I remind you?",
                        this,
                        REQUEST_CODE_STT_REMINDER_DATE
                    )
                }
            }
        //Get reminders date during creation
        reminderDate =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditReminder(
                            editField,
                            "I didn't understand that correctly. What date should I remind you?",
                            this,
                            REQUEST_CODE_STT_REMINDER_DATE,
                            REQUEST_CODE_STT_REMINDER_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }

                    try {
                        appntmnt.parseLocalDate(recognizedText)
                        countDate = 0
                        rmdr.setDate(recognizedText)
                        askUser(
                            //"Um wieviel Uhr soll ich dich erinnern?",
                            "What time should I remind you?",
                            this,
                            REQUEST_CODE_STT_REMINDER_TIME
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                        countDate++
                        if (countDate < 3) {
                            askUser(
                                //"Das habe ich nicht richtig verstanden. An welchem Datum soll ich dich erinnern?",
                                "I didn't understand that correctly. What date should I remind you?",
                                this,
                                REQUEST_CODE_STT_REMINDER_DATE
                            )
                        }
                    }
                }
            }
        //Get reminders time during creation
        reminderTime =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditReminder(
                            editField,
                            "I didn't understand that correctly. What time should I remind you?",
                            this,
                            REQUEST_CODE_STT_REMINDER_TIME,
                            REQUEST_CODE_STT_REMINDER_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }

                    try {
                        appntmnt.parseLocalTime(recognizedText)
                        countTime = 0
                        rmdr.setTime(recognizedText)
                        rmdr.addError()
                        rmdr.readData(this)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        countTime++
                        if (countTime < 3) {
                            askUser(
                                //"Das habe ich nicht richtig verstanden. Um wieviel Uhr soll ich dich erinnern?",
                                "I didn't understand that correctly. What time should I remind you?",
                                this,
                                REQUEST_CODE_STT_REMINDER_TIME
                            )
                        }
                    }
                }
            }
        //Edit reminder before creating is finished
        reminderEditCreationSecond =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug

                    if (recognizedText.contains("read") || recognizedText.contains("Read")) {
                        rmdr.readData(this)
                        return@registerForActivityResult
                    }
                    var editField = handler.editCheck(recognizedText, this)
                    if (editField != "continue") {
                        handleEditReminder(
                            editField,
                            "I did not understand that. What field do you want to edit?",
                            this,
                            REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END,
                            REQUEST_CODE_STT_REMINDER_EDIT_CREATION
                        )
                        return@registerForActivityResult
                    }
                    if (stopDialogue(recognizedText)) {
                        rmdr.createReminder(this)
                    } else {
                        askUser(
                            "I did not understand that. What do you want to do?",
                            this,
                            REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END
                        )
                    }
                }
            }
        //Delete a reminder
        reminderDelete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    rmdr.deleteReminder(this, recognizedText)
                }
            }
        //Read out a reminder
        reminderRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    rmdr.setEvent(appntmnt.listSelectedCalendars(recognizedText, this))
                    rmdr.readReminder(this, recognizedText)
                }
            }
        //Start editing a reminder
        reminderEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    rmdr.startEdit(recognizedText, this)
                }
            }
        //Get field to edit
        reminderEditField =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    if (stopDialogue(recognizedText)) return@registerForActivityResult
                    println(recognizedText) //debug
                    rmdr.continueEdit(recognizedText, this)
                }
            }
        //Read out reminder during edit
        reminderEditRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    rmdr.readReminderEdit(this)
                }
            }
        //Sanity check for date and time during edit and calls final edit function
        reminderEditNew =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    try {
                        if (rmdr.getField() == "date") {
                            appntmnt.parseLocalDate(recognizedText)
                        }
                        if (rmdr.getField() == "time") {
                            appntmnt.parseLocalTime(recognizedText)
                        }
                        countEdit = 0
                        println(recognizedText) //debug
                        rmdr.editReminder(recognizedText, this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        countEdit++
                        if (countEdit < 3) {
                            askUser(
                                //"Das habe ich nicht richtig verstanden. Wie lautet die Änderung?",
                                "I didn't understand that correctly. What's the change?",
                                this,
                                REQUEST_CODE_STT_REMINDER_EDIT_NEW
                            )
                        }
                    }
                }
            }
        //Get field to edit, maybe delete later
        reminderEditAsk =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (stopDialogue(recognizedText)) {
                        return@registerForActivityResult
                    } else {
                        println(recognizedText) //debug
                        rmdr.continueEdit(recognizedText, this)
                    }
                }
            }
        //Get lists name during creation
        listName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (countName < 3) {
                        countName++
                        lst.createList(this, recognizedText)
                    }

                }
            }
        //Get items for the list during creation
        listItem =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (stopDialogue(recognizedText)) {
                        return@registerForActivityResult
                    }
                    lst.addItem(this, recognizedText)
                }
            }
        //Reads out the list
        listRead =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (countName < 3) {
                        countName++
                        lst.readList(this, recognizedText, REQUEST_CODE_STT_LIST_READ)
                    }
                }
            }
        //Deletes a list
        listDelete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (countName < 3) {
                        countName++
                        lst.deleteList(this, recognizedText)
                    }
                }
            }
        //Start list edit dialogue
        listEdit =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (countName < 3) {
                        countName++
                        lst.editList(this, recognizedText)
                    }
                }
            }
        //Get field/item to edit
        listEditField =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (stopDialogue(recognizedText)) return@registerForActivityResult
                    var field = handler.getListIntend(recognizedText)
                    if (field.contains("error")) {
                        askUser(
                            //"Das habe ich nicht richtig verstanden, was soll ich machen?",
                            "I didn't understand that correctly, what should I do?",
                            this,
                            REQUEST_CODE_STT_LIST_EDIT_FIELD
                        )
                    }
                    when (field) {
                        "read" -> {
                            lst.readList(this, "", REQUEST_CODE_STT_LIST_EDIT_ITEM_READ)
                        }
                        "replace" -> {
                            askUser(
                                //"Welcher Gegenstand soll ersetzt werden?",
                                "Which item should be replaced?",
                                this,
                                REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE
                            )
                        }
                        "add" -> {
                            askUser(
                                //"Was möchtest du hinzufügen?",
                                "What do you want to add?",
                                this,
                                REQUEST_CODE_STT_LIST_EDIT_ITEM_ADD
                            )
                        }
                        "delete" -> {
                            askUser(
                                "What do you want to remove from the list?",
                                //"Was möchtest du von der Liste entfernen?",
                                this,
                                REQUEST_CODE_STT_LIST_EDIT_ITEM_REMOVE
                            )
                        }
                        "name" -> {
                            askUser(
                                //"Wie lautet der neue Name der Liste?",
                                "What's the new name of the list?",
                                this,
                                REQUEST_CODE_STT_LIST_EDIT_NAME
                            )
                        }
                    }

                }
            }
        //Add item during edit
        listEditItemAdd =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    lst.addItemEdit(this, recognizedText)
                }
            }
        //remive item during edit
        listEditItemRemove =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    if (countName < 3) {
                        countName++
                        lst.removeItem(this, recognizedText)
                    } else {
                        askUser(
                            //"Was möchtest du bearbeiten?",
                            "What do you want to edit?",
                            this,
                            REQUEST_CODE_STT_LIST_EDIT_FIELD
                        )
                    }
                }
            }
        //replace item during edit
        listEditItemReplace =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    //suche gegenstand in liste
                    if (countName < 3) {
                        countName++
                        var i = lst.lists[currentList].indexOf(recognizedText)
                        if (i >= 0) {
                            //item found
                            countName = 0
                            lst.replaceableIndex = i
                            lst.replaceable = recognizedText
                            askUser(
                                //"Durch was soll $recognizedText ersetzt werden?",
                                "What should $recognizedText be replaced by?",
                                this,
                                REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE_NEXT
                            )
                        } else {
                            //item not found
                            askUser(
                                //"Ich habe $recognizedText nicht in der Liste gefunden. Was soll ersetzt werden?",
                                "I couldn't find $recognizedText in the list. What should be replaced?",
                                this,
                                REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE
                            )
                        }
                    }
                }
            }
        //second step of item replacement
        listEditItemReplaceNext =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    lst.lists[currentList][lst.replaceableIndex] = recognizedText
                    askUser(
                        //"Ich habe ${lst.replaceable} durch $recognizedText ersetzt. Möchtest du noch etwas bearbeiten?",
                        "I replaced ${lst.replaceable} with $recognizedText. Would you like to edit something else?",
                        this,
                        REQUEST_CODE_STT_LIST_EDIT_FIELD
                    )
                    lst.replaceable = ""
                    lst.replaceableIndex = -1

                }
            }
        //Edit name during edit
        listEditName =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    lst.lists[currentList][0] = recognizedText
                    askUser(
                        //"Ich habe den Namen der Liste auf $recognizedText geändert. Was möchtest du noch bearbeiten?",
                        "I changed the name of the list to $recognizedText. What else do you want to edit?",
                        this,
                        REQUEST_CODE_STT_LIST_EDIT_FIELD
                    )
                }
            }
        //Appointment edit during creation
        appointmentEditCreation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    try {
                        appntmnt.checkDateAndTimeValidity(recognizedText)
                        appntmnt.updateParameter(this, recognizedText)
                        countEdit = 0
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        countEdit++
                        askUser(
                            "Unfortunately I did not understand that. What is the update?",
                            this,
                            REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION
                        )
                    }
                }
            }
        //Reminder edit during creation
        reminderEditCreation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                //Sanity check
                if (checkIntentData(result)) {
                    return@registerForActivityResult
                }
                val rslt = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                rslt?.let {
                    val recognizedText = it[0]
                    textbox.setText(recognizedText)
                    logger.writeLog(recognizedText, 1, this)
                    println(recognizedText) //debug
                    try {
                        appntmnt.checkDateAndTimeValidity(recognizedText)
                        rmdr.updateParameter(this, recognizedText)
                        countEdit = 0
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        countEdit++
                        askUser(
                            "Unfortunately I did not understand that. What is the update?",
                            this,
                            REQUEST_CODE_STT_REMINDER_EDIT_CREATION
                        )
                    }
                }
            }

        val btnSTT = findViewById<Button>(R.id.recordButton)

        /**
         * Getting user input and starting decision finding process
         * */

        btnSTT.setOnClickListener {
            stt.getUserInput(this, REQUEST_CODE_STT)
            countName = 0
            countDate = 0
            countTime = 0
            countEdit = 0
        }

    }

    /**
     * This function checks if the user wants to stop the dialogue
     * @param recognizedText User input
     * @return
     */
    private fun stopDialogue(recognizedText: String): Boolean {
        return (recognizedText.contains("nein") || recognizedText.contains("Nein") || recognizedText.contains(
            "Stop"
        ) || recognizedText.contains("stop") || recognizedText.contains("nichts") || recognizedText.contains(
            "Nichts"
        ) || recognizedText.contains("no") || recognizedText.contains("No") || recognizedText.contains(
            "nothing"
        ) || recognizedText.contains("Nothing"))
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
        val job = GlobalScope.launch {
            println("waiting in thread: ${Thread.currentThread().name}") //Debug
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainActivity.textToSpeechEngine.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "tts1"
                )
                logger.writeLog(text, 0, mainActivity)
            /*} else {
                mainActivity.textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                logger.writeLog(text, 0, mainActivity)
            }
            var speakingEnd: Boolean// = textToSpeechEngine.isSpeaking
            do {
                delay(500)
                speakingEnd = textToSpeechEngine.isSpeaking
            } while (speakingEnd)*/
            waitForTTS()
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

    internal fun waitForTTS() {
        var speakingEnd: Boolean// = textToSpeechEngine.isSpeaking
        do {
            Thread.sleep(500)
            speakingEnd = textToSpeechEngine.isSpeaking
        } while (speakingEnd)
    }

    /**
     * This function checks intent data and activity result for validity
     * @param rslt ActivityResult
     * @return
     */
    private fun checkIntentData(rslt: ActivityResult): Boolean {
        return if (rslt.data == null || rslt.resultCode != RESULT_OK) {
            Log.i("FileResultLauncher", "No Uri returned or result wasn't OK.")
            true
        } else false
    }

    /**
     * This function handles edit intend during appointment creation
     * @param field Field that will be edited
     * @param msg Message that will be asked
     * @param mainActivity Context
     * @param requestCode Context to know what to do next
     */
    private fun handleEditAppointment(
        field: String,
        msg: String,
        mainActivity: MainActivity,
        errorRequestCode: Int,
        requestCode: Int
    ) {
        if (field == "error") {
            askUser(
                msg,
                this,
                errorRequestCode
            )
        } else {
            //TODO handle "read" case
            appntmnt.setField(field)
            askUser(
                "What is the new $field?",
                mainActivity,
                requestCode
            )
        }
    }

    /**
     * This function handles edit intend during reminder creation
     * @param field Field that will be edited
     * @param msg Message that will be asked
     * @param mainActivity Context
     * @param requestCode Context to know what to do next
     */
    private fun handleEditReminder(
        field: String,
        msg: String,
        mainActivity: MainActivity,
        errorRequestCode: Int,
        requestCode: Int
    ) {
        if (field == "error") {
            askUser(
                msg,
                this,
                errorRequestCode
            )
        } else {
            //TODO handle "read" case
            rmdr.setField(field)
            askUser(
                "What is the new $field?",
                mainActivity,
                requestCode
            )
        }
    }

}
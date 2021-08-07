package de.test.errorcorrection

import android.widget.EditText
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class Appointment {

    /*
    private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.GERMANY
                }
            })
    }*/

    lateinit private var name: String
    lateinit private var date: String
    lateinit private var time: String
    lateinit private var location: String




    /**
     * This function starts the dialogue to create an appointment
     * @param mainActivity context for functions and SST and TTS
     */
    internal fun createAppointment(mainActivity: MainActivity) {

        println("Erstelle Termin")
        //mainActivity.stt.getUserInput(mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
        askName(mainActivity)
    /*
        //askName(mainActivity)
        var name = askName(mainActivity)
        //askDate()
        var date = askDate()
        //askTime()
        var time = askTime()
        //askLocation()
        var location = askLocation()

        */
        //println("test " +name)
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


        //mainActivity.sem.acquire()
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
        //sem to wait here
        //mainActivity.sem.acquire()
        Thread.sleep(3000)
        //println(mainActivity.sem.queueLength)
       // mainActivity.stt.getUserInput(mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
        //Thread.sleep(5000)
        //mainActivity.sem.acquire()
        //println(mainActivity.sem.queueLength)
        //var ret = mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString()
        //Thread.sleep(3000)
        /*
        while (mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString() == "" || mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString() == s) {

        }*/
        return mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString()
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

    internal fun setName(name: String) {
        this.name = name
    }

    internal fun setDate(date: String) {
        this.date = date
    }

    internal fun setTime(time: String) {
        this.time = time
    }

    internal fun setLocation(location: String) {
        this.location = location
    }


}
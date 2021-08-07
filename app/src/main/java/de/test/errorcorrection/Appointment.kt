package de.test.errorcorrection

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.EditText
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

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
    /*
    lateinit internal var name: String
    lateinit internal var date: String
    lateinit internal var time: String
    lateinit internal var location: String


     */

    /**
     * This function starts the dialogue to create an appointment
     * @param mainActivity context for functions and SST and TTS
     */
    internal fun createAppointment(mainActivity: MainActivity) {

        println("Erstelle Termin")
        //askName(mainActivity)
        var name = askName(mainActivity)
        //askDate()
        var date = askDate()
        //askTime()
        var time = askTime()
        //askLocation()
        var location = askLocation()

        println("test " +name)
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


        mainActivity.sem.acquire()
        mainActivity.askUser(s, mainActivity)
        //sem to wait here
        //mainActivity.sem.acquire()
        Thread.sleep(3000)
        //println(mainActivity.sem.queueLength)
        mainActivity.stt.getUserInput(mainActivity)
        //Thread.sleep(5000)
        //mainActivity.sem.acquire()
        println(mainActivity.sem.queueLength)
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



}
package de.test.errorcorrection

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.EditText
import android.widget.Toast
import java.util.*

class STT {


    /**
     * This method generates a String from users voice input
     * @return User input
     */
    internal fun getUserInputMain(mainActivity: MainActivity): String {
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

        try {
            mainActivity.startActivityForResult(sttIntent, MainActivity.REQUEST_CODE_STT)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
        }
        return mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString()
    }

    /**
     * This method generates a String from users voice input
     * @return User input
     */
    internal fun getUserInput(mainActivity: MainActivity): String {
        mainActivity.sem.acquire()
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")


        try {

            mainActivity.startActivityForResult(sttIntent, MainActivity.REQUEST_CODE_STT_ANSWER)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
        }

        //mainActivity.sem.acquire()
        //Thread.sleep(3000)
        //mainActivity.sem.acquire()

        println(mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString())
        //mainActivity.sem.release()
        return "foo"//mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString()
    }


}
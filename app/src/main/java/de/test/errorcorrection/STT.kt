package de.test.errorcorrection

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.EditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class STT {
    lateinit internal var job: Job


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
    internal fun getUserInput(mainActivity: MainActivity, requestCode: Int): String = runBlocking {
    //internal fun getUserInput(mainActivity: MainActivity): String = runBlocking {
        //mainActivity.sem.acquire()
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")


        try {
                mainActivity.startActivityForResult(sttIntent, requestCode)

            //mainActivity.startActivityForResult(sttIntent, MainActivity.REQUEST_CODE_STT_ANSWER)
            //mainActivity.setResult(Activity.RESULT_OK, sttIntent)
            //mainActivity.finish()
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
        }

        //mainActivity.sem.acquire()
        //Thread.sleep(3000)
        //mainActivity.sem.acquire()

        println(mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString())
        //mainActivity.sem.release()
        return@runBlocking "foo"//mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString()
    }


}
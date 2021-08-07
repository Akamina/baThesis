package de.test.errorcorrection

import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.EditText
import java.util.*

class TTS {

    //internal lateinit var textToSpeechEngine: TextToSpeech
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToSpeechEngine = TextToSpeech(MainActivity., OnInitListener { i ->

        })
    }*/

    private var ttse: TextToSpeech? = null

    /*
    /**
     * TODO
     *
     * @param text
     */
    internal fun askUser(text: String, mainActivity: MainActivity) {

        /*
        val textToSpeechEngine = TextToSpeech(mainActivity, )
        val textToSpeechEngine: TextToSpeech by lazy {
            TextToSpeech(mainActivity
            ) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.GERMANY
                }
            }*/

        /*val textToSpeechEngine: TextToSpeech by lazy {
            TextToSpeech(
                mainActivity
            ) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.GERMANY
                }
            }
        }*/

        val textbox = mainActivity.findViewById<EditText>(R.id.et_text_input)
        textbox.setText(text)

        val text2 = textbox.text.toString().trim()
        /*if (text.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                //Logger.readLog()
            } else {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)

                //Logger.readLog()

            }
            //getUserInput()
            //askUser(text, this)
        } else {
            Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
        }*/
        val logger = Logger()


        //if (ttse != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
                //ttse!!.speak(text2, TextToSpeech.QUEUE_FLUSH, null, "tts1")

                logger.writeLog(text2, 0)
            } else {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)
                //ttse!!.speak(text2, TextToSpeech.QUEUE_FLUSH, null)

                logger.writeLog(text2, 0)

            }
        //} else {
          //  println("ttse ist null")
        //}



    } */
    /*

    override fun onInit(p0: Int) {
        TODO("Not yet implemented")
        if (p0 == TextToSpeech.SUCCESS) {
            val result = ttse!!.setLanguage(Locale.GERMANY)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //handle
                Log.e("TTS", "Lang not supported")
            } else  {

            }
        } else {
            Log.e("TTS", "Init failed")
        }
    }*/


}
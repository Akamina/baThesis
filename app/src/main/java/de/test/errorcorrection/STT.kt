package de.test.errorcorrection

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class STT {


    /**
     * This method generates a String from users voice input. Used to start dialogue.
     * @param mainActivity Context
     */
    internal fun getUserInputMain(mainActivity: MainActivity) {
        //Create Intent
        //val tmp = Intent(mainActivity, RecognizerIntent())
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")

        //could be a possible solution
        sttIntent.putExtra("REQUEST_CODE", MainActivity.REQUEST_CODE_STT + 1)
        println("in inputmain: ${MainActivity.REQUEST_CODE_STT}")
        println("in inputmain: ${sttIntent.extras?.get("REQUEST_CODE")}")
        mainActivity.setResult(Activity.RESULT_OK, sttIntent)
        //mainActivity.test.launch(sttIntent)

        //Start STT activity
        try {
            mainActivity.startActivityForResult(sttIntent, MainActivity.REQUEST_CODE_STT)
            //mainActivity.test.launch(sttIntent)
            /*
                var test =
                mainActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { rslt: ActivityResult ->
                    val textbox = mainActivity.findViewById<EditText>(R.id.et_text_input)
                    val result =
                        rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    result?.let {
                        val recognizedText = it[0]
                        textbox.setText(recognizedText)
                        mainActivity.logger.writeLog(recognizedText, 1)
                        mainActivity.handler.handleInput(recognizedText, mainActivity)
                    }
                }

             */
            //mainActivity.test.launch(sttIntent)

        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * This function
     */
    internal fun getUserInput(mainActivity: MainActivity, requestCode: Int) {
    //internal fun getUserInput(mainActivity: MainActivity): String = runBlocking {
        //mainActivity.sem.acquire()
        //Create Intent
        val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        sttIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.GERMAN)
        sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
        sttIntent.putExtra("REQUEST_CODE", requestCode)

        mainActivity.setResult(Activity.RESULT_OK, sttIntent)
        //Start STT activity
        try {
            mainActivity.startActivityForResult(sttIntent, requestCode)
            //mainActivity.test.launch(sttIntent)


            /*
                    if (rslt.resultCode == Activity.RESULT_OK && rslt.data != null) {
                        val textbox = mainActivity.findViewById<EditText>(R.id.et_text_input)
                        println("alles ok")
                        when (requestCode) {
                            MainActivity.REQUEST_CODE_STT -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    mainActivity.handler.handleInput(recognizedText, mainActivity)
                                }
                                //}
                            }

                            //Get name of appointment and ask for date
                            MainActivity.REQUEST_CODE_STT_NAME -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText) //Debug
                                    mainActivity.appntmnt.setName(recognizedText)
                                    mainActivity.askUser(
                                        "An welchem Datum ist der Termin?", mainActivity,
                                        MainActivity.REQUEST_CODE_STT_DATE
                                    )

                                }
                                //}
                            }
                            //Get the date of the appointment and ask for time
                            MainActivity.REQUEST_CODE_STT_DATE -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText) //debug
                                    mainActivity.appntmnt.setDate(recognizedText)
                                    mainActivity.askUser(
                                        "Um welche Uhrzeit ist der Termin?", mainActivity,
                                        MainActivity.REQUEST_CODE_STT_TIME
                                    )

                                }
                                //}
                            }
                            //Get the time for the appointment and ask for location
                            //TODO add check for valid date, if invalid ask again
                            MainActivity.REQUEST_CODE_STT_TIME -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText)
                                    mainActivity.appntmnt.setTime(recognizedText)
                                    mainActivity.askUser(
                                        "Wo findet der Termin statt?", mainActivity,
                                        MainActivity.REQUEST_CODE_STT_LOCATION
                                    )

                                }
                                //}
                            }
                            //TODO add check for valid time, if invalid ask again
                            //Get the location for the appointment and start creating it
                            MainActivity.REQUEST_CODE_STT_LOCATION -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText) //debug
                                    mainActivity.appntmnt.setLocation(recognizedText)
                                    mainActivity.appntmnt.createAppointment(mainActivity)
                                }
                                //}
                            }
                            //Get the name of the appointment and call delete function
                            MainActivity.REQUEST_CODE_STT_DELETE_APPOINTMENT -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText) //debug
                                    mainActivity.appntmnt.deleteAppointment(
                                        mainActivity,
                                        recognizedText
                                    )
                                }
                                //}
                            }
                            //Get the name of the appointment and call edit function
                            MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText) //debug
                                    mainActivity.appntmnt.startEdit(recognizedText, mainActivity)
                                }
                                //}
                            }
                            //Get the field to edit and continue edit
                            MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD -> {
                                //if (resultCode == Activity.RESULT_OK && result.data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    println(recognizedText) //debug
                                    mainActivity.appntmnt.continueEdit(recognizedText, mainActivity)
                                }
                                //}
                            }
                            //Get changes and perform these on the event
                            MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW -> {
                                //if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                                val result =
                                    rslt.data!!.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                                result?.let {
                                    val recognizedText = it[0]
                                    textbox.setText(recognizedText)
                                    mainActivity.logger.writeLog(recognizedText, 1)
                                    kotlin.io.println(recognizedText) //debug
                                    //TODO add fucntion to edit event
                                    mainActivity.appntmnt.editAppointment(
                                        recognizedText,
                                        mainActivity
                                    )
                                }
                            }
                        }
                    }



             */
                   // test.launch(sttIntent)
                    //mainActivity.register

                    //mainActivity.startActivityForResult(sttIntent, MainActivity.REQUEST_CODE_STT_ANSWER)
                    //mainActivity.setResult(Activity.RESULT_OK, sttIntent)
                    //mainActivity.finish()
                //}
        }catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(this, "Your device does not support STT.", Toast.LENGTH_LONG).show()
        }


        //mainActivity.sem.acquire()
        //Thread.sleep(3000)
        //mainActivity.sem.acquire()

        //println(mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString())
        //mainActivity.sem.release()
        //return "foo"//mainActivity.findViewById<EditText>(R.id.et_text_input).getText().toString()
    }


}
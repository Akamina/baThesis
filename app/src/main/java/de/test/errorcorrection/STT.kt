package de.test.errorcorrection

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.*

class STT {

/*
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
            //mainActivity.startActivityForResult(sttIntent, MainActivity.REQUEST_CODE_STT)
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
    */

    /**
     * This function
     */
    internal fun getUserInput(mainActivity: MainActivity, requestCode: Int) {
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
            when (requestCode) {
                MainActivity.REQUEST_CODE_STT -> {
                    mainActivity.dialogueStart.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_NAME -> {
                    mainActivity.appointmentName.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_DATE -> {
                    mainActivity.appointmentDate.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_TIME -> {
                    mainActivity.appointmentTime.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LOCATION -> {
                    mainActivity.appointmentLocation.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_DELETE_APPOINTMENT -> {
                    mainActivity.appointmentDelete.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT -> {
                    mainActivity.appointmentEdit.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD -> {
                    mainActivity.appointmentEditField.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW -> {
                    mainActivity.appointmentEditNew.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK -> {
                    mainActivity.appointmentEditAsk.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_READ -> {
                    mainActivity.appointmentEditRead.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_READ_APPOINTMENT_NO_NAME -> {
                    mainActivity.appointmentEditNoName.launch(sttIntent) //maybe rename to readNoName
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_NAME -> {
                    mainActivity.reminderName.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_DATE -> {
                    mainActivity.reminderDate.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_TIME -> {
                    mainActivity.reminderTime.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_DELETE -> {
                    mainActivity.reminderDelete.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_READ -> {
                    mainActivity.reminderRead.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT -> {
                    mainActivity.reminderEdit.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD -> {
                    mainActivity.reminderEditField.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_READ -> {
                    mainActivity.reminderEditRead.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_NEW -> {
                    mainActivity.reminderEditNew.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK -> {
                    mainActivity.reminderEditAsk.launch(sttIntent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }


}
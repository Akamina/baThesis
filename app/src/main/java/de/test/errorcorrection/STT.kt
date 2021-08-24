package de.test.errorcorrection

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import java.util.*

class STT {

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
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
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
                MainActivity.REQUEST_CODE_STT_LIST_NAME -> {
                    mainActivity.listName.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_CREATE_ITEM -> {
                    mainActivity.listItem.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_READ -> {
                    mainActivity.listRead.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_DELETE -> {
                    mainActivity.listDelete.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT -> {
                    mainActivity.listEdit.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_FIELD -> {
                    mainActivity.listEditField.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_ADD -> {
                    mainActivity.listEditItemAdd.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE -> {
                    mainActivity.listEditItemReplace.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_REPLACE_NEXT -> {
                    mainActivity.listEditItemReplaceNext.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_REMOVE -> {
                    mainActivity.listEditItemRemove.launch(sttIntent)
                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_ITEM_READ -> {

                }
                MainActivity.REQUEST_CODE_STT_LIST_EDIT_NAME -> {
                    mainActivity.listEditName.launch(sttIntent)
                }
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }
}
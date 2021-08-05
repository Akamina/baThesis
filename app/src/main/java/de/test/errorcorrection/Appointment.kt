package de.test.errorcorrection

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.core.app.ActivityCompat.startActivityForResult
import de.test.errorcorrection.MainActivity.Companion.REQUEST_CODE_STT
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.sql.Time
import java.util.*

object Appointment {
    /**
     * This function starts the dialogue to create an appointment
     * @param mainActivity context for functions and SST and TTS
     */
    internal fun createAppointment(mainActivity: MainActivity) {
        var name = askName()
        var date = askDate()
        var time = askTime()
        var location = askLocation()
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

    }

    private fun askName(): String {
        return "foo"
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
package de.test.errorcorrection

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import android.content.ContentUris
import kotlin.properties.Delegates


class Appointment {

    lateinit private var name: String
    lateinit private var date: String
    lateinit private var time: String
    lateinit private var location: String
    private var eventID by Delegates.notNull<Long>()
    private lateinit var field: String

    /**
     * This function creates an event and adds it to the default calendar
     * @param mainActivity context for inserting event
     */
    internal fun createAppointment(mainActivity: MainActivity) {


        println("adding event")

        //Initialize event
        val event = ContentValues()

        //Select default calendar
        event.put(CalendarContract.Events.CALENDAR_ID, 1)
        val tm = getDateTimeFromString(date, time).toInstant(OffsetDateTime.now().offset).toEpochMilli()

        //Add data and details to event
        event.put(CalendarContract.Events.TITLE, name)
        event.put(CalendarContract.Events.DTSTART,tm)
        event.put(CalendarContract.Events.DTEND, tm + 3600000) //Duration is 1 hour
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        event.put(CalendarContract.Events.EVENT_LOCATION, location)

        val baseUri: Uri
        baseUri = if (Build.VERSION.SDK_INT >= 8) {
            Uri.parse("content://com.android.calendar/events")
        } else {
            Uri.parse("content://calendar/events")
        }

        //Insert event into calendar
        mainActivity.contentResolver.insert(baseUri, event)

        println("added event")
        //TODO notify user that the given appointment was created

    }

    /**
     * This function starts the creation dialogue for an appointment
     * @param mainActivity context to call function
     */
    internal fun askName(mainActivity: MainActivity) {
        println("Erstelle Termin")
        val s = "Wie lautet der Name des Termins?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
        Thread.sleep(3000) //do i need this?
    }

    /**
     * This function parses a date and a time from two given strings
     * @param dt String that includes the date
     * @param time String that includes the time
     * @return LocalDateTime
     */
    private fun getDateTimeFromString (dt: String, time: String): LocalDateTime {
        var formatter = DateTimeFormatter.ofPattern("dd.MM yyyy")
        //TODO create method to check time format and fix it if it does not match the pattern
        var t = "0"
        //Checking for a missing 0 in front of the time string, if it is missing, add it
        if (time.split(" ")[0][0] =='0') {
            t += time.split(" ")[0]
        } else {
            t = time.split(" ")[0]
        }
        //Checking for correct length
        if (t.length <= 2) {
            t += ":00"
        }

        var localTime = LocalTime.parse(t)
        var dateTime = LocalDateTime.now()

        //Checking for keywords to determine the correct date and returns it
        if (dt.contains("morgen")) {
            //dateTime = dateTime.withDayOfMonth(dateTime.dayOfMonth + 1)
            dateTime = dateTime.plusDays(1)
            dateTime = dateTime.withHour(localTime.hour)
            dateTime = dateTime.withMinute(localTime.minute)
            dateTime = dateTime.withSecond(0)

            return dateTime
        }
        if (dt.contains("heute")) {
            dateTime = dateTime.withHour(localTime.hour)
            dateTime = dateTime.withMinute(localTime.minute)

            return dateTime
        }
        if (dt.contains("übermorgen")) {
            dateTime = dateTime.plusDays(2)
            dateTime = dateTime.withHour(localTime.hour)
            dateTime = dateTime.withMinute(localTime.minute)

            return dateTime
        }

        var dte = dt
        if (dt.contains("am")) dte = dt.split("am ")[1]
        if (dt.contains("an dem")) dte = dt.split("an dem ")[1]

        if (dte.indexOf('.') <= 1 ) dte = "0$dte"

        //TODO insert missing 0 after first '.'
        println(dte)

        //Normal date was named, parsing it into LocalDateTime

        //TODO dtermine which formatter to use, "dd.MM yyyy" or "dd. monat yyyy"
        var d = LocalDate.parse(dte, formatter)
        dateTime = dateTime.withYear(d.year)
        dateTime = dateTime.withMonth(d.monthValue)
        dateTime = dateTime.withDayOfMonth(d.dayOfMonth)
        dateTime = dateTime.withHour(localTime.hour)
        dateTime = dateTime.withMinute(localTime.minute)

        return dateTime
    }

    /**
     * This function deletes given appointment
     * @param mainActivity Context
     * @param recognizedText Name of the appointment
     */
    internal fun deleteAppointment(mainActivity: MainActivity, recognizedText: String) {
        eventID = listSelectedCalendars(recognizedText, mainActivity)

        var tmp = 0
        if (eventID == tmp.toLong()) mainActivity.askUser("Der Termin $recognizedText konnte nicht gefunden werden.", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)

        var iNumRowsDeleted = 0

        val eventUri = ContentUris
            .withAppendedId(getCalendarUriBase(), eventID)
        iNumRowsDeleted = mainActivity.getContentResolver().delete(eventUri, null, null)

        println("Rows deleted: $iNumRowsDeleted")
        //return iNumRowsDeleted
    }

    /**
     * This function checks if the appointment is valid and starts edit dialogue
     * @param text User input
     * @param mainActivity Context
     */
    internal fun startEdit(text: String, mainActivity: MainActivity) {
        eventID = listSelectedCalendars(text, mainActivity)
        println("Event ID: $eventID")
        var tmp = 0
        if (eventID == tmp.toLong()) {
            mainActivity.askUser("Der Termin $text konnte nicht gefunden werden.", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
        } else {
            //Ask for field
            mainActivity.askUser("Was soll geändert werden?", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD)
        }
    }

    /**
     * This function checks user input for validity and starts error dialogue if input is invalid
     * @param text user input
     * @param mainActivity Context
     */
    internal fun continueEdit(text: String, mainActivity: MainActivity) {
        field = mainActivity.handler.getField(text, mainActivity)
        if (field == "error") {
            mainActivity.askUser("Was soll geändert werden?", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD)
        } else {
            mainActivity.askUser("Wie lautet die Änderung?", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW)
        }
    }

    /**
     * This function performs the changes on the given appointment
     * @param text User input
     * @param mainActivity Context
     */
    internal fun editAppointment(text: String, mainActivity: MainActivity) {
        var event = getCalendarUriBase()
        var newEvent = ContentValues()
        var updateUri = ContentUris.withAppendedId(event, eventID)
        var date: Long
        var iNumRowsUpdated = 0
        //TODO check for valid date/time if invalid ask again or stop
        when (field) {
            "name" -> {
                newEvent.put(CalendarContract.Events.TITLE, text)
                iNumRowsUpdated = mainActivity.contentResolver.update(updateUri, newEvent, null, null)
            }
            "date" -> {
                //get date from event
                date = getDateFromEvent(mainActivity, event)
                //TODO parse input to date
                newEvent.put(CalendarContract.Events.DTSTART, date)
                iNumRowsUpdated = mainActivity.contentResolver.update(updateUri, newEvent, null, null)
            }
            "time" -> {
                //get time from event
                date = getDateFromEvent(mainActivity, event)
                //TODO parse input to time
                newEvent.put(CalendarContract.Events.DTSTART, date)
                iNumRowsUpdated = mainActivity.contentResolver.update(updateUri, newEvent, null, null)
            }
            "location" -> {
                newEvent.put(CalendarContract.Events.EVENT_LOCATION, text)
                iNumRowsUpdated = mainActivity.contentResolver.update(updateUri, newEvent, null, null)

            }
        }
    }

    /**
     * Gets start timestamp from given event and returns the timestamp
     * @param mainActivity Context
     * @param event Get Timestamp from this event
     * @return timestamp
     */
    private fun getDateFromEvent(mainActivity: MainActivity, event: Uri): Long {
        val projection = arrayListOf("_id", "dtstart")
        val cursor: Cursor? = mainActivity.contentResolver.query(event, null, null, null, null)
        var result: Long = 0
        if (cursor!!.moveToFirst()) {
            var calTime: Long
            var calID: Long
            val timeCol : Int = cursor.getColumnIndex(projection[1])
            val idCol: Int = cursor.getColumnIndex(projection[0])

            do {
                calID = cursor.getLong(idCol)
                calTime = cursor.getLong(timeCol)
                if (calID == eventID) {
                    result = calTime
                    println("Zeit: $calTime ID: $calID")
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
        return result
    }

    /**
     * This function creates calendar Uri
     * @return Uri
     */
    private fun getCalendarUriBase(): Uri {
        val eventUri: Uri
        eventUri = if (Build.VERSION.SDK_INT <= 7) {
            // the old way
            Uri.parse("content://calendar/events")
        } else {
            // the new way
            Uri.parse("content://com.android.calendar/events")
        }
        return eventUri
    }

    /**
     * This function starts deletion dialogue
     * @param mainActivity Context
     */
    fun askAppointmentDelete(mainActivity: MainActivity) {
        println("Lösche Termin")
        val s = "Wie lautet der Name des Termins, den du löschen möchtest?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_DELETE_APPOINTMENT)
    }

    /**
     * This function starts edit dialogue
     * @param mainActivity Context
     */
    fun askAppointmentEdit(mainActivity: MainActivity) {
        println("Bearbeite Termin")
        val s = "Wie lautet der Name des Termins, den du bearbeiten möchtest?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT)
    }

    /**
     * This function gives the ID of an appointment
     * @param eventtitle Name of event
     * @param mainActivity Context
     * @return id of given event
     */
    private fun listSelectedCalendars(eventtitle: String, mainActivity: MainActivity): Long {
        val eventUri: Uri
        //TODO maybe call getCalendarUriBase here instead
        eventUri = if (Build.VERSION.SDK_INT <= 7) {
            // old versions
            Uri.parse("content://calendar/events")
        } else {
            // new versions
            Uri.parse("content://com.android.calendar/events")
        }
        var result = 0
        //Create array of id and title
        val projection = arrayOf("_id", "title")
        val cursor: Cursor? = mainActivity.getContentResolver().query(
            eventUri, null, null, null,
            null
        )
        if (cursor!!.moveToFirst()) {
            var calName: String
            var calID: String
            val nameCol: Int = cursor.getColumnIndex(projection[1])
            val idCol: Int = cursor.getColumnIndex(projection[0])
            //Get all events with matching name
            do {
                calName = cursor.getString(nameCol)
                calID = cursor.getString(idCol)
                if (calName != null && calName.contains(eventtitle)) {
                    result = calID.toInt()
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
        return result.toLong()
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
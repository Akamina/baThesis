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
import org.threeten.bp.LocalDateTime.ofEpochSecond


class Appointment {

    lateinit private var name: String
    lateinit private var date: String
    lateinit private var time: String
    lateinit private var location: String
    private var eventID : Long = 0
    internal lateinit var field: String

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
        val tm =
            getDateTimeFromString(date, time).toInstant(OffsetDateTime.now().offset).toEpochMilli()

        //Add data and details to event
        event.put(CalendarContract.Events.TITLE, name)
        event.put(CalendarContract.Events.DTSTART, tm)
        event.put(CalendarContract.Events.DTEND, tm + 3600000) //Duration is 1 hour
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        event.put(CalendarContract.Events.EVENT_LOCATION, location)

        val baseUri = getCalendarUriBase()

        //Insert event into calendar
        mainActivity.contentResolver.insert(baseUri, event)

        println("added event")
        var dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(tm),
            DateTimeUtils.toZoneId(TimeZone.getDefault())
        )
        mainActivity.askUser(
            "Der Termin $name am ${dateTime.dayOfMonth}.${dateTime.month} ${dateTime.year} um ${dateTime.hour}:${dateTime.minute} Uhr mit dem Ort $location wurde erstellt.",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )

    }

    /**
     * This function starts the creation dialogue for an appointment
     * @param mainActivity context to call function
     */
    internal fun askName(mainActivity: MainActivity) {
        println("Erstelle Termin")
        val s = "Wie lautet der Name des Termins?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
        //Thread.sleep(3000) //do i need this?
    }


    /**
     * This function parses a LocalDate from given String
     * @param text User input
     * @return Parsed date from string
     */
    internal fun parseLocalDate(text: String): LocalDate {
        var localDate = LocalDate.now()
        var formatter: DateTimeFormatter
        var dte = text
        //Checking for natural language here
        if (text.contains("übermorgen") || text.contains("Übermorgen")) {
            return localDate.plusDays(2)
        }
        if (text.contains("heute") || text.contains("Heute")) {
            return localDate
        }
        if (text.contains("morgen") || text.contains("Morgen")) {
            return localDate.plusDays(1)
        }
        //Parsing date from string

        if (dte.contains("am")) dte = dte.split("am ")[1]
        if (dte.contains("an dem")) dte = dte.split("an dem ")[1]
        if (dte.indexOf('.') <= 1) dte = "0$dte"

        //Check for spoken out month
        if (containsMonthName(text)) {
            formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.GERMANY)
            localDate = LocalDate.parse(dte, formatter)

        } else {
            formatter = DateTimeFormatter.ofPattern("dd.MM yyyy")
            //split at ' ' then at '.' and if [1].length <=1 add 0
            if (dte.split(" ")[0].split('.')[1].length <= 1) {
                dte = dte.split('.')[0] + "." + "0" + dte.split('.')[1]
            }
            localDate = LocalDate.parse(dte, formatter)
        }

        return localDate
    }

    /**
     * This function checks if the user said the full month (April) or just the matching digit (4)
     * @param text User input
     * @return If the text contains only digits as month false is returned
     */
    private fun containsMonthName(text: String): Boolean {
        return text.contains("Januar") || text.contains("Februar") || text.contains("März") || text.contains(
            "April"
        ) || text.contains("Mai") || text.contains("Juni") || text.contains("Juli") || text.contains(
            "August"
        ) || text.contains("September") || text.contains("Oktober") || text.contains("November") || text.contains(
            "Dezember"
        )
    }


    /**
     * This function parses a string with a valid time into a LocalTime object
     * @param text User input to parse
     * @return parsed LocalTime
     */
    internal fun parseLocalTime(text: String): LocalTime {
        var t = "0"
        var formatter = DateTimeFormatter.ofPattern("HH:mm")
        //Checking for a missing 0 in front of the time string, if it is missing, add it
        if (text.split(" ")[0][0] == '0') {
            t += text.split(" ")[0]
        } else {
            t = text.split(" ")[0]
        }
        //Checking for correct length
        if (t.length <= 2) {
            t += ":00"
        }
        return LocalTime.parse(t, formatter)
    }


    /**
     * This function parses a date and a time from two given strings
     * @param dt String that includes the date
     * @param time String that includes the time
     * @return LocalDateTime
     */
    internal fun getDateTimeFromString(dt: String, time: String): LocalDateTime {
        var localTime = parseLocalTime(time)
        var dateTime = LocalDateTime.now()
        var localDate = parseLocalDate(dt)

        dateTime = dateTime.withYear(localDate.year)
        dateTime = dateTime.withMonth(localDate.monthValue)
        dateTime = dateTime.withDayOfMonth(localDate.dayOfMonth)
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
        if (eventID == tmp.toLong()) {
            mainActivity.askUser(
                "Der Termin $recognizedText konnte nicht gefunden werden.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
            return
        }

        var iNumRowsDeleted = 0

        val eventUri = ContentUris
            .withAppendedId(getCalendarUriBase(), eventID)
        iNumRowsDeleted = mainActivity.getContentResolver().delete(eventUri, null, null)

        println("Rows deleted: $iNumRowsDeleted")
        //return iNumRowsDeleted
        mainActivity.askUser(
            "Der Termin $recognizedText wurde entfernt",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
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
            mainActivity.askUser(
                "Der Termin $text konnte nicht gefunden werden.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            //Ask for field
            mainActivity.askUser(
                "Was soll geändert werden?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD
            )
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
            mainActivity.askUser(
                "Was soll geändert werden?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD
            )
        } else if (field == "read") {
            readAppointmentEdit(mainActivity)
        } else {
            mainActivity.askUser(
                "Wie lautet die Änderung?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW
            )
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
        var localDateTime: LocalDateTime
        when (field) {
            "name" -> {
                newEvent.put(CalendarContract.Events.TITLE, text)
                iNumRowsUpdated =
                    mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "Ich habe den Namen auf $text geändert. Möchtest du noch etwas ändern?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )
            }
            "date" -> {
                //get date from event
                date = getDateFromEvent(mainActivity, event)
                //Calculate into LocalDateTime from millis
                localDateTime = ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localDate = parseLocalDate(text)
                //Update date
                localDateTime = localDateTime.withYear(localDate.year)
                localDateTime = localDateTime.withMonth(localDate.monthValue)
                localDateTime = localDateTime.withDayOfMonth(localDate.dayOfMonth)

                //TODO add time from date here to localDateTime

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                iNumRowsUpdated =
                    mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "Ich habe das Datum auf $localDate geändert. Möchtest du noch etwas ändern?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )

            }
            "time" -> {
                //TODO fix bug where date get set to 1970
                //get time from event
                date = getDateFromEvent(mainActivity, event)


                //Calculate into LocalDateTime from Millis
                localDateTime = ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localTime = parseLocalTime(text)
                //Update time
                localDateTime = localDateTime.withHour(localTime.hour)
                localDateTime = localDateTime.withMinute(localTime.minute)

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                iNumRowsUpdated =
                    mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "Ich habe die Zeit auf $localTime geändert. Möchtest du noch etwas ändern?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )

            }
            "location" -> {
                newEvent.put(CalendarContract.Events.EVENT_LOCATION, text)
                iNumRowsUpdated =
                    mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "Ich habe den Ort auf $text geändert. Möchtest du noch etwas ändern?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )


            }
            else -> {

            }
            //TODO bug detected: date was messed up somehow during changing location
        }
    }

    /**
     * Gets start timestamp from given event and returns the timestamp
     * @param mainActivity Context
     * @param event Get Timestamp from this event
     * @return timestamp
     */
    internal fun getDateFromEvent(mainActivity: MainActivity, event: Uri): Long {
        val projection = arrayListOf("_id", "dtstart")
        val cursor: Cursor? = mainActivity.contentResolver.query(event, null, null, null, null)
        var result: Long = 0
        if (cursor!!.moveToFirst()) {
            var calTime: Long
            var calID: Long
            val timeCol: Int = cursor.getColumnIndex(projection[1])
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
    internal fun getCalendarUriBase(): Uri {
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
        val s = "Wie lautet der Name des Termins den du bearbeiten möchtest?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT)
    }

    /**
     * This function gives the ID of an appointment
     * @param eventtitle Name of event
     * @param mainActivity Context
     * @return id of given event
     */
    internal fun listSelectedCalendars(eventtitle: String, mainActivity: MainActivity): Long {
        val eventUri: Uri = getCalendarUriBase()
        var result: Long = 0
        //Create array of id and title
        val projection = arrayOf("_id", "title")
        val cursor: Cursor? = mainActivity.contentResolver.query(
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
                    result = calID.toLong()
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
        return result
    }

    /**
     * Usage during edit dialogue.
     * This function checks if an event is already set and reads it out. If no event is set it starts a dialogue to ask for an event.
     * @param mainActivity Context
     */
    internal fun readAppointmentEdit(mainActivity: MainActivity) {
        println("Termin vorlesen beim bearbeiten")
        if (eventID == 0.toLong()) {
            mainActivity.askUser("Wie lautet der Name des Termins den ich vorlesen soll?", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_READ)
        } else {
            val eventUri: Uri = getCalendarUriBase()
            //Create array of id, title, start time and location
            val projection = arrayOf("_id", "title", "dtstart", "eventLocation")
            val cursor: Cursor? = mainActivity.contentResolver.query(
                eventUri, null, null, null,
                null
            )
            if (cursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var calDate: String
                var calLocation: String
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])
                val locCol: Int = cursor.getColumnIndex(projection[3])

                //Get all events with matching name
                do {
                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)
                    calLocation = cursor.getString(locCol)

                    if (calID != null && calID.toLong() == eventID) {
                        break
                        //result = calID.toLong()
                    }
                } while (cursor.moveToNext())
                cursor.close()
                val localDate =
                    ofEpochSecond(calDate.toLong() / 1000, 0, OffsetDateTime.now().offset)
                //mainActivity.askUser("Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate().dayOfMonth}. ${localDate.toLocalDate().month} ${localDate.toLocalDate().year} um ${localDate.toLocalTime().hour}:${localDate.toLocalTime().minute} Uhr $calLocation statt", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD)
                mainActivity.askUser(
                    "Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate()} um ${localDate.toLocalTime()} $calLocation statt. Möchtest du noch etwas ändern?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD
                )
            }
        }
    }

    /**
     * This function checks if an event is already set and reads it out. If no event is set it starts a dialogue to ask for an event.
     * @param mainActivity Context
     */
    fun readAppointment(mainActivity: MainActivity, text: String) {
        if (eventID == 0.toLong()) {
            mainActivity.askUser("Der Termin $text konnte nicht gefunden werden", mainActivity, MainActivity.REQUEST_CODE_STT_NOTIFY)
            //mainActivity.askUser("Wie lautet der Name des Termins den ich vorlesen soll?", mainActivity, MainActivity.REQUEST_CODE_STT_READ_APPOINTMENT_NO_NAME)
        //mainActivity.waitForTTS(mainActivity)
        } else {
            val eventUri: Uri = getCalendarUriBase()
            //Create array of id, title, start time and location
            val projection = arrayOf("_id", "title", "dtstart", "eventLocation")
            val cursor: Cursor? = mainActivity.contentResolver.query(
                eventUri, null, null, null,
                null
            )
            if (cursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var calDate: String
                var calLocation: String
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])
                val locCol: Int = cursor.getColumnIndex(projection[3])

                //Get all events with matching name
                do {
                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)
                    calLocation = cursor.getString(locCol)

                    if (calID != null && calID.toLong() == eventID) {
                        break
                        //result = calID.toLong()
                    }
                } while (cursor.moveToNext())
                cursor.close()
                val localDate =
                    ofEpochSecond(calDate.toLong() / 1000, 0, OffsetDateTime.now().offset)
                //mainActivity.askUser("Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate().dayOfMonth}. ${localDate.toLocalDate().month} ${localDate.toLocalDate().year} um ${localDate.toLocalTime().hour}:${localDate.toLocalTime().minute} Uhr $calLocation statt", mainActivity, MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD)
                mainActivity.askUser(
                    "Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate()} um ${localDate.toLocalTime()} $calLocation statt.",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_NOTIFY
                )
            }
        }
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

    internal fun setEvent(id: Long) {
        this.eventID = id
    }
}
package de.test.errorcorrection

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import android.content.ContentUris
import org.threeten.bp.LocalDateTime.ofEpochSecond
import org.threeten.bp.temporal.ChronoUnit
import kotlin.random.Random


class Appointment {

    private var name: String = ""
    private var date: String = ""
    private var time: String = ""
    private var location: String = ""
    private var eventID: Long = 0
    private var field: String = ""
    private val errorName = mutableListOf("lunch", "dinner", "dentist", "doctor")
    private val errorDate =
        mutableListOf("20th of October 2021")//LocalDate.of(2020, 10, 20), LocalDate.of(2021, 10, 2))
    private val errorTime =
        mutableListOf("11:30 p.m.")//LocalTime.of(12, 45), LocalTime.of(11, 33), LocalTime.of(15, 5), LocalTime.of(22, 45))
    private val errorLocation = mutableListOf("Hamburg", "Berlin", "Munich", "Kassel")

    /**
     * This function creates an event and adds it to the default calendar
     * @param mainActivity context for inserting event
     */
    internal fun createAppointment(mainActivity: MainActivity) {

        //TODO read out appointment in the end and ask if everything is correct
        println("adding event")

        //Initialize event
        val event = ContentValues()

        //Select default calendar
        event.put(CalendarContract.Events.CALENDAR_ID, 1)
        var dateTime = getDateTimeFromString(date, time)

        val tm =
            dateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()

        //Add data and details to event
        event.put(CalendarContract.Events.TITLE, name)
        event.put(CalendarContract.Events.DTSTART, tm)
        event.put(CalendarContract.Events.DTEND, tm + 3600000) //Duration is 1 hour
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        event.put(CalendarContract.Events.EVENT_LOCATION, location)
        val baseUri = getCalendarUriBase()

        //Insert event into calendar
        try {
            mainActivity.contentResolver.insert(baseUri, event)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        println("added event")
        //Maybe redundant
        dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(tm),
            DateTimeUtils.toZoneId(TimeZone.getDefault())
        )
        mainActivity.askUser(
            //"Der Termin $name am ${dateTime.dayOfMonth}.${dateTime.month} ${dateTime.year} um ${dateTime.hour}:${dateTime.minute} Uhr mit dem Ort $location wurde erstellt.",
            /*          "Der Termin $name am ${dateTime.dayOfMonth}.${dateTime.month} ${dateTime.year} um ${
                          dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
                      } mit dem Ort $location wurde erstellt.",
          */
            "The appointment $name on ${dateTime.dayOfMonth}. ${dateTime.month} ${dateTime.year} at ${
                dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
                //dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
            } with the location $location was created.",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        resetParameters()

    }

    /**
     * TODO
     *
     */
    internal fun addError() {
        //Get random error here
        var rng = Random
        var index = rng.nextInt(4)
        when (index) {
            0 -> {
                name = errorName[rng.nextInt(errorName.size)]
            }
            1 -> {
                date = errorDate[rng.nextInt(errorDate.size)]
            }
            2 -> {
                time = errorTime[rng.nextInt(errorTime.size)]
            }
            3 -> {
                location = errorLocation[rng.nextInt(errorLocation.size)]
            }
        }
    }

    /**
     * TODO
     *
     * @param mainActivity
     */
    internal fun readData(mainActivity: MainActivity) {
        mainActivity.askUser(
            "The name is $name, the date is $date, the time is $time and the location is $location. Do you wish to edit something?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END
        )
    }

    /**
     * TODO
     *
     */
    private fun resetParameters() {
        this.location = ""
        this.name = ""
        this.date = ""
        this.time = ""
    }

    /**
     * This function checks for valid date and time formats
     * @param text User input
     */
    internal fun checkDateAndTimeValidity(text: String) {
        if (this.field == "date") {
            parseLocalDate(text)
        }
        if (this.field == "time") {
            parseLocalTime(text)
        }
    }

    /**
     * TODO
     *
     * @param mainActivity
     * @param text
     */
    internal fun updateParameter(mainActivity: MainActivity, text: String) {
        when (field) {
            "name" -> {
                this.name = text
            }
            "date" -> {
                this.date = text
            }
            "time" -> {
                this.time = text
            }
            "location" -> {
                this.location = text
            }
            "read" -> {
                //TODO fill me
                readData(mainActivity)
                return
            }
        }
        if (this.date == "") {
            mainActivity.askUser(
                "What date is the appointment?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_DATE
            )
            return
        }
        if (this.time == "") {
            mainActivity.askUser(
                "What time is the appointment?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_TIME
            )
            return
        }
        if (this.location == "") {
            mainActivity.askUser(
                "Where does the appointment take place?", mainActivity,
                MainActivity.REQUEST_CODE_STT_LOCATION
            )
            return
        }
        //TODO add all fields set -> get to creation again
        readData(mainActivity)

    }

    /**
     * This function starts the creation dialogue for an appointment
     * @param mainActivity context to call function
     */
    internal fun askName(mainActivity: MainActivity) {
        println("Erstelle Termin")
        //val s = "Wie lautet der Name des Termins?"
        val s = "What is the name of the appointment?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
    }


    /**
     * This function parses a LocalDate from given String
     * @param text User input
     * @return Parsed date from string
     */
    internal fun parseLocalDate(text: String): LocalDate {
        var localDate = LocalDate.now()
        var formatter: DateTimeFormatter
        var dte = text.replace("(^)(on the |on)".toRegex(), "")
        println(dte)
        //Checking for natural language here
        if (text.contains("übermorgen") || text.contains("Übermorgen") || text.contains("the day after tomorrow")) {
            return localDate.plusDays(2)
        }
        if (text.contains("heute") || text.contains("Heute") || text.contains("today")) {
            return localDate
        }
        if (text.contains("morgen") || text.contains("Morgen") || text.contains("tomorrow")) {
            return localDate.plusDays(1)
        }
        //Parsing date from string

        /*
        if (dte.contains("am")) dte = dte.split("am ")[1]
        if (dte.contains("an dem")) dte = dte.split("an dem ")[1]
        if (dte.indexOf('.') <= 1) dte = "0$dte"


         */
        //Check for spoken out month
        if (containsMonthName(text)) {
            //formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy", Locale.GERMAN)
            formatter = if (text.contains("of")) {
                dte = dte.replace("of ", "")
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
            } else {
                DateTimeFormatter.ofPattern("MMMM dd yyyy", Locale.ENGLISH)
            }
            dte = dte.replace("(?<=\\d)(st|nd|rd|th)".toRegex(), "")
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
        return text.contains("January") || text.contains("February") || text.contains("March") || text.contains(
            "April"
        ) || text.contains("May") || text.contains("June") || text.contains("July") || text.contains(
            "August"
        ) || text.contains("September") || text.contains("October") || text.contains("November") || text.contains(
            "December"
        )
    }


    /**
     * This function parses a string with a valid time into a LocalTime object
     * @param text User input to parse
     * @return parsed LocalTime
     */
    internal fun parseLocalTime(text: String): LocalTime {
        //Check for eg. 8 p.m.
        println(text)
        var t = text.replace("at ", "")
        if (!t.contains(":")) {
            t = t.split(" ")[0] + ":00 " + t.split(" ")[1]
        }
        //var formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        var formatter = //DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
            //var t = "0"
            if (t.split(":")[0].length < 2) {
                DateTimeFormatter.ofPattern("h:mm a", Locale.US)
                //t += text
            } else DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        // t = text
        /*
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
        } */
        return LocalTime.parse(t.replace("a.m.", "AM").replace("p.m.", "PM"), formatter)
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
                //"Der Termin $recognizedText konnte nicht gefunden werden.",
                "The appointment $recognizedText could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
            return
        }

        var iNumRowsDeleted: Int

        val eventUri = ContentUris
            .withAppendedId(getCalendarUriBase(), eventID)
        iNumRowsDeleted = mainActivity.contentResolver.delete(eventUri, null, null)

        println("Rows deleted: $iNumRowsDeleted")
        mainActivity.askUser(
            //"Der Termin $recognizedText wurde entfernt",
            "The appointment $recognizedText was removed",
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
                //"Der Termin $text konnte nicht gefunden werden.",
                "The appointment $text could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            //Ask for field
            mainActivity.askUser(
                //"Was soll geändert werden?",
                "What should be changed?",
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
        when (field) {
            "error" -> {
                mainActivity.askUser(
                    //"Was soll geändert werden?",
                    "What should be changed?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD
                )
            }
            "read" -> {
                readAppointmentEdit(mainActivity)
            }
            else -> {
                mainActivity.askUser(
                    //"Wie lautet die Änderung?",
                    "What's the change?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_NEW
                )
            }
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
        var localDateTime: LocalDateTime
        when (field) {
            "name" -> {
                newEvent.put(CalendarContract.Events.TITLE, text)
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe den Namen auf $text geändert. Möchtest du noch etwas ändern?",
                    "I changed the name to $text. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )
            }
            "date" -> {
                //get date from event
                date = getDateFromEvent(mainActivity, event)
                println("Millis: $date")
                //Calculate into LocalDateTime from millis
                localDateTime = ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                println(localDateTime)
                var localDate = parseLocalDate(text)
                println("Neues Date: $localDate")
                //Update date
                localDateTime = localDateTime.withYear(localDate.year)
                localDateTime = localDateTime.withMonth(localDate.monthValue)
                localDateTime = localDateTime.withDayOfMonth(localDate.dayOfMonth)

                println("Updated date $localDateTime")
                println(localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli())
                //TODO add time from date here to localDateTime

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe das Datum auf $localDate geändert. Möchtest du noch etwas ändern?",
                    "I changed the date to $localDate. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )

            }
            "time" -> {
                //TODO fix bug where date get set to 1970
                //get time from event
                date = getDateFromEvent(mainActivity, event)
                println("Millis: $date")

                //Calculate into LocalDateTime from Millis
                localDateTime = ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localTime = parseLocalTime(text)
                println(localDateTime)
                println("Neues Zeit: $localTime")

                //Update time
                localDateTime = localDateTime.withHour(localTime.hour)
                localDateTime = localDateTime.withMinute(localTime.minute)
                println("Updated date $localDateTime")
                println(localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli())

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe die Zeit auf $localTime geändert. Möchtest du noch etwas ändern?",
                    "I changed the time to $localTime. Would you like to change anything?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )

            }
            "location" -> {
                newEvent.put(CalendarContract.Events.EVENT_LOCATION, text)
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    //"Ich habe den Ort auf $text geändert. Möchtest du noch etwas ändern?",
                    "I changed the location to $text. Would you like to change anything?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )


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
        return Uri.parse("content://com.android.calendar/events")
    }

    /**
     * This function starts deletion dialogue
     * @param mainActivity Context
     */
    fun askAppointmentDelete(mainActivity: MainActivity) {
        println("Lösche Termin")
        //val s = "Wie lautet der Name des Termins, den du löschen möchtest?"
        val s = "What is the name of the appointment that you want to delete?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_DELETE_APPOINTMENT)
    }

    /**
     * This function starts edit dialogue
     * @param mainActivity Context
     */
    fun askAppointmentEdit(mainActivity: MainActivity) {
        println("Bearbeite Termin")
        //val s = "Wie lautet der Name des Termins den du bearbeiten möchtest?"
        val s = "What is the name of the appointment you want to edit?"
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
            mainActivity.askUser(
                //"Wie lautet der Name des Termins den ich vorlesen soll?",
                "What is the name of the appointment I should read out?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_READ
            )
        } else {
            //TODO in externe funktion schreiben
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
                    //TODO write this into global variables so it can be moved into a seperate function

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
                    //"Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate()} um ${localDate.toLocalTime()} $calLocation statt. Möchtest du noch etwas ändern?",
                    "The name of the appointment is $calName and it will take place on ${localDate.toLocalDate()} at ${localDate.toLocalTime()} $calLocation. Would you like to change something?",
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
            mainActivity.askUser(
                //"Der Termin $text konnte nicht gefunden werden",
                "The appointment $text could not be found",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            //TODO in externe funktion schreiben
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
                    //TODO write this into global variables so it can be moved into a seperate function
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
                    //"Der Name des Termins lautet $calName und er findet am ${localDate.toLocalDate()} um ${localDate.toLocalTime()} $calLocation statt.",
                    "The name of the appointment is $calName and it will take place on ${localDate.toLocalDate()} at ${localDate.toLocalTime()} $calLocation.",
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

    internal fun setField(text: String) {
        this.field = text
    }

    internal fun getField(): String {
        return this.field
    }
}
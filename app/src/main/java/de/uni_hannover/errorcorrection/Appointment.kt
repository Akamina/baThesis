package de.uni_hannover.errorcorrection

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
    private val errorDate = mutableListOf("20th of October 2021")
    private val errorTime = mutableListOf("11:30 p.m.")
    private val errorLocation = mutableListOf("Hamburg", "Berlin", "Munich", "Kassel")

    /**
     * This function creates an event and adds it to the default calendar
     * @param mainActivity context for inserting event
     */
    @Throws(Exception::class)
    internal fun createAppointment(mainActivity: MainActivity) {


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
        mainActivity.logger.writeLog(
            "Added appointment $name on ${dateTime.dayOfMonth}. ${dateTime.month} ${dateTime.year} at ${
                dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
            } with the location $location", 0, mainActivity
        )

        //Maybe redundant
        dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(tm),
            DateTimeUtils.toZoneId(TimeZone.getDefault())
        )
        //notify user about added event via tts
        mainActivity.askUser(
            "The appointment $name on ${dateTime.dayOfMonth}. ${dateTime.month} ${dateTime.year} at ${
                dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
            } with the location $location was created.",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        resetParameters()
    }

    /**
     * This funcation adds a random error to the appointment
     */
    internal fun addError() {
        //Get random error here
        var rng = Random
        var index = rng.nextInt(4)
        //Set random error
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
     * This function reads the gathered data to the user
     * @param mainActivity Context
     */
    internal fun readData(mainActivity: MainActivity) {
        //read data via tts
        mainActivity.askUser(
            "The name is $name, the date is $date, the time is $time and the location is $location. Do you wish to edit something?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END
        )
    }

    /**
     * This function resets the parameters for appointments
     */
    private fun resetParameters() {
        this.location = ""
        this.name = ""
        this.date = ""
        this.time = ""
    }

    /**
     * This function checks for valid date and time formats for appointments
     * @param text User input
     */
    internal fun checkDateAndTimeValidity(text: String) {
        //check for field and if field matches try to parse input
        if (this.field == "date") {
            parseLocalDate(text)
        }
        if (this.field == "time") {
            parseLocalTime(text)
        }
    }

    /**
     * This function updates the parameters for this appointment and continues the dialogue
     * @param mainActivity Context
     * @param text User input
     */
    internal fun updateParameter(mainActivity: MainActivity, text: String) {
        //update field
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
                readData(mainActivity)
                return
            }
        }
        //get field to continue working on after editing previous field
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
        //all fields are set, edit right before creation is finished here
        mainActivity.askUser(
            "I have updated $field to $text. Do you wish to edit something else?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END
        )
    }

    /**
     * This function starts the creation dialogue for an appointment
     * @param mainActivity context to call function
     */
    internal fun askName(mainActivity: MainActivity) {
        val s = "What is the name of the appointment?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_NAME)
    }


    /**
     * This function calculates the shift of week days when the user says e.g. "monday" as date and updates the date object
     * @param start Index of current day
     * @param text User input
     * @param date Date that will be updated
     * @return Updated LocalDate object
     */
    private fun getWeekDayShift(start: Int, text: String, date: LocalDate): LocalDate? {
        var dt = date
        val tmp: Int
        //target day - current day, if value is < 0 add 6, if value = 0 add 7, else add result
        //set value according to day
        when {
            text.lowercase().contains("monday") -> {
                tmp = 0
            }
            text.lowercase().contains("tuesday") -> {
                tmp = 1
            }
            text.lowercase().contains("wednesday") -> {
                tmp = 2
            }
            text.lowercase().contains("thursday") -> {
                tmp = 3
            }
            text.lowercase().contains("friday") -> {
                tmp = 4
            }
            text.lowercase().contains("saturday") -> {
                tmp = 5
            }
            text.lowercase().contains("sunday") -> {
                tmp = 6
            }
            else -> {
                tmp = -1
            }
        }
        //invalid day
        if (tmp == -1) {
            return null
        }
        //calculate shift
        val result = tmp - start
        return if (result < 0) dt.plusDays(6) else if (result == 0) dt.plusDays(7) else dt.plusDays(
            result.toLong()
        )
    }

    /**
     * This function checks if the name of a day was used
     * @param text User input
     * @return
     */
    private fun containsWeekDay(text: String): Boolean {
        return text.lowercase().contains("monday") || text.lowercase()
            .contains("tuesday") || text.lowercase().contains("wednesday") || text.lowercase()
            .contains("thursday") || text.lowercase().contains("friday") || text.lowercase()
            .contains("saturday") || text.lowercase().contains("sunday")
    }

    /**
     * This function parses a LocalDate from given String
     * @param text User input
     * @return Parsed date from string
     */
    internal fun parseLocalDate(text: String): LocalDate {
        var localDate = LocalDate.now()
        var formatter: DateTimeFormatter
        //remove unwanted snippets
        var dte = text.replace("(^)(on the |on)".toRegex(), "")
        //Checking and handling of natural language here
        if (containsWeekDay(text)) {
            when (localDate.dayOfWeek.toString()) {
                "MONDAY" -> {
                    return getWeekDayShift(0, text, localDate)!!
                }
                "TUESDAY" -> {
                    return getWeekDayShift(1, text, localDate)!!
                }
                "WEDNESDAY" -> {
                    return getWeekDayShift(2, text, localDate)!!
                }
                "THURSDAY" -> {
                    return getWeekDayShift(3, text, localDate)!!
                }
                "FRIDAY" -> {
                    return getWeekDayShift(4, text, localDate)!!
                }
                "SATURDAY" -> {
                    return getWeekDayShift(5, text, localDate)!!
                }
                "SUNDAY" -> {
                    return getWeekDayShift(6, text, localDate)!!
                }
            }
        }
        //check for phrases instead of date
        if (text.contains("übermorgen") || text.contains("Übermorgen") || text.contains("the day after tomorrow")) {
            return localDate.plusDays(2)
        }
        if (text.contains("heute") || text.contains("Heute") || text.contains("today")) {
            return localDate
        }
        if (text.contains("morgen") || text.contains("Morgen") || text.contains("tomorrow")) {
            return localDate.plusDays(1)
        }
        //Check for spoken out month
        if (containsMonthName(text)) {
            //set formatter according to given string
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
        var t = text.replace("at ", "")
        //build valid string here, prepending 0 if it is missing
        if (!t.contains(":")) {
            t = t.split(" ")[0] + ":00 " + t.split(" ")[1]
        }
        //build formatter according to given string
        var formatter =
            if (t.split(":")[0].length < 2) {
                DateTimeFormatter.ofPattern("h:mm a", Locale.US)
            } else DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
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

        //updating date and time
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
        //looking for given event
        try {
            eventID = listSelectedCalendars(recognizedText, mainActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var tmp = 0
        if (eventID == tmp.toLong()) {
            mainActivity.askUser(
                "The appointment $recognizedText could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
            return
        }

        var iNumRowsDeleted: Int

        val eventUri = ContentUris
            .withAppendedId(getCalendarUriBase(), eventID)
        try {
            iNumRowsDeleted = mainActivity.contentResolver.delete(eventUri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
            //debugging
            mainActivity.askUser(
                "I have catched an exception",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        }
        //Notify user via tts about deleted event
        mainActivity.askUser(
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
        var tmp = 0
        if (eventID == tmp.toLong()) {
            //Notify user that no appointment exists with given name
            mainActivity.askUser(
                "The appointment $text could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            //Ask for field
            mainActivity.askUser(
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
                //ask user for field that has to be changed
                mainActivity.askUser(
                    "What should be changed?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_FIELD
                )
            }
            "read" -> {
                readAppointmentEdit(mainActivity)
            }
            else -> {
                //ask for update
                mainActivity.askUser(
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
                //update name
                newEvent.put(CalendarContract.Events.TITLE, text)
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
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
                //pack new data
                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                //update event
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "I changed the date to $localDate. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )

            }
            "time" -> {
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
                //pack new data
                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                //update event
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "I changed the time to $localTime. Would you like to change anything?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )

            }
            "location" -> {
                newEvent.put(CalendarContract.Events.EVENT_LOCATION, text)
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "I changed the location to $text. Would you like to change anything?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_ASK
                )


            }
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
            //iterate over events and look for the correct one
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
        val s = "What is the name of the appointment that you want to delete?"
        mainActivity.askUser(s, mainActivity, MainActivity.REQUEST_CODE_STT_DELETE_APPOINTMENT)
    }

    /**
     * This function starts edit dialogue
     * @param mainActivity Context
     */
    fun askAppointmentEdit(mainActivity: MainActivity) {
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
        //check for valid event id
        if (eventID == 0.toLong()) {
            mainActivity.askUser(
                "What is the name of the appointment I should read out?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_EDIT_APPOINTMENT_READ
            )
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
                var calLocation: String = ""
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])
                val locCol: Int = cursor.getColumnIndex(projection[3])

                //Get all events with matching name
                do {
                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)

                    if (calID != null && calID.toLong() == eventID) {
                        calLocation = cursor.getString(locCol)
                        break
                    }
                } while (cursor.moveToNext())
                cursor.close()

                val localDate =
                    ofEpochSecond(calDate.toLong() / 1000, 0, OffsetDateTime.now().offset)
                mainActivity.askUser(
                    "The name of the appointment is $calName and it will take place on ${localDate.toLocalDate()} at ${
                        localDate.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
                    } $calLocation. Would you like to change something?",
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
        //check for valid event id
        if (eventID == 0.toLong()) {
            mainActivity.askUser(
                "The appointment $text could not be found",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
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
                var calLocation: String = ""
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])
                val locCol: Int = cursor.getColumnIndex(projection[3])

                //Get all events with matching name
                do {
                    calID = cursor.getString(idCol)
                    calName = cursor.getString(nameCol)
                    calDate = cursor.getString(dateCol)
                    if (calID != null && calID.toLong() == eventID) {
                        calLocation = cursor!!.getString(locCol)
                        break
                    }
                } while (cursor.moveToNext())
                cursor.close()

                val localDate: LocalDateTime =
                    ofEpochSecond(calDate.toLong() / 1000, 0, OffsetDateTime.now().offset)
                mainActivity.askUser(
                    "The name of the appointment is $calName and it will take place on ${localDate.toLocalDate()} at ${
                        localDate.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
                    } $calLocation.",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_NOTIFY
                )
            }
        }
    }

    /**
     * Setter function for name
     * @param name Name that will be set in this object
     */
    internal fun setName(name: String) {
        this.name = name
    }

    /**
     * Setter function for date
     * @param date Date that will bet set in this object
     */
    internal fun setDate(date: String) {
        this.date = date
    }

    /**
     * Setter function for time
     * @param time time that will be ste in this object
     */
    internal fun setTime(time: String) {
        this.time = time
    }

    /**
     * Setter function for location
     * @param location Location that will be set in this object
     */
    internal fun setLocation(location: String) {
        this.location = location
    }

    /**
     * Setter function for eventId
     * @param id EventId that will be set in this object
     */
    internal fun setEvent(id: Long) {
        this.eventID = id
    }

    /**
     * Setter function for current field
     * @param text Name of the field that is currently worked on
     */
    internal fun setField(text: String) {
        this.field = text
    }

    /**
     * Getter function for current field
     * @return Field that is currently worked on
     */
    internal fun getField(): String {
        return this.field
    }
}
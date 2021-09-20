package de.uni_hannover.errorcorrection

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import java.util.*
import android.provider.CalendarContract.Reminders
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import kotlin.random.Random


class Reminder {
    private var reminderName: String = ""
    private var reminderDate: String = ""
    private var reminderTime: String = ""
    private var field: String = ""
    private var reminderID: Long = 0
    private val errorName = mutableListOf("lunch", "dinner", "dentist", "doctor")
    private val errorDate = mutableListOf("20th of October 2021")
    private val errorTime = mutableListOf("5 a.m.")


    /**
     * This function creates an reminder event in the calendar and sets up the reminder
     * @param mainActivity Context
     */
    @Throws(Exception::class)
    internal fun createReminder(mainActivity: MainActivity) {
        println("adding reminder")

        //Initialize event
        val event = ContentValues()
        //Select default calendar
        event.put(CalendarContract.Events.CALENDAR_ID, 1)

        var dateTime: LocalDateTime

        val tm =
            mainActivity.appntmnt.getDateTimeFromString(reminderDate, reminderTime)
                .toInstant(OffsetDateTime.now().offset).toEpochMilli()

        //Add data and details to event
        event.put(CalendarContract.Events.TITLE, reminderName)
        event.put(CalendarContract.Events.DTSTART, tm)
        event.put(CalendarContract.Events.DTEND, tm + 3600000) //Duration is 1 hour
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        event.put(CalendarContract.Events.HAS_ALARM, 1)

        println(event)
        val baseUri = mainActivity.appntmnt.getCalendarUriBase()

        //Insert event into calendar
        mainActivity.contentResolver.insert(baseUri, event)

        reminderID = mainActivity.appntmnt.listSelectedCalendars(reminderName, mainActivity)

        //Maybe redundant
        dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(tm),
            DateTimeUtils.toZoneId(TimeZone.getDefault())
        )
        //Set reminder for event
        var values = ContentValues().apply {
            put(Reminders.MINUTES, 0)
            put(Reminders.EVENT_ID, reminderID)
            put(Reminders.METHOD, Reminders.METHOD_ALERT)

        }
        //Add reminder to event
        mainActivity.contentResolver.insert(Reminders.CONTENT_URI, values)

        mainActivity.logger.writeLog("Added reminder $reminderName on ${dateTime.dayOfMonth}. ${dateTime.month} ${dateTime.year} at ${
            dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
        }", 0, mainActivity)

        //Notify user about creation
        mainActivity.askUser(
            "The reminder $reminderName on ${dateTime.dayOfMonth}. ${dateTime.month} ${dateTime.year} at ${
                dateTime.toLocalTime().truncatedTo(ChronoUnit.MINUTES)
            } was created.",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
        resetParameters()
    }

    /**
     * This function checks if the reminder exists and if it exists, this function deletes it.
     * @param mainActivity Context
     * @param recognizedText Name of the reminder that has to be deleted
     */
    fun deleteReminder(mainActivity: MainActivity, recognizedText: String) {
        var eventID = mainActivity.appntmnt.listSelectedCalendars(recognizedText, mainActivity)

        var tmp = 0
        //Check for valid eventID
        if (eventID == tmp.toLong()) {
            mainActivity.askUser(
                "The $recognizedText reminder could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
            return
        }

        var iNumRowsDeleted: Int

        val eventUri = ContentUris
            .withAppendedId(mainActivity.appntmnt.getCalendarUriBase(), eventID)
        iNumRowsDeleted = mainActivity.contentResolver.delete(eventUri, null, null)

        //notify user via tts about deleted reminder
        mainActivity.askUser(
            "The $recognizedText reminder has been removed",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_NOTIFY
        )
    }

    /**
     * This function checks if the reminder exists and reads it out.
     * @param mainActivity Context
     * @param text Name of the reminder
     */
    fun readReminder(mainActivity: MainActivity, text: String) {
        if (reminderID == 0.toLong()) {
            mainActivity.askUser(
                "The reminder $text could not be found",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            val eventUri: Uri = mainActivity.appntmnt.getCalendarUriBase()
            //Create array of id, title, start time and location
            val projection = arrayOf("_id", "title", "dtstart")
            val cursor: Cursor? = mainActivity.contentResolver.query(
                eventUri, null, null, null,
                null
            )
            if (cursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var calDate: String
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])

                //Get all events with matching name
                do {
                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)

                    if (calID != null && calID.toLong() == reminderID) {
                        break
                    }
                } while (cursor.moveToNext())
                cursor.close()
                val localDate =
                    LocalDateTime.ofEpochSecond(
                        calDate.toLong() / 1000,
                        0,
                        OffsetDateTime.now().offset
                    )
                //read found reminder
                mainActivity.askUser(
                    "I remind you of ${localDate.toLocalDate()} at ${localDate.toLocalTime().truncatedTo(ChronoUnit.MINUTES)} of $calName.",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_NOTIFY
                )
            }
        }
    }

    /**
     * This function checks if the reminder exists and if it exists, this function continues the dialogue.
     * @param recognizedText Name of the reminder
     * @param mainActivity Context
     */
    fun startEdit(recognizedText: String, mainActivity: MainActivity) {
        reminderID = mainActivity.appntmnt.listSelectedCalendars(recognizedText, mainActivity)
        println("Event ID: $reminderID")
        var tmp = 0
        if (reminderID == tmp.toLong()) {
            mainActivity.askUser(
                "The $recognizedText reminder could not be found.",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_NOTIFY
            )
        } else {
            //Ask for field
            mainActivity.askUser(
                "What should be changed?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD
            )
        }
    }

    /**
     * This function determines if the reminder has to be edited or read out
     * @param recognizedText Field that has to be edited/ read the reminder
     * @param mainActivity Context
     */
    fun continueEdit(recognizedText: String, mainActivity: MainActivity) {
        field = mainActivity.handler.getField(recognizedText, mainActivity)
        when (field) {
            "error" -> {
                //invalid user input
                mainActivity.askUser(
                    "What should be changed?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD
                )
            }
            "read" -> {
                readReminderEdit(mainActivity)
            }
            else -> {
                mainActivity.askUser(
                    "What's the change?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_NEW
                )
            }
        }
    }

    /**
     * This function edits given reminder with the new content
     * @param text New content for field
     * @param mainActivity Context
     */
    fun editReminder(text: String, mainActivity: MainActivity) {
        var event = mainActivity.appntmnt.getCalendarUriBase()
        var newEvent = ContentValues()
        var updateUri = ContentUris.withAppendedId(event, reminderID)
        var date: Long
        var localDateTime: LocalDateTime
        when (field) {
            "name" -> {
                newEvent.put(CalendarContract.Events.TITLE, text)
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "I changed the name to $text. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK
                )
            }
            "date" -> {
                //get date from event
                date = mainActivity.appntmnt.getDateFromEvent(mainActivity, event)
                //Calculate into LocalDateTime from millis
                localDateTime =
                    LocalDateTime.ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localDate = mainActivity.appntmnt.parseLocalDate(text)
                //Update date
                localDateTime = localDateTime.withYear(localDate.year)
                localDateTime = localDateTime.withMonth(localDate.monthValue)
                localDateTime = localDateTime.withDayOfMonth(localDate.dayOfMonth)

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "I changed the date to $localDate. Would you like to change anything else?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK
                )

            }
            "time" -> {
                //get time from event
                date = mainActivity.appntmnt.getDateFromEvent(mainActivity, event)

                //Calculate into LocalDateTime from Millis
                localDateTime = //maybe ignore this here and edit it directly in date
                    LocalDateTime.ofEpochSecond(date / 1000, 0, OffsetDateTime.now().offset)
                var localTime = mainActivity.appntmnt.parseLocalTime(text)
                //Update time
                localDateTime = localDateTime.withHour(localTime.hour)
                localDateTime = localDateTime.withMinute(localTime.minute)

                newEvent.put(
                    CalendarContract.Events.DTSTART,
                    localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
                )
                mainActivity.contentResolver.update(updateUri, newEvent, null, null)
                mainActivity.askUser(
                    "I changed the time to $localTime. Would you like to change anything?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_ASK
                )

            }
            else -> {
                //abort dialogue

            }
        }
    }

    /**
     * This function reads given reminder during edit dialogue
     * @param mainActivity Context
     */
    fun readReminderEdit(mainActivity: MainActivity) {
        if (reminderID == 0.toLong()) {
            mainActivity.askUser(
                "What is the name of the memory that I should read?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_READ
            )
        } else {
            val eventUri: Uri = mainActivity.appntmnt.getCalendarUriBase()
            //Create array of id, title, start time and location
            val projection = arrayOf("_id", "title", "dtstart")
            val cursor: Cursor? = mainActivity.contentResolver.query(
                eventUri, null, null, null,
                null
            )
            if (cursor!!.moveToFirst()) {
                var calName: String
                var calID: String
                var calDate: String
                val nameCol: Int = cursor.getColumnIndex(projection[1])
                val idCol: Int = cursor.getColumnIndex(projection[0])
                val dateCol: Int = cursor.getColumnIndex(projection[2])

                //Get all events with matching name
                do {

                    calName = cursor.getString(nameCol)
                    calID = cursor.getString(idCol)
                    calDate = cursor.getString(dateCol)

                    if (calID != null && calID.toLong() == reminderID) {
                        break
                    }
                } while (cursor.moveToNext())
                cursor.close()
                val localDate =
                    LocalDateTime.ofEpochSecond(
                        calDate.toLong() / 1000,
                        0,
                        OffsetDateTime.now().offset
                    )
                mainActivity.askUser(
                    "I remind you of ${localDate.toLocalDate()} at ${localDate.toLocalTime().truncatedTo(ChronoUnit.MINUTES)} of $calName. Would you like to change something?",
                    mainActivity,
                    MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_FIELD
                )
            }
        }
    }

    /**
     * this function updates parameters of this object
     * @param mainActivity context
     * @param text updated field
     */
    internal fun updateParameter(mainActivity: MainActivity, text: String) {
        when (field) {
            "name" -> {
                this.reminderName = text
            }
            "date" -> {
                this.reminderDate = text
            }
            "time" -> {
                this.reminderTime = text
            }
            "location" -> {
                //no location for reminders
                mainActivity.askUser("There is no field named location for a reminder. Do you want do edit something?", mainActivity, MainActivity.REQUEST_CODE_STT_APPOINTMENT_EDIT_CREATION_END)
                return
            }
            "read" -> {
                readData(mainActivity)
                return
            }
        }
        //check where to continue if edit during early creation
        if (this.reminderDate == "") {
            mainActivity.askUser(
                "What date is the appointment?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_DATE
            )
            return
        }
        if (this.reminderTime == "") {
            mainActivity.askUser(
                "What time is the appointment?",
                mainActivity,
                MainActivity.REQUEST_CODE_STT_REMINDER_TIME
            )
            return
        }
        //edit during late creation after all fields are set
        mainActivity.askUser("I have updated $field to $text. Do you wish to edit something else?", mainActivity, MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END)
    }


    /**
     * setter function for name
     * @param recognizedText new name
     */
    fun setName(recognizedText: String) {
        this.reminderName = recognizedText
    }

    /**
     * setter for time
     * @param recognizedText new time
     */
    fun setTime(recognizedText: String) {
        this.reminderTime = recognizedText
    }

    /**
     * setter for date
     * @param recognizedText new date
     */
    fun setDate(recognizedText: String) {
        this.reminderDate = recognizedText
    }

    /**
     * setter for id
     * @param listSelectedCalendars new event id
     */
    fun setEvent(listSelectedCalendars: Long) {
        this.reminderID = listSelectedCalendars
    }

    /**
     * getter for current field
     * @return current field
     */
    fun getField(): String {
        return this.field
    }

    /**
     * setter for current field
     * @param text new field
     */
    fun setField(text: String) {
        this.field = text
    }

    /**
     * this function reads saved parameters for this reminder
     * @param mainActivity context
     */
    fun readData(mainActivity: MainActivity) {
        mainActivity.askUser(
            "The name is $reminderName, the date is $reminderDate and the time is $reminderTime. Do you wish to edit something?",
            mainActivity,
            MainActivity.REQUEST_CODE_STT_REMINDER_EDIT_CREATION_END
        )

    }

    /**
     * this function resets parameters of this object
     */
    private fun resetParameters() {
        this.reminderName = ""
        this.reminderDate = ""
        this.reminderTime = ""
    }

    /**
     * this function adds a random error to this object
     */
    internal fun addError() {
        //get error
        var rng = Random
        println(rng.nextInt(1))
        var index = rng.nextInt(3)
        //set error
        when (index) {
            0 -> {
                reminderName = errorName[rng.nextInt(errorName.size)]
            }
            1 -> {
                reminderDate = errorDate[rng.nextInt(errorDate.size)]
            }
            2 -> {
                reminderTime = errorTime[rng.nextInt(errorTime.size)]

            }
        }
    }

    /**
     * This function checks for valid date and time formats for reminders
     * @param text User input
     * @param mainActivity Context
     */
    internal fun checkDateAndTimeValidity(text: String, mainActivity: MainActivity) {
        //check for field and parses date or time if field matches
        if (this.field == "date") {
            mainActivity.appntmnt.parseLocalDate(text)
        }
        if (this.field == "time") {
            mainActivity.appntmnt.parseLocalTime(text)
        }
    }


}